package lab.dev.med.univ.feature.reagents.domain.usecases

import lab.dev.med.univ.feature.reagents.domain.models.ConsumableCategory
import lab.dev.med.univ.feature.reagents.domain.models.ConsumableInventory
import lab.dev.med.univ.feature.reagents.domain.models.ReagentInventory
import lab.dev.med.univ.feature.reagents.domain.models.ReagentInventoryStatus
import lab.dev.med.univ.feature.reagents.domain.services.InventoryCommandService
import lab.dev.med.univ.feature.reagents.domain.services.InventoryQueryService
import org.springframework.stereotype.Service

interface GetReagentInventoryUseCase {
    suspend operator fun invoke(
        analyzerId: String? = null,
        status: ReagentInventoryStatus? = null,
    ): List<ReagentInventory>
}

interface GetReagentInventoryItemUseCase {
    suspend operator fun invoke(inventoryId: String): ReagentInventory
}

interface UpsertReagentInventoryUseCase {
    suspend operator fun invoke(inventoryId: String, inventory: ReagentInventory): ReagentInventory
}

interface DeleteReagentInventoryUseCase {
    suspend operator fun invoke(inventoryId: String)
}

interface GetConsumableInventoryUseCase {
    suspend operator fun invoke(category: ConsumableCategory? = null): List<ConsumableInventory>
}

interface GetConsumableInventoryItemUseCase {
    suspend operator fun invoke(inventoryId: String): ConsumableInventory
}

interface UpsertConsumableInventoryUseCase {
    suspend operator fun invoke(inventoryId: String, inventory: ConsumableInventory): ConsumableInventory
}

interface DeleteConsumableInventoryUseCase {
    suspend operator fun invoke(inventoryId: String)
}

@Service
internal class GetReagentInventoryUseCaseImpl(
    private val queryService: InventoryQueryService,
) : GetReagentInventoryUseCase {
    override suspend fun invoke(analyzerId: String?, status: ReagentInventoryStatus?): List<ReagentInventory> {
        return queryService.getReagentInventory(analyzerId, status)
    }
}

@Service
internal class GetReagentInventoryItemUseCaseImpl(
    private val queryService: InventoryQueryService,
) : GetReagentInventoryItemUseCase {
    override suspend fun invoke(inventoryId: String): ReagentInventory = queryService.getReagentInventoryItem(inventoryId)
}

@Service
internal class UpsertReagentInventoryUseCaseImpl(
    private val commandService: InventoryCommandService,
) : UpsertReagentInventoryUseCase {
    override suspend fun invoke(inventoryId: String, inventory: ReagentInventory): ReagentInventory {
        return commandService.upsertReagentInventory(inventoryId, inventory)
    }
}

@Service
internal class DeleteReagentInventoryUseCaseImpl(
    private val commandService: InventoryCommandService,
) : DeleteReagentInventoryUseCase {
    override suspend fun invoke(inventoryId: String) = commandService.deleteReagentInventory(inventoryId)
}

@Service
internal class GetConsumableInventoryUseCaseImpl(
    private val queryService: InventoryQueryService,
) : GetConsumableInventoryUseCase {
    override suspend fun invoke(category: ConsumableCategory?): List<ConsumableInventory> {
        return queryService.getConsumableInventory(category)
    }
}

@Service
internal class GetConsumableInventoryItemUseCaseImpl(
    private val queryService: InventoryQueryService,
) : GetConsumableInventoryItemUseCase {
    override suspend fun invoke(inventoryId: String): ConsumableInventory = queryService.getConsumableInventoryItem(inventoryId)
}

@Service
internal class UpsertConsumableInventoryUseCaseImpl(
    private val commandService: InventoryCommandService,
) : UpsertConsumableInventoryUseCase {
    override suspend fun invoke(inventoryId: String, inventory: ConsumableInventory): ConsumableInventory {
        return commandService.upsertConsumableInventory(inventoryId, inventory)
    }
}

@Service
internal class DeleteConsumableInventoryUseCaseImpl(
    private val commandService: InventoryCommandService,
) : DeleteConsumableInventoryUseCase {
    override suspend fun invoke(inventoryId: String) = commandService.deleteConsumableInventory(inventoryId)
}
