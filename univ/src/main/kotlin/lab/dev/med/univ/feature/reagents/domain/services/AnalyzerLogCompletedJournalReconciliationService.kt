package lab.dev.med.univ.feature.reagents.domain.services

import lab.dev.med.univ.feature.reagents.domain.models.ParsedAnalyzerSample
import lab.dev.med.univ.feature.reagents.domain.models.SampleClassification
import lab.dev.med.univ.feature.reporting.data.repository.CompletedLabStudiesJournalReconciliationIndexLoader
import lab.dev.med.univ.feature.reporting.domain.JournalAxisTextNormalization
import lab.dev.med.univ.feature.reporting.domain.models.CompletedLabStudiesJournalReconciliationIndex
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

/**
 * Cross-checks anomalous applog samples against the normalized completed lab studies journal.
 *
 * Rules:
 * - Covers [SampleClassification.SUSPICIOUS], [SampleClassification.ERROR],
 *   [SampleClassification.XML_RESULT], and [SampleClassification.PROBABLE_RERUN].
 *   LEGITIMATE and WASH_TEST samples are left unchanged.
 * - A sample is upgraded to LEGITIMATE if **any** normalized key matches a journal referral:
 *   barcode, [ParsedAnalyzerSample.orderResearchId], or [ParsedAnalyzerSample.orderId].
 * - Service lines from the journal are **not** used to reject a match: if the referral/штрих-код
 *   is present in the journal, the test is considered registered — the analyzer anomaly is a
 *   false positive (LIS delayed registration is normal, especially for immunology).
 *
 * This service is intentionally permissive: it only upgrades, never downgrades.
 */
interface AnalyzerLogCompletedJournalReconciliationService {
    suspend fun reconcileApplogsSamples(samples: List<ParsedAnalyzerSample>): List<ParsedAnalyzerSample>
}

@Service
internal class AnalyzerLogCompletedJournalReconciliationServiceImpl(
    private val journalIndexLoader: CompletedLabStudiesJournalReconciliationIndexLoader,
) : AnalyzerLogCompletedJournalReconciliationService {

    private val log = LoggerFactory.getLogger(javaClass)

    override suspend fun reconcileApplogsSamples(samples: List<ParsedAnalyzerSample>): List<ParsedAnalyzerSample> {
        val index = runCatching { journalIndexLoader.loadReconciliationIndex() }
            .onFailure { ex ->
                log.warn(
                    "Could not load completed lab studies journal index for applog reconciliation: {}",
                    ex.message,
                )
            }
            .getOrDefault(CompletedLabStudiesJournalReconciliationIndex.empty())

        if (index.referralKeys.isEmpty()) {
            return samples
        }

        val reconciled = samples.map { reconcileOne(it, index) }
        val upgraded = samples.indices.count { i ->
            samples[i].classification in RECONCILABLE_CLASSIFICATIONS &&
                reconciled[i].classification == SampleClassification.LEGITIMATE
        }
        if (upgraded > 0) {
            log.info("Reclassified {} applog sample(s) as LEGITIMATE via completed lab journal index", upgraded)
        }
        return reconciled
    }

    private fun reconcileOne(
        sample: ParsedAnalyzerSample,
        index: CompletedLabStudiesJournalReconciliationIndex,
    ): ParsedAnalyzerSample {
        // Only upgrade samples that are in anomalous classifications
        if (sample.classification !in RECONCILABLE_CLASSIFICATIONS) {
            return sample
        }

        val referralCandidates = referralKeyCandidates(sample)
        referralCandidates.firstOrNull { it in index.referralKeys } ?: return sample

        val originalClassification = sample.classification.name
        val previousReason = sample.classificationReason.orEmpty().trim()
        val suffix = if (previousReason.isNotEmpty()) " [$originalClassification: $previousReason]" else " [$originalClassification]"
        val reason = "Подтверждено журналом выполненных исследований (№ направления / штрих-код)$suffix"

        return sample.copy(
            classification = SampleClassification.LEGITIMATE,
            hasLisOrder = true,
            classificationReason = reason,
        )
    }

    companion object {
        /**
         * Classifications that can be upgraded to LEGITIMATE if a matching referral is found.
         * LEGITIMATE and WASH_TEST are not touched — the former is already resolved,
         * the latter is a technical run (wash, blank, QC) that is never billed.
         */
        val RECONCILABLE_CLASSIFICATIONS = setOf(
            SampleClassification.SUSPICIOUS,
            SampleClassification.ERROR,
            SampleClassification.XML_RESULT,
            SampleClassification.PROBABLE_RERUN,
        )
    }

    private fun referralKeyCandidates(sample: ParsedAnalyzerSample): List<String> =
        buildList {
            add(JournalAxisTextNormalization.normalizeReferral(sample.barcode))
            sample.orderResearchId?.let {
                add(JournalAxisTextNormalization.normalizeReferral(it.toString()))
            }
            sample.orderId?.let {
                add(JournalAxisTextNormalization.normalizeReferral(it.toString()))
            }
        }.distinct().filter { it.isNotBlank() }
}
