package lab.dev.med.univ.feature.reagents.presentation.rest

import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import kotlinx.coroutines.reactive.awaitFirst
import lab.dev.med.univ.feature.reagents.domain.errors.AnalyzerNotFoundException
import lab.dev.med.univ.feature.reagents.domain.errors.ReagentConsumptionReportNotFoundException
import lab.dev.med.univ.feature.reagents.domain.errors.ReagentModuleValidationException
import lab.dev.med.univ.feature.reagents.domain.usecases.CalculateDamumedReagentConsumptionUseCase
import lab.dev.med.univ.feature.reagents.domain.usecases.GenerateReagentConsumptionReportUseCase
import lab.dev.med.univ.feature.reagents.domain.usecases.GetDamumedReagentConsumptionUseCase
import lab.dev.med.univ.feature.reagents.domain.usecases.GetReagentConsumptionReportUseCase
import lab.dev.med.univ.feature.reagents.domain.usecases.GetReagentConsumptionReportsUseCase
import lab.dev.med.univ.feature.reagents.domain.usecases.RecalculateDamumedReagentConsumptionUseCase
import lab.dev.med.univ.feature.reagents.presentation.dto.CalculateDamumedConsumptionRequestDto
import lab.dev.med.univ.feature.reagents.presentation.dto.DamumedConsumptionCalculationResultDto
import lab.dev.med.univ.feature.reagents.presentation.dto.toDto
import lab.dev.med.univ.feature.reagents.presentation.dto.GenerateReagentConsumptionReportRequestDto
import lab.dev.med.univ.feature.reagents.presentation.dto.ReagentConsumptionReportResponseDto
import lab.dev.med.univ.feature.reagents.presentation.dto.toResponseDto
import org.slf4j.Logger
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.server.ResponseStatusException
import org.springframework.web.server.ServerWebExchange
import project.gigienist_reports.core.config.api.Controller
import project.gigienist_reports.core.security.SessionUser
import project.gigienist_reports.core.security.firebase.FirebaseSecurityUtils
import project.gigienist_reports.feature.users.domain.services.UserAggregateService

@RestController
@RequestMapping("/api/reagents/reports")
@Tag(name = "reagents-reports", description = "Saved reagent consumption reports API")
@SecurityRequirement(name = "security_auth")
class ReagentReportController(
    logger: Logger,
    private val generateReagentConsumptionReportUseCase: GenerateReagentConsumptionReportUseCase,
    private val getReagentConsumptionReportsUseCase: GetReagentConsumptionReportsUseCase,
    private val getReagentConsumptionReportUseCase: GetReagentConsumptionReportUseCase,
    private val calculateDamumedConsumption: CalculateDamumedReagentConsumptionUseCase,
    private val getDamumedConsumption: GetDamumedReagentConsumptionUseCase,
    private val recalculateDamumedConsumption: RecalculateDamumedReagentConsumptionUseCase,
    private val userAggregateService: UserAggregateService,
) : Controller(logger) {

    @PostMapping("/generate")
    suspend fun generateReport(
        @RequestBody dto: GenerateReagentConsumptionReportRequestDto,
        exchange: ServerWebExchange,
    ): ResponseEntity<ReagentConsumptionReportResponseDto> {
        return try {
            val actor = ensureAdmin(exchange)
            ResponseEntity.status(HttpStatus.CREATED).body(
                generateReagentConsumptionReportUseCase(
                    analyzerId = dto.analyzerId,
                    periodStart = dto.periodStart,
                    periodEnd = dto.periodEnd,
                    generatedBy = actor.login,
                ).toResponseDto(),
            )
        } catch (ex: Exception) {
            val (code, message) = getError(ex)
            throw ResponseStatusException(code, message, ex)
        }
    }

    @GetMapping
    suspend fun getReports(
        @RequestParam(required = false) analyzerId: String?,
    ): ResponseEntity<List<ReagentConsumptionReportResponseDto>> {
        return try {
            ResponseEntity.ok(getReagentConsumptionReportsUseCase(analyzerId).map { it.toResponseDto() })
        } catch (ex: Exception) {
            val (code, message) = getError(ex)
            throw ResponseStatusException(code, message, ex)
        }
    }

    @GetMapping("/{reportId}")
    suspend fun getReport(
        @PathVariable reportId: String,
    ): ResponseEntity<ReagentConsumptionReportResponseDto> {
        return try {
            ResponseEntity.ok(getReagentConsumptionReportUseCase(reportId).toResponseDto())
        } catch (ex: Exception) {
            val (code, message) = getError(ex)
            throw ResponseStatusException(code, message, ex)
        }
    }

    @PostMapping("/damumed-calculate")
    suspend fun calculateDamumedReportConsumption(
        @RequestBody dto: CalculateDamumedConsumptionRequestDto,
        exchange: ServerWebExchange,
    ): ResponseEntity<DamumedConsumptionCalculationResultDto> {
        return try {
            val actor = ensureAdmin(exchange)
            val result = calculateDamumedConsumption(
                lab.dev.med.univ.feature.reagents.domain.models.CalculateDamumedConsumptionRequest(
                    uploadId = dto.uploadId,
                    serviceCategoryFilter = dto.serviceCategoryFilter,
                    overrideAnalyzerMappings = dto.overrideAnalyzerMappings,
                )
            )
            ResponseEntity.ok(result.toDto())
        } catch (ex: Exception) {
            val (code, message) = getError(ex)
            throw ResponseStatusException(code, message, ex)
        }
    }

    @GetMapping("/damumed-consumption/{uploadId}")
    suspend fun getDamumedReportConsumption(
        @PathVariable uploadId: String,
    ): ResponseEntity<List<lab.dev.med.univ.feature.reagents.domain.models.DamumedReportReagentConsumption>> {
        return try {
            ResponseEntity.ok(getDamumedConsumption(uploadId))
        } catch (ex: Exception) {
            val (code, message) = getError(ex)
            throw ResponseStatusException(code, message, ex)
        }
    }

    @PostMapping("/damumed-recalculate/{uploadId}")
    suspend fun recalculateDamumedReportConsumption(
        @PathVariable uploadId: String,
        exchange: ServerWebExchange,
    ): ResponseEntity<DamumedConsumptionCalculationResultDto> {
        return try {
            ensureAdmin(exchange)
            val result = recalculateDamumedConsumption(uploadId)
            ResponseEntity.ok(result.toDto())
        } catch (ex: Exception) {
            val (code, message) = getError(ex)
            throw ResponseStatusException(code, message, ex)
        }
    }

    private suspend fun getSessionUser(exchange: ServerWebExchange): SessionUser {
        return FirebaseSecurityUtils.getUserFromRequest(exchange).awaitFirst()
    }

    private suspend fun ensureAdmin(exchange: ServerWebExchange): SessionUser {
        val user = getSessionUser(exchange)
        userAggregateService.checkAdminPrivilegesBySession(user)
        return user
    }

    private fun mapException(ex: Exception): ResponseStatusException {
        return when (ex) {
            is ReagentModuleValidationException -> ResponseStatusException(HttpStatus.BAD_REQUEST, ex.message, ex)
            is AnalyzerNotFoundException -> ResponseStatusException(HttpStatus.NOT_FOUND, ex.message, ex)
            is ReagentConsumptionReportNotFoundException -> ResponseStatusException(HttpStatus.NOT_FOUND, ex.message, ex)
            else -> {
                val (code, message) = getError(ex)
                ResponseStatusException(code, message, ex)
            }
        }
    }
}
