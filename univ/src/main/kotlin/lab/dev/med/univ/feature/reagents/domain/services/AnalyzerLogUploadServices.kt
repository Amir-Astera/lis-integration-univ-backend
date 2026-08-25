package lab.dev.med.univ.feature.reagents.domain.services

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.reactor.awaitSingle
import kotlinx.coroutines.withContext
import lab.dev.med.univ.feature.reagents.data.entity.toEntity
import lab.dev.med.univ.feature.reagents.data.entity.toModel
import lab.dev.med.univ.feature.reagents.data.repository.AnalyzerLogUploadRepository
import lab.dev.med.univ.feature.reagents.data.repository.AnalyzerRepository
import lab.dev.med.univ.feature.reagents.data.repository.ParsedAnalyzerSampleRepository
import lab.dev.med.univ.feature.reagents.domain.errors.AnalyzerLogParseUnsupportedException
import lab.dev.med.univ.feature.reagents.domain.services.LogAnomalyAnalysisService
import lab.dev.med.univ.feature.reagents.domain.errors.AnalyzerLogUploadNotFoundException
import lab.dev.med.univ.feature.reagents.domain.errors.AnalyzerLogValidationException
import lab.dev.med.univ.feature.reagents.domain.models.AnalyzerLogParseStatus
import lab.dev.med.univ.feature.reagents.domain.models.AnalyzerLogSourceType
import lab.dev.med.univ.feature.reagents.domain.models.AnalyzerLogUpload
import lab.dev.med.univ.feature.reagents.domain.models.ParsedAnalyzerSample
import lab.dev.med.univ.feature.reagents.domain.models.SampleClassification
import org.slf4j.LoggerFactory
import org.springframework.core.io.buffer.DataBufferUtils
import org.springframework.http.codec.multipart.FilePart
import org.springframework.stereotype.Service
import project.gigienist_reports.core.util.FileNameUtil
import java.nio.file.Files
import java.nio.file.Path
import java.security.MessageDigest
import java.time.LocalDateTime
import java.util.UUID

interface AnalyzerLogUploadQueryService {
    suspend fun getUploads(analyzerId: String? = null): List<AnalyzerLogUpload>
    suspend fun getParsedSamples(uploadId: String): List<ParsedAnalyzerSample>
}

interface AnalyzerLogUploadIngestionService {
    suspend fun upload(
        sourceType: AnalyzerLogSourceType,
        analyzerId: String?,
        part: FilePart,
        uploadedBy: String? = null,
    ): AnalyzerLogUpload

    suspend fun parseAndPersist(uploadId: String): AnalyzerLogUpload
}

@Service
internal class AnalyzerLogUploadQueryServiceImpl(
    private val uploadRepository: AnalyzerLogUploadRepository,
    private val parsedAnalyzerSampleRepository: ParsedAnalyzerSampleRepository,
) : AnalyzerLogUploadQueryService {

    override suspend fun getUploads(analyzerId: String?): List<AnalyzerLogUpload> {
        val uploads = if (analyzerId.isNullOrBlank()) {
            uploadRepository.findAllByOrderByUploadedAtDesc()
        } else {
            uploadRepository.findAllByAnalyzerIdOrderByUploadedAtDesc(analyzerId)
        }
        return uploads.toList().map { it.toModel() }
    }

    override suspend fun getParsedSamples(uploadId: String): List<ParsedAnalyzerSample> {
        ensureUploadExists(uploadId)
        return parsedAnalyzerSampleRepository.findAllByLogUploadIdOrderBySampleTimestampAsc(uploadId)
            .toList()
            .map { it.toModel() }
    }

    private suspend fun ensureUploadExists(uploadId: String) {
        if (uploadRepository.findById(uploadId) == null) {
            throw AnalyzerLogUploadNotFoundException(uploadId)
        }
    }
}

@Service
internal class AnalyzerLogUploadIngestionServiceImpl(
    private val location: Path,
    private val analyzerRepository: AnalyzerRepository,
    private val uploadRepository: AnalyzerLogUploadRepository,
    private val parsedAnalyzerSampleRepository: ParsedAnalyzerSampleRepository,
    private val applogsParserService: ApplogsParserService,
    private val errorsXmlParserService: ErrorsXmlParserService,
    private val logAnomalyAnalysisService: LogAnomalyAnalysisService,
    private val completedJournalReconciliationService: AnalyzerLogCompletedJournalReconciliationService,
    private val reconciliationSummaryService: ReconciliationSummaryService,
) : AnalyzerLogUploadIngestionService {

    private val log = LoggerFactory.getLogger(AnalyzerLogUploadIngestionServiceImpl::class.java)

    override suspend fun upload(
        sourceType: AnalyzerLogSourceType,
        analyzerId: String?,
        part: FilePart,
        uploadedBy: String?,
    ): AnalyzerLogUpload {
        analyzerId?.let {
            if (analyzerRepository.findById(it) == null) {
                throw AnalyzerLogValidationException("Analyzer '$it' was not found.")
            }
        }

        validateFileName(sourceType, part.filename())
        val bytes = extractBytes(part)
        if (bytes.isEmpty()) {
            throw AnalyzerLogValidationException("Uploaded analyzer log file is empty.")
        }

        val originalBaseName = FileNameUtil.sanitizeForFilename(
            part.filename().substringBeforeLast('.').ifBlank { sourceType.name.lowercase() },
        )
        val extension = part.filename().substringAfterLast('.', defaultExtension(sourceType))
        val targetDirectory = storageDirectory(sourceType)
        val uniqueTargetPath = withContext(Dispatchers.IO) {
            Files.createDirectories(targetDirectory)
            FileNameUtil.uniquePath(
                targetDirectory,
                "${FileNameUtil.timestamp()} - $originalBaseName.$extension",
            )
        }

        withContext(Dispatchers.IO) {
            Files.write(uniqueTargetPath, bytes)
        }

        val upload = AnalyzerLogUpload(
            id = UUID.randomUUID().toString(),
            analyzerId = analyzerId,
            sourceType = sourceType,
            originalFileName = part.filename(),
            storedFileName = uniqueTargetPath.fileName.toString(),
            storagePath = location.relativize(uniqueTargetPath).toString().replace('\\', '/'),
            fileSizeBytes = bytes.size.toLong(),
            checksumSha256 = sha256(bytes),
            parseStatus = AnalyzerLogParseStatus.PENDING,
            uploadedAt = LocalDateTime.now(),
            uploadedBy = uploadedBy,
        )

        return uploadRepository.save(upload.toEntity()).toModel()
    }

    override suspend fun parseAndPersist(uploadId: String): AnalyzerLogUpload {
        // Always re-read fresh entity from DB to avoid OptimisticLockingFailureException
        // when multiple Applogs files are uploaded for the same day or in quick succession.
        val existing = uploadRepository.findById(uploadId)?.toModel()
            ?: throw AnalyzerLogUploadNotFoundException(uploadId)

        // Only start processing if not already in progress/parsed to prevent duplicate runs
        if (existing.parseStatus == AnalyzerLogParseStatus.PROCESSING) {
            return existing
        }

        // Re-read right before save to get latest version
        val freshForProcessing = uploadRepository.findById(uploadId)?.toModel()
            ?: throw AnalyzerLogUploadNotFoundException(uploadId)

        val processing = uploadRepository.save(
            freshForProcessing.copy(
                parseStatus = AnalyzerLogParseStatus.PROCESSING,
                parseStartedAt = LocalDateTime.now(),
                parseCompletedAt = null,
                parseErrorMessage = null,
            ).toEntity(),
        ).toModel()

        return try {
            when (processing.sourceType) {
                AnalyzerLogSourceType.APPLOGS -> parseApplogsUpload(processing)
                AnalyzerLogSourceType.ERRORS_XML -> parseErrorsXmlUpload(processing)
                AnalyzerLogSourceType.USB_EXPORT -> throw AnalyzerLogParseUnsupportedException(processing.sourceType)
            }
        } catch (ex: Exception) {
            // Re-read again before marking as failed to avoid version conflict
            val freshForFailed = uploadRepository.findById(uploadId)?.toModel()
            if (freshForFailed != null) {
                runCatching {
                    uploadRepository.save(
                        freshForFailed.copy(
                            parseStatus = AnalyzerLogParseStatus.FAILED,
                            parseCompletedAt = LocalDateTime.now(),
                            parseErrorMessage = ex.message,
                        ).toEntity(),
                    )
                }
            }
            throw ex
        }
    }

    private suspend fun parseApplogsUpload(processing: AnalyzerLogUpload): AnalyzerLogUpload {
        val content = readStoredContent(processing.storagePath)
        val parsed = applogsParserService.parse(processing.id, processing.analyzerId, content)

        val reconciledSamples = completedJournalReconciliationService.reconcileApplogsSamples(parsed.samples)

        // Idempotent re-parse: drop samples that previously belonged to THIS upload
        // (e.g. retry after a parse failure). Cross-upload deduplication is handled below.
        parsedAnalyzerSampleRepository.deleteAllByLogUploadId(processing.id)

        // -------------------------------------------------------------
        // Cross-upload deduplication
        // -------------------------------------------------------------
        // A physical analyzer event is identified by (analyzer_id, barcode, sample_timestamp).
        // The same triplet can legitimately appear in multiple log files:
        //   - multiple snapshots of a growing Applogs.txt (every 30 min the file is bigger)
        //   - a rolled Applogs<timestamp>.log whose content was previously captured live
        //   - a manually re-uploaded Applogs.txt
        //
        // We pre-filter the parsed batch against samples already in the DB for this analyzer
        // within the same time window. Combined with the unique partial index installed in
        // V27 (uq_parsed_sample_dedupe), this makes duplicate inserts impossible.
        val analyzerId = processing.analyzerId
        val (uniqueSamples, droppedDuplicates) = if (analyzerId != null && reconciledSamples.isNotEmpty()) {
            val candidates = reconciledSamples.filter { it.barcode.isNotBlank() }
            if (candidates.isEmpty()) {
                reconciledSamples to 0
            } else {
                val minTs = candidates.minOf { it.sampleTimestamp }
                val maxTs = candidates.maxOf { it.sampleTimestamp }
                val existingKeys = parsedAnalyzerSampleRepository
                    .findAllByAnalyzerIdAndSampleTimestampBetweenOrderBySampleTimestampAsc(
                        analyzerId, minTs, maxTs,
                    )
                    .toList()
                    .map { it.barcode to it.sampleTimestamp }
                    .toSet()

                val filtered = reconciledSamples.filter { sample ->
                    sample.barcode.isBlank() ||
                        (sample.barcode to sample.sampleTimestamp) !in existingKeys
                }
                filtered to (reconciledSamples.size - filtered.size)
            }
        } else {
            reconciledSamples to 0
        }

        if (droppedDuplicates > 0) {
            log.info(
                "Applogs upload {}: skipped {} duplicate sample(s) already present for analyzer {} in [{}, {}]",
                processing.id, droppedDuplicates, analyzerId, parsed.logPeriodStart, parsed.logPeriodEnd,
            )
        }

        if (uniqueSamples.isNotEmpty()) {
            parsedAnalyzerSampleRepository.saveAll(uniqueSamples.map { it.toEntity() }).toList()
        }

        // Re-read fresh entity from DB to avoid optimistic locking conflict
        // (version may have changed if another thread touched the record)
        val fresh = uploadRepository.findById(processing.id)?.toModel() ?: processing
        val completed = fresh.copy(
            parseStatus = AnalyzerLogParseStatus.PARSED,
            parseCompletedAt = LocalDateTime.now(),
            parseErrorMessage = null,
            totalLinesParsed = parsed.totalLinesParsed,
            totalSamplesFound = reconciledSamples.size,
            legitimateSamples = reconciledSamples.count { it.classification == SampleClassification.LEGITIMATE },
            unauthorizedSamples = reconciledSamples.count { it.classification == SampleClassification.SUSPICIOUS },
            washTestSamples = reconciledSamples.count { it.classification == SampleClassification.WASH_TEST },
            rerunSamples = reconciledSamples.count { it.classification == SampleClassification.PROBABLE_RERUN },
            logPeriodStart = parsed.logPeriodStart,
            logPeriodEnd = parsed.logPeriodEnd,
        )
        val saved = uploadRepository.save(completed.toEntity()).toModel()
        logAnomalyAnalysisService.buildAnomaliesFromUpload(processing.id)

        // After a fresh log batch lands, recompute reconciliation_summary for the affected
        // dates so the dashboard reflects the new samples (and any late LIS matches that
        // resolved earlier discrepancies).
        rebuildReconciliationForUpload(saved)
        return saved
    }

    private suspend fun parseErrorsXmlUpload(processing: AnalyzerLogUpload): AnalyzerLogUpload {
        val bytes = readStoredBytes(processing.storagePath)
        val parsed = errorsXmlParserService.parse(bytes)

        // -------------------------------------------------------------
        // errors.xml is a current-state snapshot (the analyzer rewrites it),
        // not append-only. Each new upload supersedes the previous one for
        // this analyzer. We delete any prior errors.xml upload (and its samples
        // via FK CASCADE) so the same barcode never accumulates duplicates.
        // -------------------------------------------------------------
        val analyzerId = processing.analyzerId
        if (analyzerId != null) {
            val prior = uploadRepository
                .findFirstByAnalyzerIdAndSourceTypeOrderByUploadedAtDesc(
                    analyzerId, AnalyzerLogSourceType.ERRORS_XML,
                )
            if (prior != null && prior.id != processing.id) {
                log.info(
                    "Superseding previous errors.xml upload {} for analyzer {} (new upload {})",
                    prior.id, analyzerId, processing.id,
                )
                runCatching { uploadRepository.deleteById(prior.id) }
                    .onFailure { log.warn("Failed to delete previous errors.xml upload ${prior.id}", it) }
            }
        }

        parsedAnalyzerSampleRepository.deleteAllByLogUploadId(processing.id)

        // Use upload time as a deterministic timestamp shared across all samples in
        // this errors.xml. The XML itself has no per-sample timestamps, so without this
        // every parse pass would produce different LocalDateTime.now() values and break
        // the (analyzer_id, barcode, sample_timestamp) dedup key on retries.
        val sharedTimestamp = processing.uploadedAt

        val convertedSamples = parsed.samples.map { record ->
            ParsedAnalyzerSample(
                id = UUID.randomUUID().toString(),
                logUploadId = processing.id,
                analyzerId = processing.analyzerId,
                sampleTimestamp = sharedTimestamp,
                barcode = record.barcode,
                deviceSystemName = record.deviceSystemName,
                deviceName = record.deviceName,
                lisAnalyzerId = null,
                testMode = record.testMode,
                bloodMode = record.bloodMode,
                takeMode = record.takeMode,
                orderResearchId = null,
                orderId = null,
                serviceId = null,
                serviceName = null,
                hasLisOrder = false,
                sampleRequestCount = 0,
                wbcValue = record.wbcValue,
                rbcValue = record.rbcValue,
                hgbValue = record.hgbValue,
                pltValue = record.pltValue,
                classification = SampleClassification.XML_RESULT,
                classificationReason = "Parsed from errors.xml — results present without LIS confirmation",
            )
        }

        if (convertedSamples.isNotEmpty()) {
            parsedAnalyzerSampleRepository.saveAll(convertedSamples.map { it.toEntity() }).toList()
        }

        // Re-read fresh entity from DB to get current version before final save
        val fresh = uploadRepository.findById(processing.id)?.toModel() ?: processing
        val completed = fresh.copy(
            parseStatus = AnalyzerLogParseStatus.PARSED,
            parseCompletedAt = LocalDateTime.now(),
            parseErrorMessage = null,
            totalLinesParsed = String(bytes, Charsets.UTF_8).lineSequence().count(),
            totalSamplesFound = parsed.sampleCount,
            legitimateSamples = 0,
            unauthorizedSamples = parsed.sampleCount,
            washTestSamples = 0,
            rerunSamples = 0,
            logPeriodStart = null,
            logPeriodEnd = null,
        )
        val saved = uploadRepository.save(completed.toEntity()).toModel()
        logAnomalyAnalysisService.buildAnomaliesFromUpload(processing.id)
        rebuildReconciliationForUpload(saved)
        return saved
    }

    /**
     * Recompute reconciliation_summary for the date range covered by [upload].
     * This refreshes KPIs and per-sample status (including PENDING_GRACE → RECONCILED
     * transitions when LIS data has caught up since the previous run).
     *
     * Errors are swallowed — the upload itself has succeeded; a summary rebuild failure
     * must not roll back the new samples (we'd lose the data we just ingested).
     */
    private suspend fun rebuildReconciliationForUpload(upload: AnalyzerLogUpload) {
        val analyzerId = upload.analyzerId ?: return
        val periodStart = upload.logPeriodStart?.toLocalDate()
            ?: upload.uploadedAt.toLocalDate().minusDays(1)
        val periodEnd = upload.logPeriodEnd?.toLocalDate()
            ?: upload.uploadedAt.toLocalDate()
        runCatching {
            reconciliationSummaryService.rebuildForDateRange(periodStart, periodEnd, analyzerId)
        }.onFailure { ex ->
            log.warn(
                "Reconciliation rebuild failed after upload {} (analyzer={}, {}..{})",
                upload.id, analyzerId, periodStart, periodEnd, ex,
            )
        }
    }

    private fun validateFileName(sourceType: AnalyzerLogSourceType, filename: String) {
        val normalized = filename.lowercase()
        when (sourceType) {
            AnalyzerLogSourceType.APPLOGS -> {
                if (!normalized.endsWith(".log") && !normalized.endsWith(".txt")) {
                    throw AnalyzerLogValidationException("Applogs upload must be a .log or .txt file.")
                }
            }
            AnalyzerLogSourceType.ERRORS_XML -> {
                if (!normalized.endsWith(".xml")) {
                    throw AnalyzerLogValidationException("errors.xml upload must be an .xml file.")
                }
            }
            AnalyzerLogSourceType.USB_EXPORT -> {
                if (!normalized.endsWith(".csv") && !normalized.endsWith(".txt") && !normalized.endsWith(".xml")) {
                    throw AnalyzerLogValidationException("USB export upload must be .csv, .txt, or .xml.")
                }
            }
        }
    }

    private fun defaultExtension(sourceType: AnalyzerLogSourceType): String {
        return when (sourceType) {
            AnalyzerLogSourceType.APPLOGS -> "log"
            AnalyzerLogSourceType.ERRORS_XML -> "xml"
            AnalyzerLogSourceType.USB_EXPORT -> "txt"
        }
    }

    private fun storageDirectory(sourceType: AnalyzerLogSourceType): Path {
        return location.resolve("reagents").resolve("logs").resolve(sourceType.name.lowercase())
    }

    private suspend fun extractBytes(part: FilePart): ByteArray {
        val dataBuffer = DataBufferUtils.join(part.content()).awaitSingle()
        return try {
            ByteArray(dataBuffer.readableByteCount()).also { dataBuffer.read(it) }
        } finally {
            DataBufferUtils.release(dataBuffer)
        }
    }

    private suspend fun readStoredContent(relativeStoragePath: String): String {
        val filePath = location.resolve(relativeStoragePath).normalize()
        if (!filePath.startsWith(location) || !Files.exists(filePath)) {
            throw AnalyzerLogValidationException("Stored analyzer log file is missing or inaccessible.")
        }
        return withContext(Dispatchers.IO) {
            String(Files.readAllBytes(filePath), Charsets.UTF_8)
        }
    }

    private suspend fun readStoredBytes(relativeStoragePath: String): ByteArray {
        val filePath = location.resolve(relativeStoragePath).normalize()
        if (!filePath.startsWith(location) || !Files.exists(filePath)) {
            throw AnalyzerLogValidationException("Stored analyzer log file is missing or inaccessible.")
        }
        return withContext(Dispatchers.IO) {
            Files.readAllBytes(filePath)
        }
    }

    private fun sha256(bytes: ByteArray): String {
        val digest = MessageDigest.getInstance("SHA-256").digest(bytes)
        return digest.joinToString(separator = "") { "%02x".format(it) }
    }
}
