package lab.dev.med.univ.feature.reagents.domain.usecases

import lab.dev.med.univ.feature.reagents.domain.models.ServiceNormSource
import lab.dev.med.univ.feature.reagents.domain.models.ServiceReagentConsumptionNorm
import lab.dev.med.univ.feature.reagents.domain.models.ServiceToAnalyzerMapping
import lab.dev.med.univ.feature.reagents.domain.services.ServiceReagentNormCommandService
import lab.dev.med.univ.feature.reagents.domain.services.ServiceReagentNormQueryService
import lab.dev.med.univ.feature.reagents.domain.services.ServiceToAnalyzerMappingCommandService
import lab.dev.med.univ.feature.reagents.domain.services.ServiceToAnalyzerMappingQueryService
import org.springframework.stereotype.Service
import java.math.BigDecimal

// =============================================================================
// Service Reagent Consumption Norm Use Cases
// =============================================================================

interface GetAllServiceReagentNormsUseCase {
    suspend operator fun invoke(): List<ServiceReagentConsumptionNorm>
}

interface GetServiceReagentNormByIdUseCase {
    suspend operator fun invoke(id: String): ServiceReagentConsumptionNorm
}

interface GetServiceReagentNormsByServiceUseCase {
    suspend operator fun invoke(serviceName: String): List<ServiceReagentConsumptionNorm>
}

interface GetServiceReagentNormsByCategoryUseCase {
    suspend operator fun invoke(category: String): List<ServiceReagentConsumptionNorm>
}

interface CreateServiceReagentNormUseCase {
    suspend operator fun invoke(
        serviceName: String,
        serviceCategory: String? = null,
        analyzerId: String? = null,
        reagentName: String,
        consumableId: String? = null,
        quantityPerService: BigDecimal,
        unitType: String,
        source: ServiceNormSource = ServiceNormSource.MANUAL,
        sourceDocument: String? = null,
        notes: String? = null,
    ): ServiceReagentConsumptionNorm
}

interface UpdateServiceReagentNormUseCase {
    suspend operator fun invoke(
        id: String,
        serviceName: String? = null,
        serviceCategory: String? = null,
        analyzerId: String? = null,
        reagentName: String? = null,
        consumableId: String? = null,
        quantityPerService: BigDecimal? = null,
        unitType: String? = null,
        source: ServiceNormSource? = null,
        sourceDocument: String? = null,
        notes: String? = null,
        isActive: Boolean? = null,
    ): ServiceReagentConsumptionNorm
}

interface DeleteServiceReagentNormUseCase {
    suspend operator fun invoke(id: String)
}

@Service
internal class GetAllServiceReagentNormsUseCaseImpl(
    private val queryService: ServiceReagentNormQueryService,
) : GetAllServiceReagentNormsUseCase {
    override suspend fun invoke(): List<ServiceReagentConsumptionNorm> = queryService.getAll()
}

@Service
internal class GetServiceReagentNormByIdUseCaseImpl(
    private val queryService: ServiceReagentNormQueryService,
) : GetServiceReagentNormByIdUseCase {
    override suspend fun invoke(id: String): ServiceReagentConsumptionNorm = queryService.getById(id)
}

@Service
internal class GetServiceReagentNormsByServiceUseCaseImpl(
    private val queryService: ServiceReagentNormQueryService,
) : GetServiceReagentNormsByServiceUseCase {
    override suspend fun invoke(serviceName: String): List<ServiceReagentConsumptionNorm> =
        queryService.getByServiceName(serviceName)
}

@Service
internal class GetServiceReagentNormsByCategoryUseCaseImpl(
    private val queryService: ServiceReagentNormQueryService,
) : GetServiceReagentNormsByCategoryUseCase {
    override suspend fun invoke(category: String): List<ServiceReagentConsumptionNorm> =
        queryService.getByCategory(category)
}

@Service
internal class CreateServiceReagentNormUseCaseImpl(
    private val commandService: ServiceReagentNormCommandService,
) : CreateServiceReagentNormUseCase {
    override suspend fun invoke(
        serviceName: String,
        serviceCategory: String?,
        analyzerId: String?,
        reagentName: String,
        consumableId: String?,
        quantityPerService: BigDecimal,
        unitType: String,
        source: ServiceNormSource,
        sourceDocument: String?,
        notes: String?,
    ): ServiceReagentConsumptionNorm = commandService.create(
        serviceName = serviceName,
        serviceCategory = serviceCategory,
        analyzerId = analyzerId,
        reagentName = reagentName,
        consumableId = consumableId,
        quantityPerService = quantityPerService,
        unitType = unitType,
        source = source,
        sourceDocument = sourceDocument,
        notes = notes,
    )
}

@Service
internal class UpdateServiceReagentNormUseCaseImpl(
    private val commandService: ServiceReagentNormCommandService,
) : UpdateServiceReagentNormUseCase {
    override suspend fun invoke(
        id: String,
        serviceName: String?,
        serviceCategory: String?,
        analyzerId: String?,
        reagentName: String?,
        consumableId: String?,
        quantityPerService: BigDecimal?,
        unitType: String?,
        source: ServiceNormSource?,
        sourceDocument: String?,
        notes: String?,
        isActive: Boolean?,
    ): ServiceReagentConsumptionNorm = commandService.update(
        id = id,
        serviceName = serviceName,
        serviceCategory = serviceCategory,
        analyzerId = analyzerId,
        reagentName = reagentName,
        consumableId = consumableId,
        quantityPerService = quantityPerService,
        unitType = unitType,
        source = source,
        sourceDocument = sourceDocument,
        notes = notes,
        isActive = isActive,
    )
}

@Service
internal class DeleteServiceReagentNormUseCaseImpl(
    private val commandService: ServiceReagentNormCommandService,
) : DeleteServiceReagentNormUseCase {
    override suspend fun invoke(id: String) = commandService.delete(id)
}

// =============================================================================
// Service-to-Analyzer Mapping Use Cases
// =============================================================================

interface GetAllServiceToAnalyzerMappingsUseCase {
    suspend operator fun invoke(): List<ServiceToAnalyzerMapping>
}

interface GetServiceToAnalyzerMappingByIdUseCase {
    suspend operator fun invoke(id: String): ServiceToAnalyzerMapping
}

interface FindMatchingAnalyzerForServiceUseCase {
    suspend operator fun invoke(serviceName: String, category: String? = null): ServiceToAnalyzerMapping?
}

interface CreateServiceToAnalyzerMappingUseCase {
    suspend operator fun invoke(
        serviceNamePattern: String,
        serviceCategory: String? = null,
        analyzerId: String,
        priority: Int = 100,
    ): ServiceToAnalyzerMapping
}

interface UpdateServiceToAnalyzerMappingUseCase {
    suspend operator fun invoke(
        id: String,
        serviceNamePattern: String? = null,
        serviceCategory: String? = null,
        analyzerId: String? = null,
        priority: Int? = null,
        isActive: Boolean? = null,
    ): ServiceToAnalyzerMapping
}

interface DeleteServiceToAnalyzerMappingUseCase {
    suspend operator fun invoke(id: String)
}

@Service
internal class GetAllServiceToAnalyzerMappingsUseCaseImpl(
    private val queryService: ServiceToAnalyzerMappingQueryService,
) : GetAllServiceToAnalyzerMappingsUseCase {
    override suspend fun invoke(): List<ServiceToAnalyzerMapping> = queryService.getActive()
}

@Service
internal class GetServiceToAnalyzerMappingByIdUseCaseImpl(
    private val queryService: ServiceToAnalyzerMappingQueryService,
) : GetServiceToAnalyzerMappingByIdUseCase {
    override suspend fun invoke(id: String): ServiceToAnalyzerMapping {
        // Query all and filter by ID since repository doesn't have findById custom method
        return queryService.getAll().firstOrNull { it.id == id }
            ?: throw lab.dev.med.univ.feature.reagents.domain.errors.ServiceReagentNormNotFoundException(id)
    }
}

@Service
internal class FindMatchingAnalyzerForServiceUseCaseImpl(
    private val queryService: ServiceToAnalyzerMappingQueryService,
) : FindMatchingAnalyzerForServiceUseCase {
    override suspend fun invoke(serviceName: String, category: String?): ServiceToAnalyzerMapping? =
        queryService.findMatchingAnalyzer(serviceName, category)
}

@Service
internal class CreateServiceToAnalyzerMappingUseCaseImpl(
    private val commandService: ServiceToAnalyzerMappingCommandService,
) : CreateServiceToAnalyzerMappingUseCase {
    override suspend fun invoke(
        serviceNamePattern: String,
        serviceCategory: String?,
        analyzerId: String,
        priority: Int,
    ): ServiceToAnalyzerMapping = commandService.create(
        serviceNamePattern = serviceNamePattern,
        serviceCategory = serviceCategory,
        analyzerId = analyzerId,
        priority = priority,
    )
}

@Service
internal class UpdateServiceToAnalyzerMappingUseCaseImpl(
    private val commandService: ServiceToAnalyzerMappingCommandService,
) : UpdateServiceToAnalyzerMappingUseCase {
    override suspend fun invoke(
        id: String,
        serviceNamePattern: String?,
        serviceCategory: String?,
        analyzerId: String?,
        priority: Int?,
        isActive: Boolean?,
    ): ServiceToAnalyzerMapping = commandService.update(
        id = id,
        serviceNamePattern = serviceNamePattern,
        serviceCategory = serviceCategory,
        analyzerId = analyzerId,
        priority = priority,
        isActive = isActive,
    )
}

@Service
internal class DeleteServiceToAnalyzerMappingUseCaseImpl(
    private val commandService: ServiceToAnalyzerMappingCommandService,
) : DeleteServiceToAnalyzerMappingUseCase {
    override suspend fun invoke(id: String) = commandService.delete(id)
}
