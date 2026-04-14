package lab.dev.med.univ.feature.reporting.domain.models

/**
 * In-memory view of normalized Damumed «Журнал регистрации выполненных лабораторных исследований» rows
 * for applog reconciliation.
 *
 * [referralToJournalServices] keys are normalized referral numbers (№ направления);
 * values are normalized service lines — one entry per journal row (same referral may appear many times).
 */
data class CompletedLabStudiesJournalReconciliationIndex(
    val referralToJournalServices: Map<String, Set<String>>,
) {
    val referralKeys: Set<String> get() = referralToJournalServices.keys

    fun namedServicesForReferral(referralKey: String): Set<String> =
        referralToJournalServices[referralKey].orEmpty().filter { it.isNotBlank() }.toSet()

    companion object {
        fun empty(): CompletedLabStudiesJournalReconciliationIndex =
            CompletedLabStudiesJournalReconciliationIndex(emptyMap())
    }
}
