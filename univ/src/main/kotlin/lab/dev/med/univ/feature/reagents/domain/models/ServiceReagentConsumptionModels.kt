package lab.dev.med.univ.feature.reagents.domain.models

import java.math.BigDecimal
import java.time.LocalDateTime

/**
 * Source type for service reagent consumption norms.
 */
enum class ServiceNormSource {
    MANUAL,                         // Manually entered by user
    CALCULATED_FROM_ANALYZER_RATE,  // Derived from analyzer reagent rates
    DOCUMENT_BASED,                 // Based on official documentation/methodology
}

/**
 * Confidence level for analyzer detection from service name.
 */
enum class DetectionConfidence {
    HIGH,       // Exact match or explicit keyword
    MEDIUM,     // Pattern match with supporting context
    LOW,        // Weak pattern match
    MANUAL,     // User-assigned
}

/**
 * Domain model for service reagent consumption norms.
 */
data class ServiceReagentConsumptionNorm(
    val id: String,
    val serviceName: String,
    val serviceNameNormalized: String,
    val serviceCategory: String? = null,
    val analyzerId: String? = null,
    val reagentName: String,
    val consumableId: String? = null,
    val quantityPerService: BigDecimal,
    val unitType: ReagentUnitType,
    val source: ServiceNormSource = ServiceNormSource.MANUAL,
    val sourceDocument: String? = null,
    val notes: String? = null,
    val isActive: Boolean = true,
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val updatedAt: LocalDateTime = LocalDateTime.now(),
    val version: Long? = null,
) {
    /**
     * Calculate total consumption for given number of service executions.
     */
    fun calculateTotalQuantity(completedCount: Int): BigDecimal {
        return quantityPerService.multiply(BigDecimal(completedCount))
    }
}

/**
 * Domain model for calculated reagent consumption from Damumed reports.
 */
data class DamumedReportReagentConsumption(
    val id: String,
    val uploadId: String,
    val factId: String? = null,
    val serviceName: String,
    val serviceCategory: String? = null,
    val completedCount: Int = 1,
    val consumptionEntries: List<ConsumptionEntry>,
    val totalEstimatedCostTenge: BigDecimal = BigDecimal.ZERO,
    val detectedAnalyzerId: String? = null,
    val detectionConfidence: DetectionConfidence? = null,
    val calculatedAt: LocalDateTime = LocalDateTime.now(),
    val calculatedBy: String? = null,
    val version: Long? = null,
)

/**
 * Individual consumption entry within a calculation.
 */
data class ConsumptionEntry(
    val reagentName: String,
    val quantity: BigDecimal,
    val unitType: ReagentUnitType,
    val unitCostTenge: BigDecimal? = null,
    val totalCostTenge: BigDecimal? = null,
    val sourceNormId: String? = null,  // Reference to the norm used
)

/**
 * Domain model for service-to-analyzer auto-mapping rules.
 */
data class ServiceToAnalyzerMapping(
    val id: String,
    val serviceNamePattern: String,
    val serviceCategory: String? = null,
    val analyzerId: String,
    val matchingPriority: Int = 100,
    val isActive: Boolean = true,
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val updatedAt: LocalDateTime = LocalDateTime.now(),
    val version: Long? = null,
) {
    /**
     * Check if given service name matches this pattern.
     * Supports wildcards: * (any sequence) and ? (single char)
     */
    fun matches(serviceName: String): Boolean {
        val regex = serviceNamePattern
            .lowercase()
            .replace("*", ".*")
            .replace("?", ".")
            .toRegex()
        return regex.matches(serviceName.lowercase())
    }
}

/**
 * Request to calculate reagent consumption for a Damumed report.
 */
data class CalculateDamumedConsumptionRequest(
    val uploadId: String,
    val serviceCategoryFilter: List<String>? = null,  // Optional: only specific categories
    val overrideAnalyzerMappings: Map<String, String>? = null,  // serviceName -> analyzerId overrides
)

/**
 * Result of consumption calculation.
 */
data class DamumedConsumptionCalculationResult(
    val uploadId: String,
    val totalServicesProcessed: Int,
    val totalConsumptionEntries: Int,
    val totalEstimatedCostTenge: BigDecimal,
    val byCategory: Map<String, CategorySummary>,
    val unmappedServices: List<String>,  // Service names that couldn't be matched
)

data class CategorySummary(
    val serviceCount: Int,
    val totalCostTenge: BigDecimal,
    val topReagents: List<ReagentSummary>,
)

data class ReagentSummary(
    val reagentName: String,
    val totalQuantity: BigDecimal,
    val unitType: ReagentUnitType,
    val totalCostTenge: BigDecimal? = null,
)
