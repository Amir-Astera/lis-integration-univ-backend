package lab.dev.med.univ.feature.reagents.domain.services

import kotlinx.coroutines.flow.toList
import lab.dev.med.univ.feature.reagents.data.entity.toEntity
import lab.dev.med.univ.feature.reagents.data.entity.toModel
import lab.dev.med.univ.feature.reagents.data.repository.AnalyzerRepository
import lab.dev.med.univ.feature.reagents.data.repository.ConsumableInventoryRepository
import lab.dev.med.univ.feature.reagents.data.repository.ReagentInventoryRepository
import lab.dev.med.univ.feature.reagents.domain.errors.AnalyzerNotFoundException
import lab.dev.med.univ.feature.reagents.domain.errors.ConsumableInventoryNotFoundException
import lab.dev.med.univ.feature.reagents.domain.errors.ReagentInventoryNotFoundException
import lab.dev.med.univ.feature.reagents.domain.errors.ReagentModuleValidationException
import lab.dev.med.univ.feature.reagents.domain.models.ConsumableCategory
import lab.dev.med.univ.feature.reagents.domain.models.ConsumableInventory
import lab.dev.med.univ.feature.reagents.domain.models.ReagentInventory
import lab.dev.med.univ.feature.reagents.domain.models.ReagentInventoryStatus
import lab.dev.med.univ.feature.reagents.domain.models.ReagentUnitType
import org.springframework.stereotype.Service
import java.time.LocalDateTime

interface InventoryQueryService {
    suspend fun getReagentInventory(
        analyzerId: String? = null,
        status: ReagentInventoryStatus? = null,
    ): List<ReagentInventory>

    suspend fun getReagentInventoryItem(inventoryId: String): ReagentInventory
    suspend fun getConsumableInventory(category: ConsumableCategory? = null): List<ConsumableInventory>
    suspend fun getConsumableInventoryItem(inventoryId: String): ConsumableInventory
}

interface InventoryCommandService {
    suspend fun upsertReagentInventory(inventoryId: String, inventory: ReagentInventory): ReagentInventory
    suspend fun deleteReagentInventory(inventoryId: String)
    suspend fun upsertConsumableInventory(inventoryId: String, inventory: ConsumableInventory): ConsumableInventory
    suspend fun deleteConsumableInventory(inventoryId: String)
}

@Service
internal class InventoryQueryServiceImpl(
    private val reagentInventoryRepository: ReagentInventoryRepository,
    private val consumableInventoryRepository: ConsumableInventoryRepository,
) : InventoryQueryService {

    override suspend fun getReagentInventory(
        analyzerId: String?,
        status: ReagentInventoryStatus?,
    ): List<ReagentInventory> {
        val items = when {
            analyzerId != null -> reagentInventoryRepository.findAllByAnalyzerIdOrderByReceivedAtDescCreatedAtDesc(analyzerId)
                .toList()
            status != null -> reagentInventoryRepository.findAllByStatusOrderByReceivedAtDescCreatedAtDesc(status)
                .toList()
            else -> reagentInventoryRepository.findAllByOrderByReceivedAtDescCreatedAtDesc().toList()
        }.map { it.toModel() }

        return if (analyzerId != null && status != null) {
            items.filter { it.status == status }
        } else {
            items
        }
    }

    override suspend fun getReagentInventoryItem(inventoryId: String): ReagentInventory {
        return reagentInventoryRepository.findById(inventoryId)?.toModel()
            ?: throw ReagentInventoryNotFoundException(inventoryId)
    }

    override suspend fun getConsumableInventory(category: ConsumableCategory?): List<ConsumableInventory> {
        val items = consumableInventoryRepository.findAllByOrderByReceivedAtDescCreatedAtDesc()
            .toList()
            .map { it.toModel() }
        return if (category == null) items else items.filter { it.category == category }
    }

    override suspend fun getConsumableInventoryItem(inventoryId: String): ConsumableInventory {
        return consumableInventoryRepository.findById(inventoryId)?.toModel()
            ?: throw ConsumableInventoryNotFoundException(inventoryId)
    }
}

@Service
internal class InventoryCommandServiceImpl(
    private val analyzerRepository: AnalyzerRepository,
    private val reagentInventoryRepository: ReagentInventoryRepository,
    private val consumableInventoryRepository: ConsumableInventoryRepository,
) : InventoryCommandService {

    override suspend fun upsertReagentInventory(inventoryId: String, inventory: ReagentInventory): ReagentInventory {
        inventory.analyzerId?.let {
            if (analyzerRepository.findById(it) == null) {
                throw AnalyzerNotFoundException(it)
            }
        }
        validateReagentInventory(inventory)

        val existing = reagentInventoryRepository.findById(inventoryId)?.toModel()
        val now = LocalDateTime.now()
        val target = if (existing == null) {
            inventory.copy(
                id = inventoryId,
                createdAt = now,
                updatedAt = now,
                version = null,
            )
        } else {
            inventory.copy(
                id = inventoryId,
                createdAt = existing.createdAt,
                receivedBy = existing.receivedBy ?: inventory.receivedBy,
                updatedAt = now,
                version = existing.version,
            )
        }

        return reagentInventoryRepository.save(target.toEntity()).toModel()
    }

    override suspend fun deleteReagentInventory(inventoryId: String) {
        if (reagentInventoryRepository.findById(inventoryId) == null) {
            throw ReagentInventoryNotFoundException(inventoryId)
        }
        reagentInventoryRepository.deleteById(inventoryId)
    }

    override suspend fun upsertConsumableInventory(inventoryId: String, inventory: ConsumableInventory): ConsumableInventory {
        validateConsumableInventory(inventory)

        val existing = consumableInventoryRepository.findById(inventoryId)?.toModel()
        val now = LocalDateTime.now()
        val target = if (existing == null) {
            inventory.copy(
                id = inventoryId,
                createdAt = now,
                updatedAt = now,
                version = null,
            )
        } else {
            inventory.copy(
                id = inventoryId,
                createdAt = existing.createdAt,
                receivedBy = existing.receivedBy ?: inventory.receivedBy,
                updatedAt = now,
                version = existing.version,
            )
        }

        return consumableInventoryRepository.save(target.toEntity()).toModel()
    }

    override suspend fun deleteConsumableInventory(inventoryId: String) {
        if (consumableInventoryRepository.findById(inventoryId) == null) {
            throw ConsumableInventoryNotFoundException(inventoryId)
        }
        consumableInventoryRepository.deleteById(inventoryId)
    }

    private fun validateReagentInventory(inventory: ReagentInventory) {
        if (inventory.reagentName.isBlank()) {
            throw ReagentModuleValidationException("Reagent name must not be blank.")
        }
        if (inventory.unitPriceTenge != null && inventory.unitPriceTenge < 0.0) {
            throw ReagentModuleValidationException("Reagent unit price must be greater than or equal to zero.")
        }
        if (inventory.stabilityDaysAfterOpening != null && inventory.stabilityDaysAfterOpening < 0) {
            throw ReagentModuleValidationException("Reagent stability days after opening must be greater than or equal to zero.")
        }
        if (inventory.openedDate != null && inventory.openedDate.isBefore(inventory.receivedAt)) {
            throw ReagentModuleValidationException("Opened date cannot be earlier than received date.")
        }
        when (inventory.unitType) {
            ReagentUnitType.ML, ReagentUnitType.LITER, ReagentUnitType.UL -> {
                if (inventory.totalVolumeMl == null || inventory.totalVolumeMl <= 0.0) {
                    throw ReagentModuleValidationException("Reagent inventory in volume units must have positive totalVolumeMl.")
                }
            }
            ReagentUnitType.MG, ReagentUnitType.G, ReagentUnitType.KG -> {
                if (inventory.totalVolumeMl == null || inventory.totalVolumeMl <= 0.0) {
                    throw ReagentModuleValidationException("Reagent inventory in mass units must have positive totalVolumeMl.")
                }
            }
            ReagentUnitType.IU, ReagentUnitType.MILLI_IU -> {
                if (inventory.totalUnits == null || inventory.totalUnits <= 0) {
                    throw ReagentModuleValidationException("Reagent inventory in IU units must have positive totalUnits.")
                }
            }
            ReagentUnitType.PIECE, ReagentUnitType.TEST, ReagentUnitType.KIT, ReagentUnitType.TEST_POSITION,
            ReagentUnitType.STRIP, ReagentUnitType.SLIDE, ReagentUnitType.CHIP, ReagentUnitType.DISK, ReagentUnitType.PLATE, ReagentUnitType.WELL -> {
                if (inventory.totalUnits == null || inventory.totalUnits <= 0) {
                    throw ReagentModuleValidationException("Piece/test-position inventory must have positive totalUnits.")
                }
            }
            ReagentUnitType.BOX, ReagentUnitType.PACK, ReagentUnitType.CASE,
            ReagentUnitType.BOTTLE, ReagentUnitType.VIAL, ReagentUnitType.AMPOULE, ReagentUnitType.TUBE, ReagentUnitType.CANISTER, ReagentUnitType.CARTRIDGE, ReagentUnitType.CASSETTE -> {
                if (inventory.totalUnits == null || inventory.totalUnits <= 0) {
                    throw ReagentModuleValidationException("Container inventory must have positive totalUnits.")
                }
            }
        }
    }

    private fun validateConsumableInventory(inventory: ConsumableInventory) {
        if (inventory.name.isBlank()) {
            throw ReagentModuleValidationException("Consumable name must not be blank.")
        }
        if (inventory.quantityTotal <= 0) {
            throw ReagentModuleValidationException("Consumable quantityTotal must be greater than zero.")
        }
        if (inventory.quantityRemaining < 0) {
            throw ReagentModuleValidationException("Consumable quantityRemaining must be greater than or equal to zero.")
        }
        if (inventory.quantityRemaining > inventory.quantityTotal) {
            throw ReagentModuleValidationException("Consumable quantityRemaining cannot exceed quantityTotal.")
        }
        if (inventory.unitPriceTenge != null && inventory.unitPriceTenge < 0.0) {
            throw ReagentModuleValidationException("Consumable unit price must be greater than or equal to zero.")
        }
    }
}
