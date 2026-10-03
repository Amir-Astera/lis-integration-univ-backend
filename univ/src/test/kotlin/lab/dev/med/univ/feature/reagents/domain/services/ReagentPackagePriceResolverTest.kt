package lab.dev.med.univ.feature.reagents.domain.services

import lab.dev.med.univ.feature.reagents.domain.models.ReagentUnitType
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import java.math.BigDecimal

class ReagentPackagePriceResolverTest {

    @Test
    fun `converts known reagent kit price to millilitre price`() {
        val result = ReagentPackagePriceResolver.resolveUnitCost(
            packagePrice = 45_000.0,
            unitType = ReagentUnitType.ML,
            totalVolumeMl = 176.0,
            totalUnits = null,
        )

        assertEquals(0, BigDecimal("255.68181818").compareTo(requireNotNull(result)))
    }

    @Test
    fun `does not invent millilitre price when kit volume is absent`() {
        val result = ReagentPackagePriceResolver.resolveUnitCost(
            packagePrice = 45_000.0,
            unitType = ReagentUnitType.ML,
            totalVolumeMl = null,
            totalUnits = null,
        )

        assertNull(result)
    }

    @Test
    fun `converts a discrete package when test count is known`() {
        val result = ReagentPackagePriceResolver.resolveUnitCost(
            packagePrice = 10_000.0,
            unitType = ReagentUnitType.TEST,
            totalVolumeMl = null,
            totalUnits = 50,
        )

        assertEquals(0, BigDecimal("200.00000000").compareTo(requireNotNull(result)))
    }
}
