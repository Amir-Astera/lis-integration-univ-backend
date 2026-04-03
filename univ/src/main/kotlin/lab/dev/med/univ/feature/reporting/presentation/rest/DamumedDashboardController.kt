package lab.dev.med.univ.feature.reporting.presentation.rest

import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import kotlinx.coroutines.reactive.awaitFirst
import lab.dev.med.univ.feature.reporting.domain.errors.DamumedApiIntegrationNotReadyException
import lab.dev.med.univ.feature.reporting.domain.errors.DamumedReportSourceModeMismatchException
import lab.dev.med.univ.feature.reporting.domain.errors.DamumedReportValidationException
import lab.dev.med.univ.feature.reporting.domain.models.DamumedOperationalDailyStat
import lab.dev.med.univ.feature.reporting.domain.models.DamumedOperationalDashboardPeriodSummary
import lab.dev.med.univ.feature.reporting.domain.models.DamumedOperationalDashboardSummary
import lab.dev.med.univ.feature.reporting.domain.models.DamumedOperationalStockItem
import lab.dev.med.univ.feature.reporting.domain.models.DamumedOperationalStatusItem
import lab.dev.med.univ.feature.reporting.domain.models.DamumedOperationalTatItem
import lab.dev.med.univ.feature.reporting.domain.usecases.GetDamumedOperationalOverviewUseCase
import org.slf4j.Logger
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.server.ResponseStatusException
import org.springframework.web.server.ServerWebExchange
import project.gigienist_reports.core.config.api.Controller
import project.gigienist_reports.core.security.firebase.FirebaseSecurityUtils
import project.gigienist_reports.feature.users.domain.services.UserAggregateService

@RestController
@RequestMapping("/api/damumed-dashboard")
@Tag(name = "damumed-dashboard", description = "Optimized dashboard endpoints for better performance")
@SecurityRequirement(name = "security_auth")
class DamumedDashboardController(
    logger: Logger,
    private val getOperationalOverviewUseCase: GetDamumedOperationalOverviewUseCase,
    private val userAggregateService: UserAggregateService,
) : Controller(logger) {

    /**
     * Single unified endpoint for the full dashboard — returns ALL periods plus global metrics.
     * The frontend can pick the right period data without extra requests.
     * Uses the same in-memory snapshot cache so response is fast (<50ms when cached).
     */
    @GetMapping("/full")
    suspend fun getFull(
        @RequestParam(required = false, defaultValue = "false") refresh: Boolean,
        exchange: ServerWebExchange,
    ): ResponseEntity<Map<String, Any?>> {
        return try {
            val overview = getOperationalOverviewUseCase(refresh)
            val d = overview.dashboard
            ResponseEntity.ok(mapOf(
                "generatedAt" to overview.generatedAt,
                "sourceReportName" to overview.sourceReportName,
                "uploads" to overview.uploads,
                "day" to periodPayload(d.day),
                "week" to periodPayload(d.week),
                "month" to periodPayload(d.month),
                "criticalSamples" to d.criticalSamples,
                "samplesTotal" to d.samplesTotal,
                "validationQueue" to d.validationQueue,
                "analyzerLoadPercent" to d.analyzerLoadPercent,
                "departmentLoads" to d.departmentLoads,
                "analyzerStatuses" to d.analyzerStatuses,
                "stockAlerts" to d.stockAlerts,
                "workplaces" to d.workplaceItems,
                "dailyStats" to d.month.dailyStats,
                "reports" to overview.reports,
                "worklists" to mapOf(
                    "waitingSheets" to overview.worklists.waitingSheets,
                    "completedSheets" to overview.worklists.completedSheets,
                    "activeSheets" to overview.worklists.activeSheets,
                ),
            ))
        } catch (ex: Exception) {
            val (code, message) = getError(ex)
            throw ResponseStatusException(code, message, ex)
        }
    }

    private fun periodPayload(p: DamumedOperationalDashboardPeriodSummary) = mapOf(
        "label" to p.label,
        "researchCount" to p.researchCount,
        "patientCount" to p.patientCount,
        "departmentCount" to p.departmentCount,
        "sentResultsCount" to p.sentResultsCount,
        "materialsCount" to p.materialsCount,
        "serviceCostTotal" to p.serviceCostTotal,
        "tatByService" to p.tatByService,
        "workplaceItems" to p.workplaceItems,
        "dailyStats" to p.dailyStats,
    )

    @GetMapping("/kpi")
    suspend fun getKpi(
        @RequestParam(required = false, defaultValue = "month") period: String,
        exchange: ServerWebExchange,
    ): ResponseEntity<Map<String, Any>> {
        return try {
            val overview = getOperationalOverviewUseCase(false)
            val selectedSummary = when (period) {
                "day" -> overview.dashboard.day
                "week" -> overview.dashboard.week  
                else -> overview.dashboard.month
            }
            
            ResponseEntity.ok(mapOf(
                "researchCount" to selectedSummary.researchCount,
                "patientCount" to selectedSummary.patientCount,
                "departmentCount" to selectedSummary.departmentCount,
                "sentResultsCount" to selectedSummary.sentResultsCount,
                "materialsCount" to selectedSummary.materialsCount,
                "serviceCostTotal" to selectedSummary.serviceCostTotal,
                "criticalSamples" to overview.dashboard.criticalSamples,
                "samplesTotal" to overview.dashboard.samplesTotal,
                "validationQueue" to overview.dashboard.validationQueue,
                "analyzerLoadPercent" to overview.dashboard.analyzerLoadPercent
            ))
        } catch (ex: Exception) {
            val (code, message) = getError(ex)
            throw ResponseStatusException(code, message, ex)
        }
    }

    @GetMapping("/tat")
    suspend fun getTatAnalytics(
        @RequestParam(required = false, defaultValue = "month") period: String,
        exchange: ServerWebExchange,
    ): ResponseEntity<List<DamumedOperationalTatItem>> {
        return try {
            val overview = getOperationalOverviewUseCase(false)
            val selectedSummary = when (period) {
                "day" -> overview.dashboard.day
                "week" -> overview.dashboard.week
                else -> overview.dashboard.month
            }
            ResponseEntity.ok(selectedSummary.tatByService)
        } catch (ex: Exception) {
            val (code, message) = getError(ex)
            throw ResponseStatusException(code, message, ex)
        }
    }

    @GetMapping("/workplaces")
    suspend fun getWorkplaces(
        @RequestParam(required = false, defaultValue = "month") period: String,
        exchange: ServerWebExchange,
    ): ResponseEntity<List<DamumedOperationalStatusItem>> {
        return try {
            val overview = getOperationalOverviewUseCase(false)
            val selectedSummary = when (period) {
                "day" -> overview.dashboard.day
                "week" -> overview.dashboard.week
                else -> overview.dashboard.month
            }
            ResponseEntity.ok(selectedSummary.workplaceItems)
        } catch (ex: Exception) {
            val (code, message) = getError(ex)
            throw ResponseStatusException(code, message, ex)
        }
    }

    @GetMapping("/analyzers")
    suspend fun getAnalyzerStatuses(exchange: ServerWebExchange): ResponseEntity<List<DamumedOperationalStatusItem>> {
        return try {
            val overview = getOperationalOverviewUseCase(false)
            ResponseEntity.ok(overview.dashboard.analyzerStatuses)
        } catch (ex: Exception) {
            val (code, message) = getError(ex)
            throw ResponseStatusException(code, message, ex)
        }
    }

    @GetMapping("/warehouse")
    suspend fun getWarehouseAlerts(exchange: ServerWebExchange): ResponseEntity<List<DamumedOperationalStockItem>> {
        return try {
            val overview = getOperationalOverviewUseCase(false)
            ResponseEntity.ok(overview.dashboard.stockAlerts)
        } catch (ex: Exception) {
            val (code, message) = getError(ex)
            throw ResponseStatusException(code, message, ex)
        }
    }

    @GetMapping("/daily-stats")
    suspend fun getDailyStats(exchange: ServerWebExchange): ResponseEntity<List<DamumedOperationalDailyStat>> {
        return try {
            val overview = getOperationalOverviewUseCase(false)
            ResponseEntity.ok(overview.dashboard.month.dailyStats)
        } catch (ex: Exception) {
            val (code, message) = getError(ex)
            throw ResponseStatusException(code, message, ex)
        }
    }

    @GetMapping("/department-loads")
    suspend fun getDepartmentLoads(exchange: ServerWebExchange): ResponseEntity<Map<String, Any>> {
        return try {
            val overview = getOperationalOverviewUseCase(false)
            ResponseEntity.ok(mapOf(
                "loads" to overview.dashboard.departmentLoads,
                "analyzerLoadPercent" to overview.dashboard.analyzerLoadPercent
            ))
        } catch (ex: Exception) {
            val (code, message) = getError(ex)
            throw ResponseStatusException(code, message, ex)
        }
    }

    private suspend fun getSessionUser(exchange: ServerWebExchange) = 
        FirebaseSecurityUtils.getUserFromRequest(exchange).awaitFirst()

    private suspend fun ensureAdmin(exchange: ServerWebExchange) {
        userAggregateService.checkAdminPrivilegesBySession(getSessionUser(exchange))
    }
}
