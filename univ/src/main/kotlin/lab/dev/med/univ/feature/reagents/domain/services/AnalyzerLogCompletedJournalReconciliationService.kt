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
 * - Referral keys: normalized barcode, [ParsedAnalyzerSample.orderResearchId], [ParsedAnalyzerSample.orderId].
 * - If the journal has **no** non-blank service names for that referral → referral match is sufficient.
 * - If the journal has named services and the log has **no** [ParsedAnalyzerSample.serviceName] → referral match is sufficient
 *   (SR / metadata gap on the instrument side).
 * - If both journal and log carry a service hint → [CompletedLabJournalServiceLineMatching.likelySame] must succeed
 *   for at least one journal line (substring, shared service code, or token overlap).
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
        val matchedReferral = referralCandidates.firstOrNull { it in index.referralKeys } ?: return sample

        val namedJournalServices = index.namedServicesForReferral(matchedReferral)
        val logServiceHint = sample.serviceName?.let(JournalAxisTextNormalization::normalizeLogServiceHint).orEmpty()

        val serviceOk = when {
            namedJournalServices.isEmpty() -> true
            logServiceHint.isBlank() -> true
            else ->
                namedJournalServices.any { journalLine ->
                    journalLine.isNotBlank() &&
                        CompletedLabJournalServiceLineMatching.likelySame(journalLine, logServiceHint)
                }
        }

        if (!serviceOk) {
            return sample
        }

        val previousReason = sample.classificationReason.orEmpty().trim()
        val suffix = if (previousReason.isNotEmpty()) " ($previousReason)" else ""
        val reason = when {
            logServiceHint.isNotBlank() && namedJournalServices.any { it.isNotBlank() } ->
                "Подтверждено журналом выполненных исследований (направление и услуга)$suffix"
            else ->
                "Подтверждено журналом выполненных исследований (направление)$suffix"
        }

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
