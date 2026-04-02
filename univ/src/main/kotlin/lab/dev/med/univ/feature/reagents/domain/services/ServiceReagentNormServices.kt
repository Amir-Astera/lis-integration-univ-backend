package lab.dev.med.univ.feature.reagents.domain.services

import kotlinx.coroutines.flow.toList
import lab.dev.med.univ.feature.reagents.data.entity.ServiceReagentConsumptionNormEntity
import lab.dev.med.univ.feature.reagents.data.entity.ServiceToAnalyzerMappingEntity
import lab.dev.med.univ.feature.reagents.data.entity.normalizeServiceName
import lab.dev.med.univ.feature.reagents.data.entity.toEntity
import lab.dev.med.univ.feature.reagents.data.entity.toModel
import lab.dev.med.univ.feature.reagents.data.repository.ServiceReagentConsumptionNormRepository
import lab.dev.med.univ.feature.reagents.data.repository.ServiceToAnalyzerMappingRepository
import lab.dev.med.univ.feature.reagents.domain.errors.ServiceReagentNormNotFoundException
import lab.dev.med.univ.feature.reagents.domain.errors.ServiceReagentNormValidationException
import lab.dev.med.univ.feature.reagents.domain.models.ServiceNormSource
import lab.dev.med.univ.feature.reagents.domain.models.ServiceReagentConsumptionNorm
import lab.dev.med.univ.feature.reagents.domain.models.ServiceToAnalyzerMapping
import org.springframework.stereotype.Service
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.UUID

/**
 * Query service for service reagent consumption norms.
 */
interface ServiceReagentNormQueryService {
    suspend fun getAll(): List<ServiceReagentConsumptionNorm>
    suspend fun getById(id: String): ServiceReagentConsumptionNorm
    suspend fun getByServiceName(serviceName: String): List<ServiceReagentConsumptionNorm>
    suspend fun getByCategory(category: String): List<ServiceReagentConsumptionNorm>
    suspend fun getByAnalyzer(analyzerId: String): List<ServiceReagentConsumptionNorm>
    suspend fun findByServiceAndReagent(serviceName: String, reagentName: String): ServiceReagentConsumptionNorm?
}

/**
 * Command service for managing service reagent consumption norms.
 */
interface ServiceReagentNormCommandService {
    suspend fun create(
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
    ): ServiceReagentConsumptionNorm

    suspend fun update(
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
    ): ServiceReagentConsumptionNorm

    suspend fun delete(id: String)
}

@Service
internal class ServiceReagentNormQueryServiceImpl(
    private val normRepository: ServiceReagentConsumptionNormRepository,
) : ServiceReagentNormQueryService {

    override suspend fun getAll(): List<ServiceReagentConsumptionNorm> {
        return normRepository.findAllByIsActiveTrueOrderByServiceNameAsc()
            .toList()
            .map { it.toModel() }
    }

    override suspend fun getById(id: String): ServiceReagentConsumptionNorm {
        return normRepository.findById(id)?.toModel()
            ?: throw ServiceReagentNormNotFoundException(id)
    }

    override suspend fun getByServiceName(serviceName: String): List<ServiceReagentConsumptionNorm> {
        val normalized = normalizeServiceName(serviceName)
        return normRepository.findAllByServiceNameNormalizedContainingIgnoreCaseAndIsActiveTrue(normalized)
            .toList()
            .map { it.toModel() }
    }

    override suspend fun getByCategory(category: String): List<ServiceReagentConsumptionNorm> {
        return normRepository.findAllByServiceCategoryOrderByServiceNameAsc(category)
            .toList()
            .map { it.toModel() }
    }

    override suspend fun getByAnalyzer(analyzerId: String): List<ServiceReagentConsumptionNorm> {
        return normRepository.findAllByAnalyzerIdOrderByServiceNameAsc(analyzerId)
            .toList()
            .map { it.toModel() }
    }

    override suspend fun findByServiceAndReagent(
        serviceName: String,
        reagentName: String,
    ): ServiceReagentConsumptionNorm? {
        val normalized = normalizeServiceName(serviceName)
        return normRepository.findByServiceNameNormalizedAndReagentNameAndIsActiveTrue(normalized, reagentName)?.toModel()
    }
}

@Service
internal class ServiceReagentNormCommandServiceImpl(
    private val normRepository: ServiceReagentConsumptionNormRepository,
) : ServiceReagentNormCommandService {

    override suspend fun create(
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
    ): ServiceReagentConsumptionNorm {
        validateQuantity(quantityPerService)

        val normalizedName = normalizeServiceName(serviceName)
        val detectedCategory = serviceCategory ?: detectCategoryFromServiceName(serviceName)

        val entity = ServiceReagentConsumptionNormEntity(
            id = UUID.randomUUID().toString(),
            serviceName = serviceName.trim(),
            serviceNameNormalized = normalizedName,
            serviceCategory = detectedCategory,
            analyzerId = analyzerId,
            reagentName = reagentName.trim(),
            consumableId = consumableId,
            quantityPerService = quantityPerService,
            unitType = parseUnitType(unitType),
            source = source,
            sourceDocument = sourceDocument?.trim(),
            notes = notes?.trim(),
            isActive = true,
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now(),
        )

        return normRepository.save(entity).toModel()
    }

    override suspend fun update(
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
    ): ServiceReagentConsumptionNorm {
        val existing = normRepository.findById(id)
            ?: throw ServiceReagentNormNotFoundException(id)

        quantityPerService?.let { validateQuantity(it) }

        val updatedName = serviceName?.trim()?.let { name ->
            if (name != existing.serviceName) normalizeServiceName(name) else existing.serviceNameNormalized
        } ?: existing.serviceNameNormalized

        val updatedServiceName = serviceName?.trim() ?: existing.serviceName
        val updatedCategory = serviceCategory ?: (if (serviceName != null) detectCategoryFromServiceName(updatedServiceName) else existing.serviceCategory)

        val updated = existing.copy(
            serviceName = updatedServiceName,
            serviceNameNormalized = updatedName,
            serviceCategory = updatedCategory,
            analyzerId = analyzerId ?: existing.analyzerId,
            reagentName = reagentName?.trim() ?: existing.reagentName,
            consumableId = consumableId ?: existing.consumableId,
            quantityPerService = quantityPerService ?: existing.quantityPerService,
            unitType = unitType?.let { parseUnitType(it) } ?: existing.unitType,
            source = source ?: existing.source,
            sourceDocument = sourceDocument?.trim() ?: existing.sourceDocument,
            notes = notes?.trim() ?: existing.notes,
            isActive = isActive ?: existing.isActive,
            updatedAt = LocalDateTime.now(),
        )

        return normRepository.save(updated).toModel()
    }

    override suspend fun delete(id: String) {
        normRepository.findById(id)?.let {
            normRepository.delete(it)
        } ?: throw ServiceReagentNormNotFoundException(id)
    }

    private fun validateQuantity(quantity: BigDecimal) {
        if (quantity <= BigDecimal.ZERO) {
            throw ServiceReagentNormValidationException("Quantity per service must be greater than zero")
        }
    }

    private fun parseUnitType(unitType: String) = try {
        lab.dev.med.univ.feature.reagents.domain.models.ReagentUnitType.valueOf(unitType.uppercase())
    } catch (e: IllegalArgumentException) {
        throw ServiceReagentNormValidationException("Invalid unit type: $unitType. Valid values: ML, PIECE, TEST_POSITION")
    }

    private fun detectCategoryFromServiceName(serviceName: String): String? {
        val normalized = serviceName.lowercase()
        return when {
            normalized.contains("гематолог") || normalized.contains("крови") ||
                normalized.contains("вск") || normalized.contains("рвс") -> "Гематология"
            normalized.contains("биохим") || normalized.contains("химия") -> "Биохимия"
            normalized.contains("коагул") || normalized.contains("протромбин") -> "Коагулология"
            normalized.contains("иммун") || normalized.contains("гормон") -> "Иммунология"
            normalized.contains("микробиол") || normalized.contains("посев") -> "Микробиология"
            normalized.contains("молекуляр") || normalized.contains("пцр") -> "Молекулярная_биология"
            else -> null
        }
    }
}

// =============================================================================
// Service-to-Analyzer Mapping Services
// =============================================================================

interface ServiceToAnalyzerMappingQueryService {
    suspend fun getAll(): List<ServiceToAnalyzerMapping>
    suspend fun getActive(): List<ServiceToAnalyzerMapping>
    suspend fun getByCategory(category: String): List<ServiceToAnalyzerMapping>
    suspend fun getByAnalyzer(analyzerId: String): List<ServiceToAnalyzerMapping>
    suspend fun findMatchingAnalyzer(serviceName: String, category: String? = null): ServiceToAnalyzerMapping?
}

interface ServiceToAnalyzerMappingCommandService {
    suspend fun create(
        serviceNamePattern: String,
        serviceCategory: String?,
        analyzerId: String,
        priority: Int,
    ): ServiceToAnalyzerMapping

    suspend fun update(
        id: String,
        serviceNamePattern: String?,
        serviceCategory: String?,
        analyzerId: String?,
        priority: Int?,
        isActive: Boolean?,
    ): ServiceToAnalyzerMapping

    suspend fun delete(id: String)
}

@Service
internal class ServiceToAnalyzerMappingQueryServiceImpl(
    private val mappingRepository: ServiceToAnalyzerMappingRepository,
) : ServiceToAnalyzerMappingQueryService {

    override suspend fun getAll(): List<ServiceToAnalyzerMapping> {
        return mappingRepository.findAllByIsActiveTrueOrderByMatchingPriorityAsc()
            .toList()
            .map { it.toModel() }
    }

    override suspend fun getActive(): List<ServiceToAnalyzerMapping> {
        return mappingRepository.findAllByIsActiveTrueOrderByMatchingPriorityAsc()
            .toList()
            .map { it.toModel() }
    }

    override suspend fun getByCategory(category: String): List<ServiceToAnalyzerMapping> {
        return mappingRepository.findAllByServiceCategoryOrderByMatchingPriorityAsc(category)
            .toList()
            .map { it.toModel() }
    }

    override suspend fun getByAnalyzer(analyzerId: String): List<ServiceToAnalyzerMapping> {
        return mappingRepository.findAllByAnalyzerIdOrderByMatchingPriorityAsc(analyzerId)
            .toList()
            .map { it.toModel() }
    }

    override suspend fun findMatchingAnalyzer(serviceName: String, category: String?): ServiceToAnalyzerMapping? {
        val mappings = mappingRepository.findAllByIsActiveTrueOrderByMatchingPriorityAsc().toList()

        // First try exact matches
        val exactMatch = mappings.firstOrNull { mapping ->
            mapping.serviceNamePattern.equals(serviceName, ignoreCase = true) &&
                (category == null || mapping.serviceCategory == category)
        }
        if (exactMatch != null) return exactMatch.toModel()

        // Then try pattern matches
        return mappings.map { it.toModel() }
            .firstOrNull { it.matches(serviceName) }
    }
}

@Service
internal class ServiceToAnalyzerMappingCommandServiceImpl(
    private val mappingRepository: ServiceToAnalyzerMappingRepository,
) : ServiceToAnalyzerMappingCommandService {

    override suspend fun create(
        serviceNamePattern: String,
        serviceCategory: String?,
        analyzerId: String,
        priority: Int,
    ): ServiceToAnalyzerMapping {
        validatePriority(priority)

        val entity = ServiceToAnalyzerMappingEntity(
            id = UUID.randomUUID().toString(),
            serviceNamePattern = serviceNamePattern.trim(),
            serviceCategory = serviceCategory,
            analyzerId = analyzerId,
            matchingPriority = priority,
            isActive = true,
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now(),
        )

        return mappingRepository.save(entity).toModel()
    }

    override suspend fun update(
        id: String,
        serviceNamePattern: String?,
        serviceCategory: String?,
        analyzerId: String?,
        priority: Int?,
        isActive: Boolean?,
    ): ServiceToAnalyzerMapping {
        val existing = mappingRepository.findById(id)
            ?: throw ServiceReagentNormNotFoundException(id)

        priority?.let { validatePriority(it) }

        val updated = existing.copy(
            serviceNamePattern = serviceNamePattern?.trim() ?: existing.serviceNamePattern,
            serviceCategory = serviceCategory ?: existing.serviceCategory,
            analyzerId = analyzerId ?: existing.analyzerId,
            matchingPriority = priority ?: existing.matchingPriority,
            isActive = isActive ?: existing.isActive,
            updatedAt = LocalDateTime.now(),
        )

        return mappingRepository.save(updated).toModel()
    }

    override suspend fun delete(id: String) {
        mappingRepository.findById(id)?.let {
            mappingRepository.delete(it)
        } ?: throw ServiceReagentNormNotFoundException(id)
    }

    private fun validatePriority(priority: Int) {
        if (priority <= 0) {
            throw ServiceReagentNormValidationException("Priority must be greater than zero (lower values = higher priority)")
        }
    }
}
