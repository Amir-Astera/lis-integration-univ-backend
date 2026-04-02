package lab.dev.med.univ.feature.reporting.domain.services

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.reactor.awaitSingle
import kotlinx.coroutines.withContext
import lab.dev.med.univ.feature.reporting.data.entity.toEntity
import lab.dev.med.univ.feature.reporting.data.repository.DamumedReportUploadRepository
import lab.dev.med.univ.feature.reporting.domain.errors.DamumedReportValidationException
import lab.dev.med.univ.feature.reporting.domain.models.DamumedLabReportKind
import lab.dev.med.univ.feature.reporting.domain.models.DamumedReportSourceMode
import lab.dev.med.univ.feature.reporting.domain.models.DamumedReportUpload
import org.apache.poi.ss.usermodel.WorkbookFactory
import org.springframework.core.io.buffer.DataBufferUtils
import org.springframework.http.codec.multipart.FilePart
import org.springframework.stereotype.Service
import project.gigienist_reports.core.concurrency.ExcelLimiter
import project.gigienist_reports.core.util.FileNameUtil
import project.gigienist_reports.core.util.ensureExtensionMatchesWorkbook
import java.io.ByteArrayInputStream
import java.nio.file.Files
import java.nio.file.Path
import java.security.MessageDigest
import java.time.LocalDateTime
import java.util.UUID

interface DamumedManualReportSource {
    suspend fun upload(
        reportKind: DamumedLabReportKind,
        part: FilePart,
        uploadedBy: String? = null,
    ): DamumedReportUpload
}

@Service
internal class DamumedManualReportSourceImpl(
    private val location: Path,
    private val repository: DamumedReportUploadRepository,
    private val excelLimiter: ExcelLimiter,
    private val workbookRawParsingService: DamumedWorkbookRawParsingService,
) : DamumedManualReportSource {
    override suspend fun upload(
        reportKind: DamumedLabReportKind,
        part: FilePart,
        uploadedBy: String?,
    ): DamumedReportUpload {
        val bytes = extractBytes(part)
        if (bytes.isEmpty()) {
            throw DamumedReportValidationException("Uploaded report file is empty.")
        }

        val workbookPath = excelLimiter.withPermit {
            withContext(Dispatchers.IO) {
                try {
                    WorkbookFactory.create(ByteArrayInputStream(bytes)).use { workbook ->
                        val originalBaseName = FileNameUtil.sanitizeForFilename(
                            part.filename().substringBeforeLast('.').ifBlank { reportKind.storageDirectory },
                        )
                        val preliminary = storageDirectory(reportKind).resolve(
                            "${FileNameUtil.timestamp()} - ${originalBaseName}.xlsx",
                        )
                        ensureExtensionMatchesWorkbook(preliminary, workbook)
                    }
                } catch (ex: Exception) {
                    throw DamumedReportValidationException("Uploaded file is not a valid Excel workbook.")
                }
            }
        }

        val targetDirectory = workbookPath.parent
            ?: throw DamumedReportValidationException("Invalid target storage directory.")
        val uniqueTargetPath = withContext(Dispatchers.IO) {
            Files.createDirectories(targetDirectory)
            FileNameUtil.uniquePath(targetDirectory, workbookPath.fileName.toString())
        }

        withContext(Dispatchers.IO) {
            Files.write(uniqueTargetPath, bytes)
        }

        val upload = DamumedReportUpload(
            id = UUID.randomUUID().toString(),
            reportKind = reportKind,
            sourceMode = DamumedReportSourceMode.MANUAL,
            originalFileName = part.filename(),
            storedFileName = uniqueTargetPath.fileName.toString(),
            storagePath = location.relativize(uniqueTargetPath).toString().replace('\\', '/'),
            format = uniqueTargetPath.fileName.toString().substringAfterLast('.', "xlsx"),
            contentType = part.headers().contentType?.toString(),
            checksumSha256 = sha256(bytes),
            sizeBytes = bytes.size.toLong(),
            uploadedAt = LocalDateTime.now(),
            uploadedBy = uploadedBy,
        )

        val persistedUpload = repository.save(upload.toEntity()).let {
            upload.copy(version = it.version)
        }

        return excelLimiter.withPermit {
            withContext(Dispatchers.IO) {
                try {
                    WorkbookFactory.create(ByteArrayInputStream(bytes)).use { workbook ->
                        workbookRawParsingService.parseAndPersist(persistedUpload, workbook)
                    }
                } catch (ex: Exception) {
                    throw DamumedReportValidationException("Uploaded workbook could not be parsed and persisted.")
                }
            }
        }
    }

    private fun storageDirectory(reportKind: DamumedLabReportKind): Path {
        return location.resolve("damumed").resolve("manual").resolve(reportKind.storageDirectory)
    }

    private suspend fun extractBytes(part: FilePart): ByteArray {
        val dataBuffer = DataBufferUtils.join(part.content()).awaitSingle()
        return try {
            ByteArray(dataBuffer.readableByteCount()).also { dataBuffer.read(it) }
        } finally {
            DataBufferUtils.release(dataBuffer)
        }
    }

    private fun sha256(bytes: ByteArray): String {
        val digest = MessageDigest.getInstance("SHA-256").digest(bytes)
        return digest.joinToString(separator = "") { "%02x".format(it) }
    }
}
