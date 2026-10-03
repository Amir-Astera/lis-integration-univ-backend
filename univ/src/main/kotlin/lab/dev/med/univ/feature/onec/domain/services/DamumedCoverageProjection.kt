package lab.dev.med.univ.feature.onec.domain.services

import java.math.BigDecimal
import java.math.RoundingMode

/**
 * Pure arithmetic for projecting observed Damumed consumption to a planning
 * horizon. Input must already be in the 1C mapping unit (for example PACK).
 */
internal object DamumedCoverageProjection {
    fun averageDaily(observedQuantity: BigDecimal?, observationDays: Int?): BigDecimal? {
        if (observedQuantity == null || observationDays == null || observationDays <= 0) return null
        return observedQuantity.divide(BigDecimal(observationDays), SCALE, RoundingMode.HALF_UP)
    }

    fun projectedQuantity(averageDaily: BigDecimal?, planningDays: Int): BigDecimal? {
        if (averageDaily == null || planningDays <= 0) return null
        return averageDaily.multiply(BigDecimal(planningDays))
    }

    fun coverageDays(quantityOnHand: BigDecimal?, averageDaily: BigDecimal?): BigDecimal? {
        if (quantityOnHand == null || averageDaily == null || averageDaily <= BigDecimal.ZERO) return null
        return quantityOnHand.divide(averageDaily, 2, RoundingMode.HALF_UP)
    }

    private const val SCALE = 6
}
