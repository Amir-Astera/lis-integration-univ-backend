package lab.dev.med.univ.feature.reagents.data.entity

import lab.dev.med.univ.feature.reagents.domain.models.DetectionConfidence
import lab.dev.med.univ.feature.reagents.domain.models.ReagentUnitType
import lab.dev.med.univ.feature.reagents.domain.models.ServiceNormSource
import org.springframework.data.annotation.Id
import org.springframework.data.annotation.Version
import org.springframework.data.relational.core.mapping.Table
import java.math.BigDecimal
import java.time.LocalDateTime

/**
 * Entity for service-to-reagent consumption norms.
 * Defines how much reagent/consumable is used per laboratory service execution.
 */
@Table("service_reagent_consumption_norms")
data class ServiceReagentConsumptionNormEntity(
    @Id
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
    @Version
    val version: Long? = null,
)

/**
 * Entity for calculated reagent consumption from Damumed reports.
 * Stores per-fact or aggregated consumption calculations.
 */
@Table("damumed_report_reagent_consumption")
data class DamumedReportReagentConsumptionEntity(
    @Id
    val id: String,
    val uploadId: String,
    val factId: String? = null,
    val serviceName: String,
    val serviceCategory: String? = null,
    val completedCount: Int = 1,
    val consumptionJson: String,
    val totalEstimatedCostTenge: BigDecimal = BigDecimal.ZERO,
    val detectedAnalyzerId: String? = null,
    val detectionConfidence: DetectionConfidence? = null,
    val calculatedAt: LocalDateTime = LocalDateTime.now(),
    val calculatedBy: String? = null,
    @Version
    val version: Long? = null,
)

/**
 * Entity for service-to-analyzer auto-mapping rules.
 * Helps detect which analyzer performs a given service.
 */
@Table("service_to_analyzer_mappings")
data class ServiceToAnalyzerMappingEntity(
    @Id
    val id: String,
    val serviceNamePattern: String,
    val serviceCategory: String? = null,
    val analyzerId: String,
    val matchingPriority: Int = 100,
    val isActive: Boolean = true,
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val updatedAt: LocalDateTime = LocalDateTime.now(),
    @Version
    val version: Long? = null,
)
