package lab.dev.med.univ.feature.reagents.domain.services

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import lab.dev.med.univ.feature.reporting.domain.events.DamumedReportNormalizedEvent
import lab.dev.med.univ.feature.reporting.domain.models.DamumedLabReportKind
import org.slf4j.LoggerFactory
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Component
import java.time.LocalDate

/**
 * Consumer of reporting-side events. When a Damumed report is normalized,
 * the reconciliation summary for the affected period is automatically rebuilt.
 *
 * The listener is decoupled from the reporting feature via Spring Application Events —
 * no direct dependency from reporting → reagents.
 *
 * Execution is launched on a coroutine scope backed by IO dispatcher to avoid
 * blocking the publisher thread. Errors are swallowed (logged) to prevent upload
 * failures from rolling back successful parsing.
 */
@Component
class ReconciliationEventListener(
    private val reconciliationSummaryService: ReconciliationSummaryService,
) {
    private val log = LoggerFactory.getLogger(javaClass)

    // Dedicated scope — SupervisorJob so one failed rebuild doesn't cancel the others.
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    @EventListener
    fun onDamumedReportNormalized(event: DamumedReportNormalizedEvent) {
        // Only journal-style reports affect reconciliation.
        // Operational dashboards (e.g. WORKPLACE_COMPLETED_STUDIES) are not the reconciliation source.
        if (event.reportKind != DamumedLabReportKind.COMPLETED_LAB_STUDIES_JOURNAL &&
            event.reportKind != DamumedLabReportKind.REFERRAL_REGISTRATION_JOURNAL
        ) {
            return
        }

        scope.launch {
            try {
                // Rebuild last 30 days — captures any newly-registered LIS data affecting recent logs.
                val to   = LocalDate.now()
                val from = to.minusDays(29)
                log.info(
                    "Auto-rebuild reconciliation after LIS upload={} (kind={}), period {} – {}",
                    event.uploadId, event.reportKind, from, to,
                )
                reconciliationSummaryService.rebuildForDateRange(from, to, null)
            } catch (ex: Exception) {
                log.error("Failed to auto-rebuild reconciliation after LIS upload={}", event.uploadId, ex)
            }
        }
    }
}
