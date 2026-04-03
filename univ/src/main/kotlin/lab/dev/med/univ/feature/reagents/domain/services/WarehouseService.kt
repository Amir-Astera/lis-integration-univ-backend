package lab.dev.med.univ.feature.reagents.domain.services

import kotlinx.coroutines.flow.toList
import lab.dev.med.univ.feature.reagents.data.entity.WarehouseMovementEntity
import lab.dev.med.univ.feature.reagents.data.entity.toModel
import lab.dev.med.univ.feature.reagents.data.repository.ReagentInventoryRepository
import lab.dev.med.univ.feature.reagents.data.repository.ConsumableInventoryRepository
import lab.dev.med.univ.feature.reagents.data.repository.WarehouseDailySnapshotRepository
import lab.dev.med.univ.feature.reagents.data.repository.WarehouseMovementRepository
import lab.dev.med.univ.feature.reagents.domain.models.CreateWarehouseMovementRequest
import lab.dev.med.univ.feature.reagents.domain.models.WarehouseDailySnapshot
import lab.dev.med.univ.feature.reagents.domain.models.WarehouseMovement
import lab.dev.med.univ.feature.reagents.domain.models.WarehouseMovementType
import lab.dev.med.univ.feature.reagents.domain.models.WarehouseSummary
import org.springframework.stereotype.Service
import java.time.LocalDate
import java.util.UUID

interface WarehouseService {
    suspend fun createMovement(request: CreateWarehouseMovementRequest, performedBy: String): WarehouseMovement
    suspend fun getMovements(from: LocalDate, to: LocalDate): List<WarehouseMovement>
    suspend fun getMovementsByReagent(reagentId: String): List<WarehouseMovement>
    suspend fun getMovementsByConsumable(consumableId: String): List<WarehouseMovement>
    suspend fun getLowStockItems(): List<WarehouseDailySnapshot>
    suspend fun getExpiryWarningItems(): List<WarehouseDailySnapshot>
    suspend fun getSummary(): WarehouseSummary
    suspend fun deleteMovement(id: String)
}

@Service
class WarehouseServiceImpl(
    private val movementRepository: WarehouseMovementRepository,
    private val snapshotRepository: WarehouseDailySnapshotRepository,
    private val reagentInventoryRepository: ReagentInventoryRepository,
    private val consumableInventoryRepository: ConsumableInventoryRepository,
) : WarehouseService {

    override suspend fun createMovement(
        request: CreateWarehouseMovementRequest,
        performedBy: String,
    ): WarehouseMovement {
        val entity = WarehouseMovementEntity(
            id = UUID.randomUUID().toString(),
            movementType = request.movementType.name,
            itemType = request.itemType.name,
            reagentId = request.reagentId,
            consumableId = request.consumableId,
            analyzerId = request.analyzerId,
            quantity = request.quantity,
            unitType = request.unitType,
            unitPriceTenge = request.unitPriceTenge,
            lotNumber = request.lotNumber,
            expiryDate = request.expiryDate,
            supplier = request.supplier,
            invoiceNumber = request.invoiceNumber,
            reason = request.reason,
            notes = request.notes,
            performedBy = performedBy,
            movementDate = request.movementDate,
        )
        val saved = movementRepository.save(entity)

        // Update inventory quantities
        applyInventoryChange(request)

        return saved.toModel()
    }

    private suspend fun applyInventoryChange(request: CreateWarehouseMovementRequest) {
        val delta = when (request.movementType) {
            WarehouseMovementType.RECEIPT, WarehouseMovementType.RETURN -> request.quantity
            WarehouseMovementType.WRITE_OFF -> -request.quantity
            WarehouseMovementType.ADJUSTMENT, WarehouseMovementType.INVENTORY_CORRECTION -> request.quantity
            WarehouseMovementType.TRANSFER -> -request.quantity
        }

        if (request.reagentId != null) {
            reagentInventoryRepository.findById(request.reagentId)?.let { reagent ->
                // For reagents: adjust totalVolumeMl or totalUnits depending on unitType
                val newVolume = reagent.totalVolumeMl?.let { maxOf(0.0, it + delta) }
                val newUnits = if (newVolume == null) {
                    reagent.totalUnits?.let { maxOf(0, (it + delta).toInt()) }
                } else null
                reagentInventoryRepository.save(
                    reagent.copy(
                        totalVolumeMl = newVolume ?: reagent.totalVolumeMl,
                        totalUnits = newUnits ?: reagent.totalUnits,
                        updatedAt = java.time.LocalDateTime.now(),
                    )
                )
            }
        } else if (request.consumableId != null) {
            consumableInventoryRepository.findById(request.consumableId)?.let { consumable ->
                val newRemaining = maxOf(0, consumable.quantityRemaining + delta.toInt())
                consumableInventoryRepository.save(
                    consumable.copy(
                        quantityRemaining = newRemaining,
                        updatedAt = java.time.LocalDateTime.now(),
                    )
                )
            }
        }
    }

    override suspend fun getMovements(from: LocalDate, to: LocalDate): List<WarehouseMovement> =
        movementRepository.findAllByMovementDateBetweenOrderByMovementDateDescCreatedAtDesc(from, to)
            .toList()
            .map { it.toModel() }

    override suspend fun getMovementsByReagent(reagentId: String): List<WarehouseMovement> =
        movementRepository.findAllByReagentIdOrderByMovementDateDescCreatedAtDesc(reagentId)
            .toList()
            .map { it.toModel() }

    override suspend fun getMovementsByConsumable(consumableId: String): List<WarehouseMovement> =
        movementRepository.findAllByConsumableIdOrderByMovementDateDescCreatedAtDesc(consumableId)
            .toList()
            .map { it.toModel() }

    override suspend fun getLowStockItems(): List<WarehouseDailySnapshot> =
        snapshotRepository.findAllByLowStockFlagTrueOrderByClosingQuantityAsc()
            .toList()
            .map { it.toModel() }

    override suspend fun getExpiryWarningItems(): List<WarehouseDailySnapshot> =
        snapshotRepository.findAllByExpiryWarningFlagTrueOrderBySnapshotDate()
            .toList()
            .map { it.toModel() }

    override suspend fun getSummary(): WarehouseSummary {
        val now = LocalDate.now()
        val monthStart = now.withDayOfMonth(1)

        val recentMovements = movementRepository
            .findAllByMovementDateBetweenOrderByMovementDateDescCreatedAtDesc(monthStart, now)
            .toList()
            .map { it.toModel() }

        val receipts = recentMovements.filter { it.movementType == WarehouseMovementType.RECEIPT }
            .sumOf { it.quantity }
        val writeOffs = recentMovements.filter { it.movementType == WarehouseMovementType.WRITE_OFF }
            .sumOf { it.quantity }

        val lowStock = snapshotRepository.findAllByLowStockFlagTrueOrderByClosingQuantityAsc()
            .toList().map { it.toModel() }
        val expiryWarning = snapshotRepository.findAllByExpiryWarningFlagTrueOrderBySnapshotDate()
            .toList().map { it.toModel() }

        return WarehouseSummary(
            totalReceiptsThisMonth = receipts,
            totalWriteOffsThisMonth = writeOffs,
            lowStockItems = lowStock,
            expiryWarningItems = expiryWarning,
            recentMovements = recentMovements.take(20),
        )
    }

    override suspend fun deleteMovement(id: String) {
        movementRepository.deleteById(id)
    }
}
