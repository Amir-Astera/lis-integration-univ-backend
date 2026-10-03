package lab.dev.med.univ.feature.onec.presentation.dto

import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime

data class OnecLimsItemMappingDto(
    val id: String,
    val snapshotId: String,
    val onecItemRef: String?,
    val onecItemCode: String?,
    val onecItemName: String,
    val targetKind: String,
    val targetName: String,
    val targetUnit: String,
    val conversionFactor: BigDecimal,
    val mappingStatus: String,
    val notes: String?,
    val updatedAt: LocalDateTime,
)

data class UpsertOnecLimsItemMappingRequestDto(
    val snapshotId: String,
    val onecItemRef: String? = null,
    val onecItemCode: String? = null,
    val onecItemName: String,
    val targetKind: String,
    val targetName: String,
    val targetUnit: String,
    val conversionFactor: BigDecimal = BigDecimal.ONE,
    val mappingStatus: String = "SUGGESTED",
    val notes: String? = null,
)

data class OnecStockCoverageDto(
    val snapshotId: String,
    val sourceActualityAt: LocalDateTime?,
    val planningDays: Int,
    val matchedDamumedUploadId: String?,
    val damumedPeriodStart: LocalDate? = null,
    val damumedPeriodEnd: LocalDate? = null,
    val damumedObservationDays: Int? = null,
    val rows: List<OnecStockCoverageRowDto>,
    val unmappedInventoryLines: Int,
    val unmappedDamumedReagents: List<String>,
)

data class OnecStockCoverageRowDto(
    val onecItemRef: String?,
    val onecItemCode: String?,
    val onecItemName: String,
    val warehouse: String?,
    val lotNumber: String?,
    val quantityOnHand: BigDecimal?,
    val inventoryValue: BigDecimal?,
    val inventoryLineCount: Int = 1,
    val targetKind: String?,
    val targetName: String?,
    val targetUnit: String?,
    val mappingStatus: String?,
    val observedDamumedQuantity: BigDecimal?,
    val expectedDamumedQuantity: BigDecimal?,
    val averageDailyExpectedQuantity: BigDecimal?,
    val estimatedCoverageDays: BigDecimal?,
    val suggestedPurchaseQuantity: BigDecimal?,
    val confidence: String,
)
