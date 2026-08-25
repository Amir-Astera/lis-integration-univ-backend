package lab.dev.med.univ.feature.agent.presentation

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import kotlinx.coroutines.reactor.awaitSingle
import lab.dev.med.univ.feature.agent.domain.AgentAuthentication
import lab.dev.med.univ.feature.agent.domain.ByteArrayFilePart
import lab.dev.med.univ.feature.reagents.data.repository.AnalyzerLogUploadRepository
import lab.dev.med.univ.feature.reagents.domain.errors.AnalyzerLogParseUnsupportedException
import lab.dev.med.univ.feature.reagents.domain.errors.AnalyzerLogUploadNotFoundException
import lab.dev.med.univ.feature.reagents.domain.errors.AnalyzerLogValidationException
import lab.dev.med.univ.feature.reagents.domain.models.AnalyzerLogSourceType
import lab.dev.med.univ.feature.reagents.domain.services.AnalyzerLogUploadIngestionService
import org.slf4j.LoggerFactory
import org.springframework.core.io.buffer.DataBufferUtils
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.http.codec.multipart.FilePart
import org.springframework.security.core.context.ReactiveSecurityContextHolder
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RequestPart
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.server.ResponseStatusException
import org.springframework.web.server.ServerWebExchange
import java.security.MessageDigest
import java.time.LocalDateTime

/**
 * Endpoint consumed exclusively by the Windows log-uploader agent (LIMSApodUer).
 * Authentication: X-Agent-Key header (handled by AgentSecurityFilter upstream).
 *
 * Idempotency contract: requests are deduplicated by (analyzerId, sha256). If the same
 * file content has already been uploaded for this analyzer, the existing upload record
 * is returned with idempotent=true and parsing is skipped.
 */
@RestController
@RequestMapping("/api/agent")
@Tag(name = "agent", description = "Windows log-uploader agent API")
class AgentLogController(
    private val ingestionService: AnalyzerLogUploadIngestionService,
    private val uploadRepository: AnalyzerLogUploadRepository,
) {
    private val log = LoggerFactory.getLogger(AgentLogController::class.java)

    /**
     * POST /api/agent/log-uploads/{sourceType}
     *
     * Multipart fields:
     *   file        (required) — log file bytes
     *   analyzerId  (required) — id from the `analyzers` table
     *   clientMtime (optional) — ISO-8601 timestamp of the source file on disk
     */
    @PostMapping(
        "/log-uploads/{sourceType}",
        consumes = [MediaType.MULTIPART_FORM_DATA_VALUE],
    )
    @Operation(summary = "Agent log upload (idempotent by sha256)")
    suspend fun uploadLog(
        @PathVariable sourceType: AnalyzerLogSourceType,
        @RequestPart("file") file: FilePart,
        @RequestParam analyzerId: String,
        @RequestParam(required = false) clientMtime: String?,
        exchange: ServerWebExchange,
    ): ResponseEntity<AgentUploadResponseDto> {
        return try {
            val agentName = resolveAgentName()
            val bytes = readBytes(file)
            if (bytes.isEmpty()) {
                throw AnalyzerLogValidationException("Uploaded file is empty.")
            }
            val sha256 = sha256Hex(bytes)

            // Idempotency check — if same file content for this analyzer already exists, return it.
            val existing = uploadRepository.findByAnalyzerIdAndChecksumSha256(analyzerId, sha256)
            if (existing != null) {
                log.info("Agent '{}' upload skipped (idempotent): analyzer={}, sha256={}, existingId={}",
                    agentName, analyzerId, sha256, existing.id)
                return ResponseEntity.ok(
                    AgentUploadResponseDto(
                        uploadId = existing.id,
                        status = existing.parseStatus.name,
                        idempotent = true,
                        message = "Already uploaded — skipped",
                    ),
                )
            }

            // Wrap the pre-read bytes in a synthetic FilePart so we can reuse the existing pipeline.
            val syntheticPart = ByteArrayFilePart(file.filename(), bytes)
            val upload = ingestionService.upload(
                sourceType = sourceType,
                analyzerId = analyzerId,
                part = syntheticPart,
                uploadedBy = "agent:$agentName",
            )

            val parsed = try {
                ingestionService.parseAndPersist(upload.id)
            } catch (ex: AnalyzerLogParseUnsupportedException) {
                upload // unsupported source types still saved; parsing skipped
            }

            log.info("Agent '{}' uploaded: analyzer={}, file={}, uploadId={}, status={}",
                agentName, analyzerId, file.filename(), parsed.id, parsed.parseStatus)

            ResponseEntity.status(HttpStatus.CREATED).body(
                AgentUploadResponseDto(
                    uploadId = parsed.id,
                    status = parsed.parseStatus.name,
                    idempotent = false,
                    message = "Uploaded and parsed",
                ),
            )
        } catch (ex: Exception) {
            throw mapException(ex)
        }
    }

    /** POST /api/agent/heartbeat */
    @PostMapping("/heartbeat")
    @Operation(summary = "Agent heartbeat")
    suspend fun heartbeat(): ResponseEntity<AgentHeartbeatResponseDto> {
        val agentName = resolveAgentName()
        return ResponseEntity.ok(
            AgentHeartbeatResponseDto(
                status = "ok",
                agent = agentName,
                serverTime = LocalDateTime.now().toString(),
            ),
        )
    }

    // -------------------------------------------------------------------------

    private suspend fun resolveAgentName(): String =
        ReactiveSecurityContextHolder.getContext()
            .map { ctx -> (ctx.authentication as? AgentAuthentication)?.agentName ?: "unknown" }
            .awaitSingle()

    private suspend fun readBytes(part: FilePart): ByteArray {
        val dataBuffer = DataBufferUtils.join(part.content()).awaitSingle()
        return try {
            ByteArray(dataBuffer.readableByteCount()).also { dataBuffer.read(it) }
        } finally {
            DataBufferUtils.release(dataBuffer)
        }
    }

    private fun sha256Hex(bytes: ByteArray): String {
        val digest = MessageDigest.getInstance("SHA-256").digest(bytes)
        return digest.joinToString("") { "%02x".format(it) }
    }

    private fun mapException(ex: Exception): ResponseStatusException = when (ex) {
        is ResponseStatusException -> ex
        is AnalyzerLogValidationException -> ResponseStatusException(HttpStatus.BAD_REQUEST, ex.message, ex)
        is AnalyzerLogUploadNotFoundException -> ResponseStatusException(HttpStatus.NOT_FOUND, ex.message, ex)
        is AnalyzerLogParseUnsupportedException -> ResponseStatusException(HttpStatus.NOT_IMPLEMENTED, ex.message, ex)
        else -> {
            log.error("Agent upload failed unexpectedly", ex)
            ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, ex.message, ex)
        }
    }
}

data class AgentUploadResponseDto(
    val uploadId: String,
    val status: String,
    val idempotent: Boolean,
    val message: String,
)

data class AgentHeartbeatResponseDto(
    val status: String,
    val agent: String,
    val serverTime: String,
)
