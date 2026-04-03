package lab.dev.med.univ.feature.reagents.data.entity

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table
import java.time.LocalDate
import java.time.LocalDateTime

@Table("warehouse_movements")
data class WarehouseMovementEntity(
    @Id
    val id: String,
    val movementType: String,
    val itemType: String,
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

@Table("warehouse_daily_snapshots")
data class WarehouseDailySnapshotEntity(
    @Id
    val id: String,
    val snapshotDate: LocalDate,
    val itemType: String,
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
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val updatedAt: LocalDateTime = LocalDateTime.now(),
)
