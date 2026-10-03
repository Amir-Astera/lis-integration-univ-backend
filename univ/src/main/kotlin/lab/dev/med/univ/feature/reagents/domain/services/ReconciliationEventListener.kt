package lab.dev.med.univ.feature.reagents.domain.services

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import lab.dev.med.univ.feature.reporting.data.repository.CompletedLabStudiesJournalReferralIndexRepository
import lab.dev.med.univ.feature.reporting.domain.events.DamumedReportNormalizedEvent
import lab.dev.med.univ.feature.reporting.domain.models.DamumedLabReportKind
import org.slf4j.LoggerFactory
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Component

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
    private val completedJournalIndexRepository: CompletedLabStudiesJournalReferralIndexRepository,
) {
    private val log = LoggerFactory.getLogger(javaClass)

    // Dedicated scope — SupervisorJob so one failed rebuild doesn't cancel the others.
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    @EventListener
    fun onDamumedReportNormalized(event: DamumedReportNormalizedEvent) {
        // Only the completed-studies journal provides the dated Damumed facts
        // used by reconciliation. A referral-registration upload is useful for
        // operations, but it must not claim coverage for analyzer events.
        if (event.reportKind != DamumedLabReportKind.COMPLETED_LAB_STUDIES_JOURNAL) {
            return
        }

        scope.launch {
            try {
                val bounds = completedJournalIndexRepository.loadDatedCoverageBounds(event.uploadId)
                if (bounds == null) {
                    log.info(
                        "Completed journal upload={} has no valid completed_at dates; reconciliation rebuild skipped",
                        event.uploadId,
                    )
                    return@launch
                }
                log.info(
                    "Auto-rebuild reconciliation after LIS upload={} (kind={}), period {} – {}",
                    event.uploadId, event.reportKind, bounds.from, bounds.to,
                )
                reconciliationSummaryService.rebuildForDateRange(bounds.from, bounds.to, null)
            } catch (ex: Exception) {
                log.error("Failed to auto-rebuild reconciliation after LIS upload={}", event.uploadId, ex)
            }
        }
    }
}
