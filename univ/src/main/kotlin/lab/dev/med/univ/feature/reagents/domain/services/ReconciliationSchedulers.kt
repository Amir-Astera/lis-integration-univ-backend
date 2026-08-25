package lab.dev.med.univ.feature.reagents.domain.services

import kotlinx.coroutines.runBlocking
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

/**
 * Background maintenance for reconciliation state.
 *
 * Responsibilities:
 * - Promote sample_reconciliation rows whose grace window has expired (PENDING_GRACE → DISCREPANCY).
 *
 * Disabled by default in local/dev profiles: add `reconciliation.scheduler.enabled=true`
 * to application-*.properties to enable.
 */
@Component
@ConditionalOnProperty(prefix = "reconciliation.scheduler", name = ["enabled"], havingValue = "true", matchIfMissing = true)
class ReconciliationScheduler(
    private val reconciliationSummaryService: ReconciliationSummaryService,
) {
    private val log = LoggerFactory.getLogger(javaClass)

    /**
     * Runs every hour, on the hour. Promotes samples whose grace window has expired.
     *
     * Uses runBlocking because @Scheduled does not support suspend functions directly.
     * Given this runs on a dedicated scheduler thread (not a reactive event loop),
     * runBlocking is acceptable here.
     *
     * Batch size 1000 protects against memory pressure for large backlogs;
     * remaining samples are processed in the next tick.
     */
    @Scheduled(cron = "0 0 * * * *")  // every hour at :00
    fun promoteExpiredGraceSamples() {
        try {
            val promoted = runBlocking {
                reconciliationSummaryService.promoteExpiredGraceSamples(batchSize = 1000)
            }
            if (promoted > 0) {
                log.info("Scheduler: promoted {} expired-grace samples to DISCREPANCY", promoted)
            }
        } catch (ex: Exception) {
            log.error("Scheduler failure in promoteExpiredGraceSamples", ex)
        }
    }
}
