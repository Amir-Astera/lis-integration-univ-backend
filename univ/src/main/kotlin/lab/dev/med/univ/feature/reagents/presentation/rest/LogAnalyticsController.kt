package lab.dev.med.univ.feature.reagents.presentation.rest

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import kotlinx.coroutines.reactive.awaitFirst
import lab.dev.med.univ.feature.reagents.domain.models.AnalyticsPeriod
import lab.dev.med.univ.feature.reagents.domain.services.LogAnomalyAnalysisService
import lab.dev.med.univ.feature.reagents.presentation.dto.LogAnalyticsResultDto
import lab.dev.med.univ.feature.reagents.presentation.dto.toDto
import org.slf4j.Logger
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.server.ResponseStatusException
import org.springframework.web.server.ServerWebExchange
import project.gigienist_reports.core.config.api.Controller
import project.gigienist_reports.core.security.firebase.FirebaseSecurityUtils
import project.gigienist_reports.feature.users.domain.services.UserAggregateService
import java.time.LocalDate

@RestController
@RequestMapping("/api/reagents/log-analytics")
@Tag(name = "reagents-log-analytics", description = "Analyzer log anomaly analytics API")
@SecurityRequirement(name = "security_auth")
class LogAnalyticsController(
    logger: Logger,
    private val logAnomalyAnalysisService: LogAnomalyAnalysisService,
    private val userAggregateService: UserAggregateService,
) : Controller(logger) {

    @GetMapping
    @Operation(summary = "Get log anomaly analytics for a period")
    suspend fun getAnalytics(
        @RequestParam(required = false, defaultValue = "WEEK") period: AnalyticsPeriod,
        @RequestParam(required = false) analyzerId: String?,
        @RequestParam(required = false) referenceDate: LocalDate?,
        @RequestParam(required = false) dateFrom: LocalDate?,
        @RequestParam(required = false) dateTo: LocalDate?,
        exchange: ServerWebExchange,
    ): ResponseEntity<LogAnalyticsResultDto> {
        return try {
            val result = logAnomalyAnalysisService.getAnalytics(
                period = period,
                analyzerId = analyzerId,
                referenceDate = referenceDate ?: LocalDate.now(),
                dateFrom = dateFrom,
                dateTo = dateTo,
            )
            ResponseEntity.ok(result.toDto())
        } catch (ex: Exception) {
            val (code, message) = getError(ex)
            throw ResponseStatusException(code, message, ex)
        }
    }

    @PostMapping("/rebuild/{logUploadId}")
    @Operation(summary = "Rebuild anomaly records for a specific log upload (admin)")
    suspend fun rebuildForUpload(
        @PathVariable logUploadId: String,
        exchange: ServerWebExchange,
    ): ResponseEntity<Map<String, String>> {
        return try {
            ensureAdmin(exchange)
            logAnomalyAnalysisService.buildAnomaliesFromUpload(logUploadId)
            ResponseEntity.ok(mapOf("status" to "rebuilt", "logUploadId" to logUploadId))
        } catch (ex: Exception) {
            val (code, message) = getError(ex)
            throw ResponseStatusException(code, message, ex)
        }
    }

    private suspend fun ensureAdmin(exchange: ServerWebExchange) {
        val user = FirebaseSecurityUtils.getUserFromRequest(exchange).awaitFirst()
        userAggregateService.checkAdminPrivilegesBySession(user)
    }
}
