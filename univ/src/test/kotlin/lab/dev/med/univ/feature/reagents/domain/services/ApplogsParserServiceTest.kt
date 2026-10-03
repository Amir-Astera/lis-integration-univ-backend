package lab.dev.med.univ.feature.reagents.domain.services

import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class ApplogsParserServiceTest {

    @Test
    fun `persists service identity carried by a Save string`() {
        val content = """
            01.09.2026 10:00:00	Information:	SAMPLE! 12345678
            01.09.2026 10:00:01	Information:	Sample request count: 1
            01.09.2026 10:00:02	Information:	Save string:
            01.09.2026 10:00:03	Information:	{"OrderResearchID":987,"AnalyzerId":24,"OrderResearch":{"OrderID":654,"ServiceMo":{"ServiceID":321,"Code":"B03.115.002","NameRU":"Определение С-реактивного белка"}}}
        """.trimIndent()

        val parser = ApplogsParserServiceImpl(ObjectMapper())
        val sample = parser.parse("upload-1", "mindray-bs-240", content).samples.single()

        assertTrue(sample.hasLisOrder)
        assertEquals(987, sample.orderResearchId)
        assertEquals(654, sample.orderId)
        assertEquals(321, sample.serviceId)
        assertEquals("B03.115.002", sample.serviceCode)
        assertEquals("Определение С-реактивного белка", sample.serviceName)
    }
}
