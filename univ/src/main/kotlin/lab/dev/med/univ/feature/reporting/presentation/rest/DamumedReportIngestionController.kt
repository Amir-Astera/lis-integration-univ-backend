package lab.dev.med.univ.feature.reporting.presentation.rest

import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import kotlinx.coroutines.reactive.awaitFirst
import lab.dev.med.univ.feature.reporting.domain.errors.DamumedApiIntegrationNotReadyException
import lab.dev.med.univ.feature.reporting.domain.errors.DamumedReportSourceModeMismatchException
import lab.dev.med.univ.feature.reporting.domain.errors.DamumedReportValidationException
import lab.dev.med.univ.feature.reporting.domain.models.DamumedEmployeeCompletedStudiesSummaryProcessedView
import lab.dev.med.univ.feature.reporting.domain.models.DamumedLabReportKind
import lab.dev.med.univ.feature.reporting.domain.models.DamumedNormalizedReportPreview
import lab.dev.med.univ.feature.reporting.domain.models.DamumedOperationalOverview
import lab.dev.med.univ.feature.reporting.domain.models.DamumedParsedWorkbookPreview
import lab.dev.med.univ.feature.reporting.domain.models.DamumedReferralCountByMaterialProcessedView
import lab.dev.med.univ.feature.reporting.domain.models.DamumedReferralRegistrationSummary
import lab.dev.med.univ.feature.reporting.domain.models.DamumedWorkplaceCompletedStudiesProcessedView
import lab.dev.med.univ.feature.reporting.domain.models.DamumedWorkplaceDetailReport
import lab.dev.med.univ.feature.reporting.domain.usecases.GetDamumedEmployeeCompletedStudiesSummaryProcessedViewUseCase
import lab.dev.med.univ.feature.reporting.domain.usecases.GetDamumedNormalizedReportPreviewUseCase
import lab.dev.med.univ.feature.reporting.domain.usecases.GetDamumedOperationalOverviewUseCase
import lab.dev.med.univ.feature.reporting.domain.usecases.GetDamumedParsedWorkbookPreviewUseCase
import lab.dev.med.univ.feature.reporting.domain.usecases.GetDamumedReferralCountByMaterialProcessedViewUseCase
import lab.dev.med.univ.feature.reporting.domain.usecases.GetDamumedReferralRegistrationSummaryUseCase
import lab.dev.med.univ.feature.reporting.domain.usecases.GetDamumedReportSourceSettingsUseCase
import lab.dev.med.univ.feature.reporting.domain.usecases.GetDamumedReportUploadsUseCase
import lab.dev.med.univ.feature.reporting.domain.usecases.GetDamumedWorkplaceCompletedStudiesProcessedViewUseCase
import lab.dev.med.univ.feature.reporting.domain.usecases.GetDamumedWorkplaceDetailReportUseCase
import lab.dev.med.univ.feature.reporting.domain.usecases.NormalizeDamumedReportUploadUseCase
import lab.dev.med.univ.feature.reporting.domain.usecases.SynchronizeDamumedReportsUseCase
import lab.dev.med.univ.feature.reporting.domain.usecases.UpdateDamumedReportSourceModeUseCase
import lab.dev.med.univ.feature.reporting.domain.usecases.UploadManualDamumedReportUseCase
import lab.dev.med.univ.feature.reporting.presentation.dto.DamumedReportKindOptionDto
import lab.dev.med.univ.feature.reporting.presentation.dto.DamumedReportSourceSettingsResponseDto
import lab.dev.med.univ.feature.reporting.presentation.dto.DamumedReportUploadResponseDto
import lab.dev.med.univ.feature.reporting.presentation.dto.UpdateDamumedReportSourceModeDto
import lab.dev.med.univ.feature.reporting.presentation.dto.toOptionDto
import lab.dev.med.univ.feature.reporting.presentation.dto.toResponseDto
import org.slf4j.Logger
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.http.server.reactive.ServerHttpRequest
import org.springframework.http.codec.multipart.FilePart
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
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
@RequestMapping("/api/damumed-reports")
@Tag(name = "damumed-reports", description = "Manual and future API-based Damumed laboratory report ingestion")
@SecurityRequirement(name = "security_auth")
class DamumedReportIngestionController(
    logger: Logger,
    private val getOperationalOverviewUseCase: GetDamumedOperationalOverviewUseCase,
    private val getReferralRegistrationSummaryUseCase: GetDamumedReferralRegistrationSummaryUseCase,
    private val getEmployeeCompletedStudiesSummaryProcessedViewUseCase: GetDamumedEmployeeCompletedStudiesSummaryProcessedViewUseCase,
    private val getNormalizedReportPreviewUseCase: GetDamumedNormalizedReportPreviewUseCase,
    private val getParsedWorkbookPreviewUseCase: GetDamumedParsedWorkbookPreviewUseCase,
    private val getReferralCountByMaterialProcessedViewUseCase: GetDamumedReferralCountByMaterialProcessedViewUseCase,
    private val getWorkplaceProcessedViewUseCase: GetDamumedWorkplaceCompletedStudiesProcessedViewUseCase,
    private val getWorkplaceDetailReportUseCase: GetDamumedWorkplaceDetailReportUseCase,
    private val getSettingsUseCase: GetDamumedReportSourceSettingsUseCase,
    private val updateModeUseCase: UpdateDamumedReportSourceModeUseCase,
    private val uploadManualUseCase: UploadManualDamumedReportUseCase,
    private val getUploadsUseCase: GetDamumedReportUploadsUseCase,
    private val normalizeUploadUseCase: NormalizeDamumedReportUploadUseCase,
    private val synchronizeUseCase: SynchronizeDamumedReportsUseCase,
    private val userAggregateService: UserAggregateService,
) : Controller(logger) {

    @GetMapping("/operational-overview")
    suspend fun getOperationalOverview(
        @RequestParam(required = false, defaultValue = "false") refresh: Boolean,
        exchange: ServerWebExchange,
    ): ResponseEntity<DamumedOperationalOverview> {
        return try {
            if (refresh) {
                ensureAdmin(exchange)
            }
            ResponseEntity.ok(getOperationalOverviewUseCase(refresh))
        } catch (ex: Exception) {
            throw mapException(ex)
        }
    }

    @GetMapping("/referral-registration-summary")
    suspend fun getReferralRegistrationSummary(): ResponseEntity<DamumedReferralRegistrationSummary> {
        return try {
            ResponseEntity.ok(getReferralRegistrationSummaryUseCase())
        } catch (ex: Exception) {
            throw mapException(ex)
        }
    }

    @GetMapping("/workplace-detail-report")
    suspend fun getWorkplaceDetailReport(): ResponseEntity<DamumedWorkplaceDetailReport> {
        return try {
            ResponseEntity.ok(getWorkplaceDetailReportUseCase())
        } catch (ex: Exception) {
            throw mapException(ex)
        }
    }

    @GetMapping("/report-kinds")
    suspend fun getReportKinds(): ResponseEntity<List<DamumedReportKindOptionDto>> {
        return ResponseEntity.ok(
            DamumedLabReportKind.entries.map { it.toOptionDto() },
        )
    }

    @GetMapping("/source-mode")
    suspend fun getSourceMode(
        exchange: ServerWebExchange,
    ): ResponseEntity<DamumedReportSourceSettingsResponseDto> {
        return try {
            ensureAdmin(exchange)
            ResponseEntity.ok(getSettingsUseCase().toResponseDto())
        } catch (ex: Exception) {
            throw mapException(ex)
        }
    }

    @PutMapping("/source-mode")
    suspend fun updateSourceMode(
        @RequestBody dto: UpdateDamumedReportSourceModeDto,
        exchange: ServerWebExchange,
    ): ResponseEntity<DamumedReportSourceSettingsResponseDto> {
        return try {
            ensureAdmin(exchange)
            ResponseEntity.ok(updateModeUseCase(dto.mode).toResponseDto())
        } catch (ex: Exception) {
            throw mapException(ex)
        }
    }

    @GetMapping("/uploads")
    suspend fun getUploads(
        @RequestParam(required = false) reportKind: DamumedLabReportKind?,
    ): ResponseEntity<List<DamumedReportUploadResponseDto>> {
        return try {
            ResponseEntity.ok(getUploadsUseCase(reportKind).map { it.toResponseDto() })
        } catch (ex: Exception) {
            throw mapException(ex)
        }
    }

    @GetMapping("/uploads/{uploadId}/parsed-preview")
    suspend fun getParsedPreview(
        @PathVariable uploadId: String,
        @RequestParam(required = false, defaultValue = "25") maxRowsPerSheet: Int,
        exchange: ServerWebExchange,
    ): ResponseEntity<DamumedParsedWorkbookPreview> {
        return try {
            ensureAdmin(exchange)
            ResponseEntity.ok(getParsedWorkbookPreviewUseCase(uploadId, maxRowsPerSheet))
        } catch (ex: Exception) {
            throw mapException(ex)
        }
    }

    @GetMapping("/uploads/{uploadId}/normalized-preview")
    suspend fun getNormalizedPreview(
        @PathVariable uploadId: String,
        @RequestParam(required = false, defaultValue = "250") maxFacts: Int,
        exchange: ServerWebExchange,
    ): ResponseEntity<DamumedNormalizedReportPreview> {
        return try {
            ensureAdmin(exchange)
            ResponseEntity.ok(getNormalizedReportPreviewUseCase(uploadId, maxFacts))
        } catch (ex: Exception) {
            throw mapException(ex)
        }
    }

    @GetMapping("/uploads/{uploadId}/workplace-processed-view")
    suspend fun getWorkplaceProcessedView(
        @PathVariable uploadId: String,
        exchange: ServerWebExchange,
    ): ResponseEntity<DamumedWorkplaceCompletedStudiesProcessedView> {
        return try {
            ensureAdmin(exchange)
            ResponseEntity.ok(getWorkplaceProcessedViewUseCase(uploadId))
        } catch (ex: Exception) {
            throw mapException(ex)
        }
    }

    @GetMapping("/uploads/{uploadId}/referral-count-by-material-processed-view")
    suspend fun getReferralCountByMaterialProcessedView(
        @PathVariable uploadId: String,
        exchange: ServerWebExchange,
    ): ResponseEntity<DamumedReferralCountByMaterialProcessedView> {
        return try {
            ensureAdmin(exchange)
            ResponseEntity.ok(getReferralCountByMaterialProcessedViewUseCase(uploadId))
        } catch (ex: Exception) {
            throw mapException(ex)
        }
    }

    @GetMapping("/uploads/{uploadId}/employee-completed-studies-summary-processed-view")
    suspend fun getEmployeeCompletedStudiesSummaryProcessedView(
        @PathVariable uploadId: String,
        exchange: ServerWebExchange,
    ): ResponseEntity<DamumedEmployeeCompletedStudiesSummaryProcessedView> {
        return try {
            ensureAdmin(exchange)
            ResponseEntity.ok(getEmployeeCompletedStudiesSummaryProcessedViewUseCase(uploadId))
        } catch (ex: Exception) {
            throw mapException(ex)
        }
    }

    @PostMapping("/uploads/{uploadId}/normalize")
    suspend fun normalizeUpload(
        @PathVariable uploadId: String,
        exchange: ServerWebExchange,
    ): ResponseEntity<DamumedReportUploadResponseDto> {
        return try {
            ensureAdmin(exchange)
            ResponseEntity.ok(normalizeUploadUseCase(uploadId).toResponseDto())
        } catch (ex: Exception) {
            throw mapException(ex)
        }
    }

    @PostMapping(
        "/uploads/manual/{reportKind}",
        consumes = [MediaType.MULTIPART_FORM_DATA_VALUE],
    )
    suspend fun uploadManual(
        @PathVariable reportKind: DamumedLabReportKind,
        @RequestPart("file") file: FilePart,
        request: ServerHttpRequest,
        exchange: ServerWebExchange,
    ): ResponseEntity<DamumedReportUploadResponseDto> {
        return try {
            ensureAdmin(exchange)
            val upload = uploadManualUseCase(reportKind, file).toResponseDto()
            ResponseEntity.status(HttpStatus.CREATED)
                .header("Location", "${request.uri}/${upload.id}")
                .body(upload)
        } catch (ex: Exception) {
            throw mapException(ex)
        }
    }

    @PostMapping("/sync")
    suspend fun synchronize(exchange: ServerWebExchange): ResponseEntity<List<DamumedReportUploadResponseDto>> {
        return try {
            ensureAdmin(exchange)
            ResponseEntity.ok(synchronizeUseCase().map { it.toResponseDto() })
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
            is DamumedReportValidationException -> ResponseStatusException(HttpStatus.BAD_REQUEST, ex.message, ex)
            is DamumedReportSourceModeMismatchException -> ResponseStatusException(HttpStatus.CONFLICT, ex.message, ex)
            is DamumedApiIntegrationNotReadyException -> ResponseStatusException(HttpStatus.NOT_IMPLEMENTED, ex.message, ex)
            else -> {
                val (code, message) = getError(ex)
                ResponseStatusException(code, message, ex)
            }
        }
    }
}
