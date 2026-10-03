package lab.dev.med.univ.feature.onec.domain.services

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import java.math.BigDecimal

class DamumedCoverageProjectionTest {

    @Test
    fun `projects confirmed package consumption from actual observation period`() {
        val daily = DamumedCoverageProjection.averageDaily(BigDecimal("5.910685"), observationDays = 46)
        val projected = DamumedCoverageProjection.projectedQuantity(daily, planningDays = 30)
        val coverage = DamumedCoverageProjection.coverageDays(BigDecimal("13"), daily)

        assertEquals(0, BigDecimal("0.128493").compareTo(requireNotNull(daily)))
        assertEquals(0, BigDecimal("3.854790").compareTo(requireNotNull(projected)))
        assertEquals(0, BigDecimal("101.17").compareTo(requireNotNull(coverage)))
    }

    @Test
    fun `does not create coverage without a valid consumption rate`() {
        assertNull(DamumedCoverageProjection.averageDaily(BigDecimal.ONE, observationDays = 0))
        assertNull(DamumedCoverageProjection.coverageDays(BigDecimal("13"), BigDecimal.ZERO))
    }
}
