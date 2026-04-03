package lab.dev.med.univ.feature.reagents.data.entity

import lab.dev.med.univ.feature.reagents.domain.models.WarehouseDailySnapshot
import lab.dev.med.univ.feature.reagents.domain.models.WarehouseItemType
import lab.dev.med.univ.feature.reagents.domain.models.WarehouseMovement
import lab.dev.med.univ.feature.reagents.domain.models.WarehouseMovementType

fun WarehouseMovementEntity.toModel() = WarehouseMovement(
    id = id,
    movementType = WarehouseMovementType.valueOf(movementType),
    itemType = WarehouseItemType.valueOf(itemType),
    reagentId = reagentId,
    consumableId = consumableId,
    analyzerId = analyzerId,
    quantity = quantity,
    unitType = unitType,
    unitPriceTenge = unitPriceTenge,
    lotNumber = lotNumber,
    expiryDate = expiryDate,
    supplier = supplier,
    invoiceNumber = invoiceNumber,
    referenceId = referenceId,
    reason = reason,
    notes = notes,
    performedBy = performedBy,
    movementDate = movementDate,
    createdAt = createdAt,
)

fun WarehouseMovement.toEntity() = WarehouseMovementEntity(
    id = id,
    movementType = movementType.name,
    itemType = itemType.name,
    reagentId = reagentId,
    consumableId = consumableId,
    analyzerId = analyzerId,
    quantity = quantity,
    unitType = unitType,
    unitPriceTenge = unitPriceTenge,
    lotNumber = lotNumber,
    expiryDate = expiryDate,
    supplier = supplier,
    invoiceNumber = invoiceNumber,
    referenceId = referenceId,
    reason = reason,
    notes = notes,
    performedBy = performedBy,
    movementDate = movementDate,
    createdAt = createdAt,
)

fun WarehouseDailySnapshotEntity.toModel() = WarehouseDailySnapshot(
    id = id,
    snapshotDate = snapshotDate,
    itemType = WarehouseItemType.valueOf(itemType),
    reagentId = reagentId,
    consumableId = consumableId,
    itemName = itemName,
    analyzerId = analyzerId,
    openingQuantity = openingQuantity,
    receiptsQuantity = receiptsQuantity,
    writeOffsQuantity = writeOffsQuantity,
    adjustmentsQuantity = adjustmentsQuantity,
    closingQuantity = closingQuantity,
    unitType = unitType,
    closingCostTenge = closingCostTenge,
    lowStockFlag = lowStockFlag,
    expiryWarningFlag = expiryWarningFlag,
)
