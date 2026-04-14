package lab.dev.med.univ.feature.reagents.domain.services

import kotlinx.coroutines.runBlocking
import lab.dev.med.univ.feature.reagents.domain.models.ParsedAnalyzerSample
import lab.dev.med.univ.feature.reagents.domain.models.SampleClassification
import lab.dev.med.univ.feature.reporting.data.repository.CompletedLabStudiesJournalReconciliationIndexLoader
import lab.dev.med.univ.feature.reporting.domain.JournalAxisTextNormalization
import lab.dev.med.univ.feature.reporting.domain.models.CompletedLabStudiesJournalReconciliationIndex
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.time.LocalDateTime

class AnalyzerLogCompletedJournalReconciliationServiceTest {

    private fun ns(s: String) = JournalAxisTextNormalization.normalizeServiceLine(s)

    private fun loaderWith(index: CompletedLabStudiesJournalReconciliationIndex) =
        object : CompletedLabStudiesJournalReconciliationIndexLoader {
            override suspend fun loadReconciliationIndex(): CompletedLabStudiesJournalReconciliationIndex = index
        }

    private fun suspicious(
        barcode: String = "22244695",
        serviceName: String? = null,
        orderResearchId: Long? = null,
    ) = ParsedAnalyzerSample(
        id = "s1",
        logUploadId = "u1",
        sampleTimestamp = LocalDateTime.of(2026, 4, 13, 11, 11),
        barcode = barcode,
        hasLisOrder = false,
        classification = SampleClassification.SUSPICIOUS,
        classificationReason = "THERE IS NO SR FOR Barcode $barcode",
        serviceName = serviceName,
        orderResearchId = orderResearchId,
    )

    @Test
    fun `referral in journal without named services becomes legitimate`() = runBlocking {
        val index = CompletedLabStudiesJournalReconciliationIndex(
            mapOf("22244695" to setOf("")),
        )
        val service = AnalyzerLogCompletedJournalReconciliationServiceImpl(loaderWith(index))
        val out = service.reconcileApplogsSamples(listOf(suspicious()))
        assertEquals(SampleClassification.LEGITIMATE, out.single().classification)
        assertEquals(true, out.single().hasLisOrder)
    }

    @Test
    fun `referral with journal services but no log service name becomes legitimate`() = runBlocking {
        val index = CompletedLabStudiesJournalReconciliationIndex(
            mapOf(
                "22244695" to setOf(
                    ns("B04.149.002, АЧТВ"),
                    ns("B04.436.002, РФМК"),
                ),
            ),
        )
        val service = AnalyzerLogCompletedJournalReconciliationServiceImpl(loaderWith(index))
        val out = service.reconcileApplogsSamples(listOf(suspicious(serviceName = null)))
        assertEquals(SampleClassification.LEGITIMATE, out.single().classification)
    }

    @Test
    fun `referral and log service hint must align when both present`() = runBlocking {
        val index = CompletedLabStudiesJournalReconciliationIndex(
            mapOf(
                "22244695" to setOf(
                    ns("B04.149.002, Определение АЧТВ"),
                    ns("B04.436.002, Определение РФМК"),
                ),
            ),
        )
        val service = AnalyzerLogCompletedJournalReconciliationServiceImpl(loaderWith(index))
        val ok = service.reconcileApplogsSamples(listOf(suspicious(serviceName = "B04.149.002")))
        assertEquals(SampleClassification.LEGITIMATE, ok.single().classification)

        val bad = service.reconcileApplogsSamples(listOf(suspicious(serviceName = "B04.999.999")))
        assertEquals(SampleClassification.SUSPICIOUS, bad.single().classification)
    }

    @Test
    fun `matches orderResearchId when barcode differs`() = runBlocking {
        val index = CompletedLabStudiesJournalReconciliationIndex(
            mapOf("22244695" to setOf(ns("B04.149.002"))),
        )
        val service = AnalyzerLogCompletedJournalReconciliationServiceImpl(loaderWith(index))
        val out = service.reconcileApplogsSamples(
            listOf(
                suspicious(barcode = "other", orderResearchId = 22244695L, serviceName = null),
            ),
        )
        assertEquals(SampleClassification.LEGITIMATE, out.single().classification)
    }

    @Test
    fun `non suspicious unchanged`() = runBlocking {
        val index = CompletedLabStudiesJournalReconciliationIndex(mapOf("22244695" to setOf(ns("x"))))
        val service = AnalyzerLogCompletedJournalReconciliationServiceImpl(loaderWith(index))
        val legit = suspicious().copy(classification = SampleClassification.LEGITIMATE, hasLisOrder = true)
        val out = service.reconcileApplogsSamples(listOf(legit))
        assertEquals(SampleClassification.LEGITIMATE, out.single().classification)
    }
}
