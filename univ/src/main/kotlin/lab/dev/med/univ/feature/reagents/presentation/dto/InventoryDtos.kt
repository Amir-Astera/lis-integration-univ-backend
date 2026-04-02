package lab.dev.med.univ.feature.reagents.presentation.dto

import lab.dev.med.univ.feature.reagents.domain.models.ConsumableCategory
import lab.dev.med.univ.feature.reagents.domain.models.ConsumableInventory
import lab.dev.med.univ.feature.reagents.domain.models.ReagentInventory
import lab.dev.med.univ.feature.reagents.domain.models.ReagentInventoryStatus
import lab.dev.med.univ.feature.reagents.domain.models.ReagentUnitType
import lab.dev.med.univ.feature.reagents.domain.models.TubeColor
import java.time.LocalDate
import java.time.LocalDateTime

data class ReagentInventoryResponseDto(
    val id: String,
    val analyzerId: String?,
    val reagentName: String,
    val lotNumber: String?,
    val manufacturer: String?,
    val expiryDateSealed: LocalDate?,
    val stabilityDaysAfterOpening: Int?,
    val openedDate: LocalDate?,
    val totalVolumeMl: Double?,
    val totalUnits: Int?,
    val unitType: ReagentUnitType,
    val unitPriceTenge: Double?,
    val status: ReagentInventoryStatus,
    val receivedAt: LocalDate,
    val receivedBy: String?,
    val notes: String?,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime,
)

data class UpsertReagentInventoryRequestDto(
    val analyzerId: String? = null,
    val reagentName: String,
    val lotNumber: String? = null,
    val manufacturer: String? = null,
    val expiryDateSealed: LocalDate? = null,
    val stabilityDaysAfterOpening: Int? = null,
    val openedDate: LocalDate? = null,
    val totalVolumeMl: Double? = null,
    val totalUnits: Int? = null,
    val unitType: ReagentUnitType,
    val unitPriceTenge: Double? = null,
    val status: ReagentInventoryStatus = ReagentInventoryStatus.IN_STOCK,
    val receivedAt: LocalDate,
    val notes: String? = null,
)

data class ConsumableInventoryResponseDto(
    val id: String,
    val name: String,
    val category: ConsumableCategory,
    val tubeColor: TubeColor?,
    val linkedAnalyzerTypes: String?,
    val linkedServiceKeywords: String?,
    val quantityTotal: Int,
    val quantityRemaining: Int,
    val unitPriceTenge: Double?,
    val lotNumber: String?,
    val expiryDate: LocalDate?,
    val receivedAt: LocalDate,
    val receivedBy: String?,
    val notes: String?,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime,
)

data class UpsertConsumableInventoryRequestDto(
    val name: String,
    val category: ConsumableCategory,
    val tubeColor: TubeColor? = null,
    val linkedAnalyzerTypes: String? = null,
    val linkedServiceKeywords: String? = null,
    val quantityTotal: Int,
    val quantityRemaining: Int,
    val unitPriceTenge: Double? = null,
    val lotNumber: String? = null,
    val expiryDate: LocalDate? = null,
    val receivedAt: LocalDate,
    val notes: String? = null,
)

fun ReagentInventory.toResponseDto() = ReagentInventoryResponseDto(
    id = id,
    analyzerId = analyzerId,
    reagentName = reagentName,
    lotNumber = lotNumber,
    manufacturer = manufacturer,
    expiryDateSealed = expiryDateSealed,
    stabilityDaysAfterOpening = stabilityDaysAfterOpening,
    openedDate = openedDate,
    totalVolumeMl = totalVolumeMl,
    totalUnits = totalUnits,
    unitType = unitType,
    unitPriceTenge = unitPriceTenge,
    status = status,
    receivedAt = receivedAt,
    receivedBy = receivedBy,
    notes = notes,
    createdAt = createdAt,
    updatedAt = updatedAt,
)

fun UpsertReagentInventoryRequestDto.toModel(id: String, actor: String?) = ReagentInventory(
    id = id,
    analyzerId = analyzerId,
    reagentName = reagentName,
    lotNumber = lotNumber,
    manufacturer = manufacturer,
    expiryDateSealed = expiryDateSealed,
    stabilityDaysAfterOpening = stabilityDaysAfterOpening,
    openedDate = openedDate,
    totalVolumeMl = totalVolumeMl,
    totalUnits = totalUnits,
    unitType = unitType,
    unitPriceTenge = unitPriceTenge,
    status = status,
    receivedAt = receivedAt,
    receivedBy = actor,
    notes = notes,
)

fun ConsumableInventory.toResponseDto() = ConsumableInventoryResponseDto(
    id = id,
    name = name,
    category = category,
    tubeColor = tubeColor,
    linkedAnalyzerTypes = linkedAnalyzerTypes,
    linkedServiceKeywords = linkedServiceKeywords,
    quantityTotal = quantityTotal,
    quantityRemaining = quantityRemaining,
    unitPriceTenge = unitPriceTenge,
    lotNumber = lotNumber,
    expiryDate = expiryDate,
    receivedAt = receivedAt,
    receivedBy = receivedBy,
    notes = notes,
    createdAt = createdAt,
    updatedAt = updatedAt,
)

fun UpsertConsumableInventoryRequestDto.toModel(id: String, actor: String?) = ConsumableInventory(
    id = id,
    name = name,
    category = category,
    tubeColor = tubeColor,
    linkedAnalyzerTypes = linkedAnalyzerTypes,
    linkedServiceKeywords = linkedServiceKeywords,
    quantityTotal = quantityTotal,
    quantityRemaining = quantityRemaining,
    unitPriceTenge = unitPriceTenge,
    lotNumber = lotNumber,
    expiryDate = expiryDate,
    receivedAt = receivedAt,
    receivedBy = actor,
    notes = notes,
)
