package lab.dev.med.univ.feature.reagents.domain.services

import lab.dev.med.univ.feature.reporting.domain.JournalAxisTextNormalization
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class CompletedLabJournalServiceLineMatchingTest {

    private fun j(s: String) = JournalAxisTextNormalization.normalizeServiceLine(s)
    private fun l(s: String) = JournalAxisTextNormalization.normalizeLogServiceHint(s)

    @Test
    fun `matches identical NMU code`() {
        assertTrue(
            CompletedLabJournalServiceLineMatching.likelySame(
                j("B04.149.002, Определение активированного частичного тромбопластинового времени (АЧТВ)"),
                l("B04.149.002"),
            ),
        )
    }

    @Test
    fun `matches short analyzer hint contained in journal line`() {
        assertTrue(
            CompletedLabJournalServiceLineMatching.likelySame(
                j("B04.149.002, Определение … (АЧТВ) в плазме крови"),
                l("АЧТВ (анализатор): 24.31 сек"),
            ),
        )
    }

    @Test
    fun `does not match unrelated service on same referral family`() {
        assertFalse(
            CompletedLabJournalServiceLineMatching.likelySame(
                j("B04.149.002, Определение АЧТВ"),
                l("B04.436.002, РФМК"),
            ),
        )
    }

    @Test
    fun `does not match unrelated short tokens`() {
        assertFalse(
            CompletedLabJournalServiceLineMatching.likelySame(
                j("B04.487.002, Определение тромбинового времени (ТВ)"),
                l("гемоглобин"),
            ),
        )
    }
}
