package lab.dev.med.univ.feature.reagents.presentation.rest

import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import kotlinx.coroutines.reactive.awaitFirst
import lab.dev.med.univ.feature.reagents.domain.errors.AnalyzerLogParseUnsupportedException
import lab.dev.med.univ.feature.reagents.domain.errors.AnalyzerLogUploadNotFoundException
import lab.dev.med.univ.feature.reagents.domain.errors.AnalyzerLogValidationException
import lab.dev.med.univ.feature.reagents.domain.models.AnalyzerLogSourceType
import lab.dev.med.univ.feature.reagents.domain.usecases.BatchUploadAnalyzerLogsUseCase
import lab.dev.med.univ.feature.reagents.domain.usecases.GetAnalyzerLogUploadsUseCase
import lab.dev.med.univ.feature.reagents.domain.usecases.GetParsedAnalyzerSamplesUseCase
import lab.dev.med.univ.feature.reagents.domain.usecases.ParseAnalyzerLogUploadUseCase
import lab.dev.med.univ.feature.reagents.domain.usecases.UploadAnalyzerLogUseCase
import lab.dev.med.univ.feature.reagents.presentation.dto.AnalyzerLogUploadResponseDto
import lab.dev.med.univ.feature.reagents.presentation.dto.BatchAnalyzerLogResultDto
import lab.dev.med.univ.feature.reagents.presentation.dto.ParsedAnalyzerSampleResponseDto
import lab.dev.med.univ.feature.reagents.presentation.dto.toDto
import lab.dev.med.univ.feature.reagents.presentation.dto.toResponseDto
import org.slf4j.Logger
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.http.codec.multipart.FilePart
import org.springframework.http.server.reactive.ServerHttpRequest
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RequestPart
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.server.ResponseStatusException
import org.springframework.web.server.ServerWebExchange
import project.gigienist_reports.core.config.api.Controller
import project.gigienist_reports.core.security.SessionUser
import project.gigienist_reports.core.security.firebase.FirebaseSecurityUtils
import project.gigienist_reports.feature.users.domain.services.UserAggregateService

@RestController
@RequestMapping("/api/reagents/log-uploads")
@Tag(name = "reagents-log-uploads", description = "Analyzer log upload and parsing API")
@SecurityRequirement(name = "security_auth")
class AnalyzerLogController(
    logger: Logger,
    private val uploadAnalyzerLogUseCase: UploadAnalyzerLogUseCase,
    private val batchUploadAnalyzerLogsUseCase: BatchUploadAnalyzerLogsUseCase,
    private val parseAnalyzerLogUploadUseCase: ParseAnalyzerLogUploadUseCase,
    private val getAnalyzerLogUploadsUseCase: GetAnalyzerLogUploadsUseCase,
    private val getParsedAnalyzerSamplesUseCase: GetParsedAnalyzerSamplesUseCase,
    private val userAggregateService: UserAggregateService,
) : Controller(logger) {

    @GetMapping
    suspend fun getUploads(
        @RequestParam(required = false) analyzerId: String?,
        exchange: ServerWebExchange,
    ): ResponseEntity<List<AnalyzerLogUploadResponseDto>> {
        return try {
            ensureAdmin(exchange)
            ResponseEntity.ok(getAnalyzerLogUploadsUseCase(analyzerId).map { it.toResponseDto() })
        } catch (ex: Exception) {
            throw mapException(ex)
        }
    }

    @GetMapping("/{uploadId}/samples")
    suspend fun getParsedSamples(
        @PathVariable uploadId: String,
        exchange: ServerWebExchange,
    ): ResponseEntity<List<ParsedAnalyzerSampleResponseDto>> {
        return try {
            ensureAdmin(exchange)
            ResponseEntity.ok(getParsedAnalyzerSamplesUseCase(uploadId).map { it.toResponseDto() })
        } catch (ex: Exception) {
            throw mapException(ex)
        }
    }

    @PostMapping(
        "/manual/{sourceType}",
        consumes = [MediaType.MULTIPART_FORM_DATA_VALUE],
    )
    suspend fun uploadManual(
        @PathVariable sourceType: AnalyzerLogSourceType,
        @RequestPart("file") file: FilePart,
        @RequestParam(required = false) analyzerId: String?,
        @RequestParam(required = false, defaultValue = "true") autoParse: Boolean,
        request: ServerHttpRequest,
        exchange: ServerWebExchange,
    ): ResponseEntity<AnalyzerLogUploadResponseDto> {
        return try {
            ensureAdmin(exchange)
            val upload = uploadAnalyzerLogUseCase(
                sourceType = sourceType,
                analyzerId = analyzerId,
                part = file,
                uploadedBy = getSessionUser(exchange).login,
                autoParse = autoParse,
            ).toResponseDto()
            ResponseEntity.status(HttpStatus.CREATED)
                .header("Location", "${request.uri}/${upload.id}")
                .body(upload)
        } catch (ex: Exception) {
            throw mapException(ex)
        }
    }

    @PostMapping(
        "/manual/{sourceType}/batch",
        consumes = [MediaType.MULTIPART_FORM_DATA_VALUE],
    )
    suspend fun uploadManualBatch(
        @PathVariable sourceType: AnalyzerLogSourceType,
        @RequestPart("files") files: List<FilePart>,
        @RequestParam(required = false) analyzerId: String?,
        @RequestParam(required = false, defaultValue = "true") autoParse: Boolean,
        exchange: ServerWebExchange,
    ): ResponseEntity<List<BatchAnalyzerLogResultDto>> {
        return try {
            ensureAdmin(exchange)
            if (files.isEmpty()) {
                throw AnalyzerLogValidationException(
                    "Не переданы файлы. Добавьте в multipart одну или несколько частей с именем поля \"files\".",
                )
            }
            val results = batchUploadAnalyzerLogsUseCase(
                sourceType = sourceType,
                analyzerId = analyzerId,
                parts = files,
                uploadedBy = getSessionUser(exchange).login,
                autoParse = autoParse,
            ).map { it.toDto() }
            ResponseEntity.status(HttpStatus.MULTI_STATUS).body(results)
        } catch (ex: Exception) {
            throw mapException(ex)
        }
    }

    @PostMapping("/{uploadId}/parse")
    suspend fun parseUpload(
        @PathVariable uploadId: String,
        exchange: ServerWebExchange,
    ): ResponseEntity<AnalyzerLogUploadResponseDto> {
        return try {
            ensureAdmin(exchange)
            ResponseEntity.ok(parseAnalyzerLogUploadUseCase(uploadId).toResponseDto())
        } catch (ex: Exception) {
            throw mapException(ex)
        }
    }

    private suspend fun getSessionUser(exchange: ServerWebExchange): SessionUser {
        return FirebaseSecurityUtils.getUserFromRequest(exchange).awaitFirst()
    }

    private suspend fun ensureAdmin(exchange: ServerWebExchange) {
        userAggregateService.checkAdminPrivilegesBySession(getSessionUser(exchange))
    }

    private fun mapException(ex: Exception): ResponseStatusException {
        return when (ex) {
            is AnalyzerLogValidationException -> ResponseStatusException(HttpStatus.BAD_REQUEST, ex.message, ex)
            is AnalyzerLogUploadNotFoundException -> ResponseStatusException(HttpStatus.NOT_FOUND, ex.message, ex)
            is AnalyzerLogParseUnsupportedException -> ResponseStatusException(HttpStatus.NOT_IMPLEMENTED, ex.message, ex)
            else -> {
                val (code, message) = getError(ex)
                ResponseStatusException(code, message, ex)
            }
        }
    }
}
