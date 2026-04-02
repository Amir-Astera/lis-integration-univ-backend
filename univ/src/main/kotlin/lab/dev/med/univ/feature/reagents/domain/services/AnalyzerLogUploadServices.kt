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
import lab.dev.med.univ.feature.reagents.domain.errors.AnalyzerLogUploadNotFoundException
import lab.dev.med.univ.feature.reagents.domain.errors.AnalyzerLogValidationException
import lab.dev.med.univ.feature.reagents.domain.models.AnalyzerLogParseStatus
import lab.dev.med.univ.feature.reagents.domain.models.AnalyzerLogSourceType
import lab.dev.med.univ.feature.reagents.domain.models.AnalyzerLogUpload
import lab.dev.med.univ.feature.reagents.domain.models.ParsedAnalyzerSample
import lab.dev.med.univ.feature.reagents.domain.models.SampleClassification
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
) : AnalyzerLogUploadIngestionService {

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
        val existing = uploadRepository.findById(uploadId)?.toModel()
            ?: throw AnalyzerLogUploadNotFoundException(uploadId)

        val processing = uploadRepository.save(
            existing.copy(
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
            uploadRepository.save(
                processing.copy(
                    parseStatus = AnalyzerLogParseStatus.FAILED,
                    parseCompletedAt = LocalDateTime.now(),
                    parseErrorMessage = ex.message,
                ).toEntity(),
            )
            throw ex
        }
    }

    private suspend fun parseApplogsUpload(processing: AnalyzerLogUpload): AnalyzerLogUpload {
        val content = readStoredContent(processing.storagePath)
        val parsed = applogsParserService.parse(processing.id, processing.analyzerId, content)

        parsedAnalyzerSampleRepository.deleteAllByLogUploadId(processing.id)
        parsedAnalyzerSampleRepository.saveAll(parsed.samples.map { it.toEntity() }).toList()

        val completed = processing.copy(
            parseStatus = AnalyzerLogParseStatus.PARSED,
            parseCompletedAt = LocalDateTime.now(),
            parseErrorMessage = null,
            totalLinesParsed = parsed.totalLinesParsed,
            totalSamplesFound = parsed.samples.size,
            legitimateSamples = parsed.samples.count { it.classification == SampleClassification.LEGITIMATE },
            unauthorizedSamples = parsed.samples.count { it.classification == SampleClassification.SUSPICIOUS },
            washTestSamples = parsed.samples.count { it.classification == SampleClassification.WASH_TEST },
            rerunSamples = parsed.samples.count { it.classification == SampleClassification.PROBABLE_RERUN },
            logPeriodStart = parsed.logPeriodStart,
            logPeriodEnd = parsed.logPeriodEnd,
        )
        return uploadRepository.save(completed.toEntity()).toModel()
    }

    private suspend fun parseErrorsXmlUpload(processing: AnalyzerLogUpload): AnalyzerLogUpload {
        val content = readStoredContent(processing.storagePath)
        val parsed = errorsXmlParserService.parse(content)

        parsedAnalyzerSampleRepository.deleteAllByLogUploadId(processing.id)

        val completed = processing.copy(
            parseStatus = AnalyzerLogParseStatus.PARSED,
            parseCompletedAt = LocalDateTime.now(),
            parseErrorMessage = null,
            totalLinesParsed = content.lineSequence().count(),
            totalSamplesFound = parsed.sampleCount,
            legitimateSamples = 0,
            unauthorizedSamples = 0,
            washTestSamples = 0,
            rerunSamples = 0,
            logPeriodStart = null,
            logPeriodEnd = null,
        )
        return uploadRepository.save(completed.toEntity()).toModel()
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

    private fun sha256(bytes: ByteArray): String {
        val digest = MessageDigest.getInstance("SHA-256").digest(bytes)
        return digest.joinToString(separator = "") { "%02x".format(it) }
    }
}
