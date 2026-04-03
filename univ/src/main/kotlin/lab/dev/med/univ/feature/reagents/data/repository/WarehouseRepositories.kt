package lab.dev.med.univ.feature.reagents.data.repository

import kotlinx.coroutines.flow.Flow
import lab.dev.med.univ.feature.reagents.data.entity.WarehouseDailySnapshotEntity
import lab.dev.med.univ.feature.reagents.data.entity.WarehouseMovementEntity
import org.springframework.data.repository.kotlin.CoroutineCrudRepository
import java.time.LocalDate

interface WarehouseMovementRepository : CoroutineCrudRepository<WarehouseMovementEntity, String> {

    fun findAllByMovementDateBetweenOrderByMovementDateDescCreatedAtDesc(
        from: LocalDate,
        to: LocalDate,
    ): Flow<WarehouseMovementEntity>

    fun findAllByReagentIdOrderByMovementDateDescCreatedAtDesc(
        reagentId: String,
    ): Flow<WarehouseMovementEntity>

    fun findAllByConsumableIdOrderByMovementDateDescCreatedAtDesc(
        consumableId: String,
    ): Flow<WarehouseMovementEntity>

    fun findAllByAnalyzerIdAndMovementDateBetweenOrderByMovementDateDesc(
        analyzerId: String,
        from: LocalDate,
        to: LocalDate,
    ): Flow<WarehouseMovementEntity>

    fun findAllByMovementTypeAndMovementDateBetween(
        movementType: String,
        from: LocalDate,
        to: LocalDate,
    ): Flow<WarehouseMovementEntity>
}

interface WarehouseDailySnapshotRepository : CoroutineCrudRepository<WarehouseDailySnapshotEntity, String> {

    fun findAllBySnapshotDateBetweenOrderBySnapshotDateDesc(
        from: LocalDate,
        to: LocalDate,
    ): Flow<WarehouseDailySnapshotEntity>

    fun findAllBySnapshotDateOrderByItemName(snapshotDate: LocalDate): Flow<WarehouseDailySnapshotEntity>

    fun findAllByLowStockFlagTrueOrderByClosingQuantityAsc(): Flow<WarehouseDailySnapshotEntity>

    fun findAllByExpiryWarningFlagTrueOrderBySnapshotDate(): Flow<WarehouseDailySnapshotEntity>

    suspend fun findBySnapshotDateAndReagentId(snapshotDate: LocalDate, reagentId: String): WarehouseDailySnapshotEntity?

    suspend fun findBySnapshotDateAndConsumableId(snapshotDate: LocalDate, consumableId: String): WarehouseDailySnapshotEntity?

    suspend fun deleteAllBySnapshotDateBetween(from: LocalDate, to: LocalDate)
}
