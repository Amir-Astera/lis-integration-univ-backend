package lab.dev.med.univ.feature.reagents.presentation.rest

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import kotlinx.coroutines.reactive.awaitFirst
import lab.dev.med.univ.feature.reagents.domain.models.SampleReconciliationStatus
import lab.dev.med.univ.feature.reagents.domain.services.ReconciliationSummaryService
import lab.dev.med.univ.feature.reagents.presentation.dto.DrillDownPageDto
import lab.dev.med.univ.feature.reagents.presentation.dto.ReconciliationAnalyzerSummaryDto
import lab.dev.med.univ.feature.reagents.presentation.dto.ReconciliationDailyPointDto
import lab.dev.med.univ.feature.reagents.presentation.dto.ReconciliationKpiSummaryDto
import lab.dev.med.univ.feature.reagents.presentation.dto.ReconciliationServiceRowDto
import lab.dev.med.univ.feature.reagents.presentation.dto.toDto
import org.slf4j.Logger
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
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

/**
 * Reconciliation dashboard API.
 *
 * Core business goal: detect reagent budget leakage by comparing what the analyzer
 * actually ran (APPLOGS) against what was registered in the LIS (Damumed journal).
 *
 * discrepancy = analyzer_ran - lis_registered — every unit here is money out the door
 * without a corresponding LIS record (unauthorized / unregistered test execution).
 */
@RestController
@RequestMapping("/api/reagents/reconciliation")
@Tag(name = "reagents-reconciliation", description = "LIS vs Analyzer log reconciliation dashboard API")
@SecurityRequirement(name = "security_auth")
class ReconciliationDashboardController(
    logger: Logger,
    private val reconciliationSummaryService: ReconciliationSummaryService,
    private val userAggregateService: UserAggregateService,
) : Controller(logger) {

    /**
     * Main KPI card summary: total discrepancy count, wasted cost, discrepancy rate.
     * Used for the top-level dashboard widgets.
     */
    @GetMapping("/summary")
    @Operation(summary = "Reconciliation KPI summary for a date range")
    suspend fun getSummary(
        @RequestParam(required = false) dateFrom: LocalDate?,
        @RequestParam(required = false) dateTo: LocalDate?,
        @RequestParam(required = false) analyzerId: String?,
        exchange: ServerWebExchange,
    ): ResponseEntity<ReconciliationKpiSummaryDto> {
        return try {
            val (from, to) = resolvePeriod(dateFrom, dateTo)
            val kpi = reconciliationSummaryService.getKpiSummary(from, to, analyzerId)
            ResponseEntity.ok(kpi.toDto())
        } catch (ex: Exception) {
            val (code, message) = getError(ex)
            throw ResponseStatusException(code, message, ex)
        }
    }

    /**
     * Daily time-series data for discrepancy trend chart.
     * Each point: logsCount, lisCount, discrepancyCount, wastedCostTenge.
     */
    @GetMapping("/timeline")
    @Operation(summary = "Daily reconciliation trend for chart rendering")
    suspend fun getTimeline(
        @RequestParam(required = false) dateFrom: LocalDate?,
        @RequestParam(required = false) dateTo: LocalDate?,
        @RequestParam(required = false) analyzerId: String?,
        exchange: ServerWebExchange,
    ): ResponseEntity<List<ReconciliationDailyPointDto>> {
        return try {
            val (from, to) = resolvePeriod(dateFrom, dateTo)
            val points = reconciliationSummaryService.getDailyTimeline(from, to, analyzerId)
                .map { it.toDto() }
            ResponseEntity.ok(points)
        } catch (ex: Exception) {
            val (code, message) = getError(ex)
            throw ResponseStatusException(code, message, ex)
        }
    }

    /**
     * Per-analyzer breakdown sorted by discrepancy count descending.
     * Used for the "top offenders" leaderboard widget.
     */
    @GetMapping("/by-analyzer")
    @Operation(summary = "Reconciliation breakdown by analyzer")
    suspend fun getByAnalyzer(
        @RequestParam(required = false) dateFrom: LocalDate?,
        @RequestParam(required = false) dateTo: LocalDate?,
        exchange: ServerWebExchange,
    ): ResponseEntity<List<ReconciliationAnalyzerSummaryDto>> {
        return try {
            val (from, to) = resolvePeriod(dateFrom, dateTo)
            val rows = reconciliationSummaryService.getByAnalyzer(from, to)
                .map { it.toDto() }
            ResponseEntity.ok(rows)
        } catch (ex: Exception) {
            val (code, message) = getError(ex)
            throw ResponseStatusException(code, message, ex)
        }
    }

    /**
     * Per-service comparison table (aggregated across all dates in range).
     * Paginated. Only rows with logsCount > 0 are returned.
     * sorted by discrepancyCount DESC.
     */
    @GetMapping("/by-service")
    @Operation(summary = "Reconciliation breakdown by service (paginated)")
    suspend fun getByService(
        @RequestParam(required = false) dateFrom: LocalDate?,
        @RequestParam(required = false) dateTo: LocalDate?,
        @RequestParam(required = false) analyzerId: String?,
        @RequestParam(required = false, defaultValue = "0") page: Int,
        @RequestParam(required = false, defaultValue = "50") size: Int,
        exchange: ServerWebExchange,
    ): ResponseEntity<List<ReconciliationServiceRowDto>> {
        return try {
            val (from, to) = resolvePeriod(dateFrom, dateTo)
            val effectiveSize = size.coerceIn(1, 200)
            val rows = reconciliationSummaryService.getByService(from, to, analyzerId, page, effectiveSize)
                .map { it.toDto() }
            ResponseEntity.ok(rows)
        } catch (ex: Exception) {
            val (code, message) = getError(ex)
            throw ResponseStatusException(code, message, ex)
        }
    }

    /**
     * Drill-down: list individual sample reconciliation records for a narrow slice.
     * Useful for "Show me the actual 47 samples that caused this discrepancy" UX.
     */
    @GetMapping("/drill-down")
    @Operation(summary = "Per-sample drill-down for investigation")
    suspend fun drillDown(
        @RequestParam(required = false) dateFrom: LocalDate?,
        @RequestParam(required = false) dateTo: LocalDate?,
        @RequestParam(required = false) catalogId: String?,
        @RequestParam(required = false) analyzerId: String?,
        @RequestParam(required = false) status: SampleReconciliationStatus?,
        @RequestParam(required = false, defaultValue = "0") page: Int,
        @RequestParam(required = false, defaultValue = "50") size: Int,
        exchange: ServerWebExchange,
    ): ResponseEntity<DrillDownPageDto> {
        return try {
            val (from, to) = resolvePeriod(dateFrom, dateTo)
            val effectiveSize = size.coerceIn(1, 500)
            val result = reconciliationSummaryService.getDrillDown(from, to, catalogId, analyzerId, status, page, effectiveSize)
            ResponseEntity.ok(result.toDto())
        } catch (ex: Exception) {
            val (code, message) = getError(ex)
            throw ResponseStatusException(code, message, ex)
        }
    }

    /**
     * Admin: force promotion of samples whose grace window has expired.
     * Also triggered hourly by the scheduler.
     */
    @PostMapping("/promote-grace")
    @Operation(summary = "Admin: promote expired-grace samples to DISCREPANCY")
    suspend fun promoteGrace(
        @RequestParam(required = false, defaultValue = "500") batchSize: Int,
        exchange: ServerWebExchange,
    ): ResponseEntity<Map<String, Any>> {
        return try {
            ensureAdmin(exchange)
            val promoted = reconciliationSummaryService.promoteExpiredGraceSamples(batchSize.coerceIn(1, 5000))
            ResponseEntity.ok(mapOf("promoted" to promoted))
        } catch (ex: Exception) {
            val (code, message) = getError(ex)
            throw ResponseStatusException(code, message, ex)
        }
    }

    /**
     * Admin: rebuild reconciliation summary for a date range.
     * Triggers a full recompute from raw samples + journal data.
     * Can be called after re-parsing logs or re-normalizing LIS uploads.
     */
    @PostMapping("/rebuild")
    @Operation(summary = "Admin: rebuild reconciliation summary for date range")
    suspend fun rebuild(
        @RequestParam(required = false) dateFrom: LocalDate?,
        @RequestParam(required = false) dateTo: LocalDate?,
        @RequestParam(required = false) analyzerId: String?,
        exchange: ServerWebExchange,
    ): ResponseEntity<Map<String, Any>> {
        return try {
            ensureAdmin(exchange)
            val (from, to) = resolvePeriod(dateFrom, dateTo)
            reconciliationSummaryService.rebuildForDateRange(from, to, analyzerId)
            ResponseEntity.ok(
                mapOf(
                    "status"     to "rebuilt",
                    "dateFrom"   to from.toString(),
                    "dateTo"     to to.toString(),
                    "analyzerId" to (analyzerId ?: "ALL"),
                )
            )
        } catch (ex: Exception) {
            val (code, message) = getError(ex)
            throw ResponseStatusException(code, message, ex)
        }
    }

    // ─── Helpers ──────────────────────────────────────────────────────────────

    private suspend fun ensureAdmin(exchange: ServerWebExchange) {
        val user = FirebaseSecurityUtils.getUserFromRequest(exchange).awaitFirst()
        userAggregateService.checkAdminPrivilegesBySession(user)
    }

    /** Default period: last 30 days ending today. */
    private fun resolvePeriod(from: LocalDate?, to: LocalDate?): Pair<LocalDate, LocalDate> {
        val effectiveTo   = to   ?: LocalDate.now()
        val effectiveFrom = from ?: effectiveTo.minusDays(29)
        return effectiveFrom to effectiveTo
    }
}
