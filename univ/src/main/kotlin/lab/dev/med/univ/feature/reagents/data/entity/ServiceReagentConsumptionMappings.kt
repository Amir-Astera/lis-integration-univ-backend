package lab.dev.med.univ.feature.reagents.data.entity

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import lab.dev.med.univ.feature.reagents.domain.models.ConsumptionEntry
import lab.dev.med.univ.feature.reagents.domain.models.DamumedReportReagentConsumption
import lab.dev.med.univ.feature.reagents.domain.models.DetectionConfidence
import lab.dev.med.univ.feature.reagents.domain.models.ServiceNormSource
import lab.dev.med.univ.feature.reagents.domain.models.ServiceReagentConsumptionNorm
import lab.dev.med.univ.feature.reagents.domain.models.ServiceToAnalyzerMapping
import java.math.BigDecimal

private val objectMapper = ObjectMapper().findAndRegisterModules()

// =============================================================================
// ServiceReagentConsumptionNorm mappings
// =============================================================================

fun ServiceReagentConsumptionNormEntity.toModel() = ServiceReagentConsumptionNorm(
    id = id,
    serviceName = serviceName,
    serviceNameNormalized = serviceNameNormalized,
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
    createdAt = createdAt,
    updatedAt = updatedAt,
    version = version,
)

fun ServiceReagentConsumptionNorm.toEntity() = ServiceReagentConsumptionNormEntity(
    id = id,
    serviceName = serviceName,
    serviceNameNormalized = serviceNameNormalized,
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
    createdAt = createdAt,
    updatedAt = updatedAt,
    version = version,
)

// =============================================================================
// DamumedReportReagentConsumption mappings (with JSON handling)
// =============================================================================

fun DamumedReportReagentConsumptionEntity.toModel(): DamumedReportReagentConsumption {
    val entries = try {
        objectMapper.readValue<List<ConsumptionEntry>>(consumptionJson)
    } catch (e: Exception) {
        emptyList()
    }

    return DamumedReportReagentConsumption(
        id = id,
        uploadId = uploadId,
        factId = factId,
        serviceName = serviceName,
        serviceCategory = serviceCategory,
        completedCount = completedCount,
        consumptionEntries = entries,
        totalEstimatedCostTenge = totalEstimatedCostTenge,
        detectedAnalyzerId = detectedAnalyzerId,
        detectionConfidence = detectionConfidence,
        calculatedAt = calculatedAt,
        calculatedBy = calculatedBy,
        version = version,
    )
}

fun DamumedReportReagentConsumption.toEntity(): DamumedReportReagentConsumptionEntity {
    val json = try {
        objectMapper.writeValueAsString(consumptionEntries)
    } catch (e: Exception) {
        "[]"
    }

    return DamumedReportReagentConsumptionEntity(
        id = id,
        uploadId = uploadId,
        factId = factId,
        serviceName = serviceName,
        serviceCategory = serviceCategory,
        completedCount = completedCount,
        consumptionJson = json,
        totalEstimatedCostTenge = totalEstimatedCostTenge,
        detectedAnalyzerId = detectedAnalyzerId,
        detectionConfidence = detectionConfidence,
        calculatedAt = calculatedAt,
        calculatedBy = calculatedBy,
        version = version,
    )
}

// =============================================================================
// ServiceToAnalyzerMapping mappings
// =============================================================================

fun ServiceToAnalyzerMappingEntity.toModel() = ServiceToAnalyzerMapping(
    id = id,
    serviceNamePattern = serviceNamePattern,
    serviceCategory = serviceCategory,
    analyzerId = analyzerId,
    matchingPriority = matchingPriority,
    isActive = isActive,
    createdAt = createdAt,
    updatedAt = updatedAt,
    version = version,
)

fun ServiceToAnalyzerMapping.toEntity() = ServiceToAnalyzerMappingEntity(
    id = id,
    serviceNamePattern = serviceNamePattern,
    serviceCategory = serviceCategory,
    analyzerId = analyzerId,
    matchingPriority = matchingPriority,
    isActive = isActive,
    createdAt = createdAt,
    updatedAt = updatedAt,
    version = version,
)

// =============================================================================
// Helper functions for text normalization
// =============================================================================

/**
 * Normalize service name for matching.
 * Lowercase, remove extra spaces, common punctuation.
 */
fun normalizeServiceName(name: String): String {
    return name
        .lowercase()
        .replace(Regex("\\s+"), " ")
        .replace(Regex("[.,;:!?]"), "")
        .replace(Regex("[\\u00A0]"), " ")  // Non-breaking space
        .trim()
}

/**
 * Detect service category from service name using keywords.
 */
fun detectServiceCategory(serviceName: String): String? {
    val normalized = serviceName.lowercase()
    return when {
        normalized.contains("гематолог") || normalized.contains("крови") ||
            normalized.contains("вск") || normalized.contains("рвс") ||
            normalized.contains("лейкоцит") || normalized.contains("тромбоцит") -> "Гематология"

        normalized.contains("биохим") || normalized.contains("химия") ||
            normalized.contains("глюкоз") || normalized.contains("холестерин") ||
            normalized.contains("белок") || normalized.contains("креатинин") ||
            normalized.contains("мочевин") || normalized.contains("билирубин") -> "Биохимия"

        normalized.contains("коагул") || normalized.contains("протромбин") ||
            normalized.contains("аптв") || normalized.contains("фибриноген") ||
            normalized.contains("д-димер") -> "Коагулология"

        normalized.contains("иммун") || normalized.contains("гормон") ||
            normalized.contains("антител") || normalized.contains("вич") ||
            normalized.contains("гепатит") || normalized.contains("сифилис") -> "Иммунология"

        normalized.contains("микробиол") || normalized.contains("посев") ||
            normalized.contains("бакпосев") || normalized.contains("чувствительность") -> "Микробиология"

        normalized.contains("молекуляр") || normalized.contains("пцр") ||
            normalized.contains("генет") -> "Молекулярная_биология"

        else -> null
    }
}
