package lab.dev.med.univ.feature.reagents.domain.services

import lab.dev.med.univ.feature.reagents.domain.models.ParsedAnalyzerSample
import lab.dev.med.univ.feature.reagents.domain.models.SampleClassification
import lab.dev.med.univ.feature.reporting.data.repository.CompletedLabStudiesJournalReconciliationIndexLoader
import lab.dev.med.univ.feature.reporting.domain.JournalAxisTextNormalization
import lab.dev.med.univ.feature.reporting.domain.models.CompletedLabStudiesJournalReconciliationIndex
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

/**
 * Cross-checks suspicious applog samples against the normalized completed lab studies journal.
 *
 * Rules:
 * - Only [SampleClassification.SUSPICIOUS] samples are changed.
 * - A sample is upgraded if **any** normalized key matches a journal referral: barcode,
 *   [ParsedAnalyzerSample.orderResearchId], or [ParsedAnalyzerSample.orderId].
 * - Service lines from the journal are **not** used to reject a match: if the referral/штрих-код is in the journal,
 *   the sample is treated as legitimate (same direction as «THERE IS NO SR» false positives when LIS row exists).
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
            samples[i].classification == SampleClassification.SUSPICIOUS &&
                reconciled[i].classification == SampleClassification.LEGITIMATE
        }
        if (upgraded > 0) {
            log.debug("Reclassified {} applog sample(s) as legitimate via completed lab journal index", upgraded)
        }
        return reconciled
    }

    private fun reconcileOne(
        sample: ParsedAnalyzerSample,
        index: CompletedLabStudiesJournalReconciliationIndex,
    ): ParsedAnalyzerSample {
        if (sample.classification != SampleClassification.SUSPICIOUS) {
            return sample
        }

        val referralCandidates = referralKeyCandidates(sample)
        referralCandidates.firstOrNull { it in index.referralKeys } ?: return sample

        val previousReason = sample.classificationReason.orEmpty().trim()
        val suffix = if (previousReason.isNotEmpty()) " ($previousReason)" else ""
        val reason = "Подтверждено журналом выполненных исследований (№ направления / штрих-код)$suffix"

        return sample.copy(
            classification = SampleClassification.LEGITIMATE,
            hasLisOrder = true,
            classificationReason = reason,
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
