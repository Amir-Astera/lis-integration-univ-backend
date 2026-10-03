package lab.dev.med.univ.feature.reagents.domain.services

import lab.dev.med.univ.feature.reagents.domain.models.ReagentUnitType
import java.math.BigDecimal
import java.math.RoundingMode

/**
 * Converts a procurement package price into the price of the unit consumed by
 * a norm. It deliberately returns null when the package capacity is unknown:
 * treating a package price as an mL/test price produces false cost estimates.
 */
internal object ReagentPackagePriceResolver {
    fun resolveUnitCost(
        packagePrice: Double?,
        unitType: ReagentUnitType,
        totalVolumeMl: Double?,
        totalUnits: Int?,
    ): BigDecimal? {
        val price = packagePrice?.let(::BigDecimal) ?: return null
        return when (unitType) {
            ReagentUnitType.ML -> totalVolumeMl
                ?.takeIf { it > 0.0 }
                ?.let { price.divide(BigDecimal(it), SCALE, RoundingMode.HALF_UP) }

            else -> totalUnits
                ?.takeIf { it > 0 }
                ?.let { price.divide(BigDecimal(it), SCALE, RoundingMode.HALF_UP) }
        }
    }

    private const val SCALE = 8
}
