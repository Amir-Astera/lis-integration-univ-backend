package lab.dev.med.univ.feature.reagents.domain.models

import java.time.LocalDate
import java.time.LocalDateTime

enum class WarehouseMovementType {
    RECEIPT,
    WRITE_OFF,
    ADJUSTMENT,
    TRANSFER,
    RETURN,
    INVENTORY_CORRECTION,
}

enum class WarehouseItemType {
    REAGENT,
    CONSUMABLE,
}

data class WarehouseMovement(
    val id: String,
    val movementType: WarehouseMovementType,
    val itemType: WarehouseItemType,
    val reagentId: String? = null,
    val consumableId: String? = null,
    val analyzerId: String? = null,
    val quantity: Double,
    val unitType: String = "UNITS",
    val unitPriceTenge: Double? = null,
    val lotNumber: String? = null,
    val expiryDate: LocalDate? = null,
    val supplier: String? = null,
    val invoiceNumber: String? = null,
    val referenceId: String? = null,
    val reason: String? = null,
    val notes: String? = null,
    val performedBy: String,
    val movementDate: LocalDate = LocalDate.now(),
    val createdAt: LocalDateTime = LocalDateTime.now(),
)

data class WarehouseDailySnapshot(
    val id: String,
    val snapshotDate: LocalDate,
    val itemType: WarehouseItemType,
    val reagentId: String? = null,
    val consumableId: String? = null,
    val itemName: String,
    val analyzerId: String? = null,
    val openingQuantity: Double = 0.0,
    val receiptsQuantity: Double = 0.0,
    val writeOffsQuantity: Double = 0.0,
    val adjustmentsQuantity: Double = 0.0,
    val closingQuantity: Double = 0.0,
    val unitType: String = "UNITS",
    val closingCostTenge: Double? = null,
    val lowStockFlag: Boolean = false,
    val expiryWarningFlag: Boolean = false,
)

data class CreateWarehouseMovementRequest(
    val movementType: WarehouseMovementType,
    val itemType: WarehouseItemType,
    val reagentId: String? = null,
    val consumableId: String? = null,
    val analyzerId: String? = null,
    val quantity: Double,
    val unitType: String = "UNITS",
    val unitPriceTenge: Double? = null,
    val lotNumber: String? = null,
    val expiryDate: LocalDate? = null,
    val supplier: String? = null,
    val invoiceNumber: String? = null,
    val reason: String? = null,
    val notes: String? = null,
    val movementDate: LocalDate = LocalDate.now(),
)

data class WarehouseSummary(
    val totalReceiptsThisMonth: Double,
    val totalWriteOffsThisMonth: Double,
    val lowStockItems: List<WarehouseDailySnapshot>,
    val expiryWarningItems: List<WarehouseDailySnapshot>,
    val recentMovements: List<WarehouseMovement>,
)
