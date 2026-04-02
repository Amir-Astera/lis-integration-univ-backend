package lab.dev.med.univ.feature.reagents.presentation.dto

import lab.dev.med.univ.feature.reagents.domain.models.CategorySummary
import lab.dev.med.univ.feature.reagents.domain.models.DamumedConsumptionCalculationResult
import lab.dev.med.univ.feature.reagents.domain.models.DetectionConfidence
import lab.dev.med.univ.feature.reagents.domain.models.ReagentSummary
import lab.dev.med.univ.feature.reagents.domain.models.ServiceNormSource
import lab.dev.med.univ.feature.reagents.domain.models.ServiceReagentConsumptionNorm
import lab.dev.med.univ.feature.reagents.domain.models.ServiceToAnalyzerMapping
import java.math.BigDecimal

// =============================================================================
// DTOs for Service Reagent Consumption Norms
// =============================================================================

data class ServiceReagentNormDto(
    val id: String,
    val serviceName: String,
    val serviceCategory: String?,
    val analyzerId: String?,
    val reagentName: String,
    val consumableId: String?,
    val quantityPerService: String,
    val unitType: String,
    val source: String,
    val sourceDocument: String?,
    val notes: String?,
    val isActive: Boolean,
    val createdAt: String,
    val updatedAt: String,
)

data class CreateServiceReagentNormRequestDto(
    val serviceName: String,
    val serviceCategory: String? = null,
    val analyzerId: String? = null,
    val reagentName: String,
    val consumableId: String? = null,
    val quantityPerService: String,
    val unitType: String,
    val source: ServiceNormSource? = ServiceNormSource.MANUAL,
    val sourceDocument: String? = null,
    val notes: String? = null,
)

// =============================================================================
// DTOs for Service-to-Analyzer Mappings
// =============================================================================

data class ServiceToAnalyzerMappingDto(
    val id: String,
    val serviceNamePattern: String,
    val serviceCategory: String?,
    val analyzerId: String,
    val matchingPriority: Int,
    val isActive: Boolean,
    val createdAt: String,
    val updatedAt: String,
)

data class CreateServiceToAnalyzerMappingRequestDto(
    val serviceNamePattern: String,
    val serviceCategory: String? = null,
    val analyzerId: String,
    val priority: Int? = 100,
)

// =============================================================================
// DTOs for Damumed Consumption Calculation
// =============================================================================

data class CalculateDamumedConsumptionRequestDto(
    val uploadId: String,
    val serviceCategoryFilter: List<String>? = null,
    val overrideAnalyzerMappings: Map<String, String>? = null,
)

data class DamumedConsumptionCalculationResultDto(
    val uploadId: String,
    val totalServicesProcessed: Int,
    val totalConsumptionEntries: Int,
    val totalEstimatedCostTenge: String,
    val byCategory: Map<String, CategorySummaryDto>,
    val unmappedServices: List<String>,
)

data class CategorySummaryDto(
    val serviceCount: Int,
    val totalCostTenge: String,
    val topReagents: List<ReagentSummaryDto>,
)

data class ReagentSummaryDto(
    val reagentName: String,
    val totalQuantity: String,
    val unitType: String,
    val totalCostTenge: String?,
)

// =============================================================================
// Mapping Functions - MUST be defined before usage
// =============================================================================

fun CategorySummary.toDto() = CategorySummaryDto(
    serviceCount = serviceCount,
    totalCostTenge = totalCostTenge.toString(),
    topReagents = topReagents.map { it.toDto() },
)

fun ReagentSummary.toDto() = ReagentSummaryDto(
    reagentName = reagentName,
    totalQuantity = totalQuantity.toString(),
    unitType = unitType.name,
    totalCostTenge = totalCostTenge?.toString(),
)

fun DamumedConsumptionCalculationResult.toDto() = DamumedConsumptionCalculationResultDto(
    uploadId = uploadId,
    totalServicesProcessed = totalServicesProcessed,
    totalConsumptionEntries = totalConsumptionEntries,
    totalEstimatedCostTenge = totalEstimatedCostTenge.toString(),
    byCategory = byCategory.mapValues { it.value.toDto() },
    unmappedServices = unmappedServices,
)

fun ServiceReagentConsumptionNorm.toDto() = ServiceReagentNormDto(
    id = id,
    serviceName = serviceName,
    serviceCategory = serviceCategory,
    analyzerId = analyzerId,
    reagentName = reagentName,
    consumableId = consumableId,
    quantityPerService = quantityPerService.toString(),
    unitType = unitType.name,
    source = source.name,
    sourceDocument = sourceDocument,
    notes = notes,
    isActive = isActive,
    createdAt = createdAt.toString(),
    updatedAt = updatedAt.toString(),
)

fun ServiceToAnalyzerMapping.toDto() = ServiceToAnalyzerMappingDto(
    id = id,
    serviceNamePattern = serviceNamePattern,
    serviceCategory = serviceCategory,
    analyzerId = analyzerId,
    matchingPriority = matchingPriority,
    isActive = isActive,
    createdAt = createdAt.toString(),
    updatedAt = updatedAt.toString(),
)
