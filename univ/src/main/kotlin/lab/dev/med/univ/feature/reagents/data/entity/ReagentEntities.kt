package lab.dev.med.univ.feature.reagents.data.entity

import lab.dev.med.univ.feature.reagents.domain.models.AnalyzerLogParseStatus
import lab.dev.med.univ.feature.reagents.domain.models.AnalyzerLogSourceType
import lab.dev.med.univ.feature.reagents.domain.models.AnalyzerType
import lab.dev.med.univ.feature.reagents.domain.models.ConsumableCategory
import lab.dev.med.univ.feature.reagents.domain.models.ReagentInventoryStatus
import lab.dev.med.univ.feature.reagents.domain.models.ReagentOperationType
import lab.dev.med.univ.feature.reagents.domain.models.ReagentUnitType
import lab.dev.med.univ.feature.reagents.domain.models.SampleClassification
import lab.dev.med.univ.feature.reagents.domain.models.TubeColor
import org.springframework.data.annotation.Id
import org.springframework.data.annotation.Version
import org.springframework.data.relational.core.mapping.Table
import java.time.LocalDate
import java.time.LocalDateTime

@Table("analyzers")
data class AnalyzerEntity(
    @Id
    val id: String,
    val name: String,
    val type: AnalyzerType,
    val workplaceName: String,
    val lisDeviceSystemName: String? = null,
    val lisAnalyzerId: Int? = null,
    val lisDeviceName: String? = null,
    val serialNumber: String? = null,
    val isActive: Boolean = true,
    val notes: String? = null,
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val updatedAt: LocalDateTime = LocalDateTime.now(),
    @Version
    val version: Long? = null,
)

@Table("analyzer_reagent_rates")
data class AnalyzerReagentRateEntity(
    @Id
    val id: String,
    val analyzerId: String,
    val reagentName: String,
    val operationType: ReagentOperationType,
    val testMode: String? = null,
    val volumePerOperationMl: Double? = null,
    val unitsPerOperation: Int? = null,
    val unitType: ReagentUnitType,
    val sourceDocument: String? = null,
    val notes: String? = null,
    val createdAt: LocalDateTime = LocalDateTime.now(),
    @Version
    val version: Long? = null,
)

@Table("reagent_inventory")
data class ReagentInventoryEntity(
    @Id
    val id: String,
    val analyzerId: String? = null,
    val reagentName: String,
    val lotNumber: String? = null,
    val manufacturer: String? = null,
    val expiryDateSealed: LocalDate? = null,
    val stabilityDaysAfterOpening: Int? = null,
    val openedDate: LocalDate? = null,
    val totalVolumeMl: Double? = null,
    val totalUnits: Int? = null,
    val unitType: ReagentUnitType,
    val unitPriceTenge: Double? = null,
    val status: ReagentInventoryStatus = ReagentInventoryStatus.IN_STOCK,
    val receivedAt: LocalDate,
    val receivedBy: String? = null,
    val notes: String? = null,
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val updatedAt: LocalDateTime = LocalDateTime.now(),
    @Version
    val version: Long? = null,
)

@Table("consumable_inventory")
data class ConsumableInventoryEntity(
    @Id
    val id: String,
    val name: String,
    val category: ConsumableCategory,
    val tubeColor: TubeColor? = null,
    val linkedAnalyzerTypes: String? = null,
    val linkedServiceKeywords: String? = null,
    val quantityTotal: Int,
    val quantityRemaining: Int,
    val unitPriceTenge: Double? = null,
    val lotNumber: String? = null,
    val expiryDate: LocalDate? = null,
    val receivedAt: LocalDate,
    val receivedBy: String? = null,
    val notes: String? = null,
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val updatedAt: LocalDateTime = LocalDateTime.now(),
    @Version
    val version: Long? = null,
)

@Table("analyzer_log_uploads")
data class AnalyzerLogUploadEntity(
    @Id
    val id: String,
    val analyzerId: String? = null,
    val sourceType: AnalyzerLogSourceType,
    val originalFileName: String,
    val storedFileName: String,
    val storagePath: String,
    val fileSizeBytes: Long,
    val checksumSha256: String,
    val parseStatus: AnalyzerLogParseStatus = AnalyzerLogParseStatus.PENDING,
    val parseStartedAt: LocalDateTime? = null,
    val parseCompletedAt: LocalDateTime? = null,
    val parseErrorMessage: String? = null,
    val totalLinesParsed: Int = 0,
    val totalSamplesFound: Int = 0,
    val legitimateSamples: Int = 0,
    val unauthorizedSamples: Int = 0,
    val washTestSamples: Int = 0,
    val rerunSamples: Int = 0,
    val logPeriodStart: LocalDateTime? = null,
    val logPeriodEnd: LocalDateTime? = null,
    val uploadedAt: LocalDateTime = LocalDateTime.now(),
    val uploadedBy: String? = null,
    @Version
    val version: Long? = null,
)

@Table("parsed_analyzer_samples")
data class ParsedAnalyzerSampleEntity(
    @Id
    val id: String,
    val logUploadId: String,
    val analyzerId: String? = null,
    val sampleTimestamp: LocalDateTime,
    val barcode: String,
    val deviceSystemName: String? = null,
    val deviceName: String? = null,
    val lisAnalyzerId: Int? = null,
    val testMode: String? = null,
    val bloodMode: String? = null,
    val takeMode: String? = null,
    val orderResearchId: Long? = null,
    val orderId: Long? = null,
    val serviceId: Int? = null,
    val serviceName: String? = null,
    val hasLisOrder: Boolean,
    val sampleRequestCount: Int = 0,
    val wbcValue: Double? = null,
    val rbcValue: Double? = null,
    val hgbValue: Double? = null,
    val pltValue: Double? = null,
    val classification: SampleClassification,
    val classificationReason: String? = null,
    val correlatedLegitimateSampleId: String? = null,
    val estimatedDiluentMl: Double? = null,
    val estimatedDiffLyseMl: Double? = null,
    val estimatedLhLyseMl: Double? = null,
    val estimatedCostTenge: Double? = null,
    val createdAt: LocalDateTime = LocalDateTime.now(),
    @Version
    val version: Long? = null,
)

@Table("reagent_consumption_reports")
data class ReagentConsumptionReportEntity(
    @Id
    val id: String,
    val analyzerId: String,
    val periodStart: LocalDate,
    val periodEnd: LocalDate,
    val legitimateTestCount: Int = 0,
    val legitimateReagentConsumptionJson: String? = null,
    val legitimateCostTenge: Double = 0.0,
    val serviceOperationsJson: String? = null,
    val serviceReagentConsumptionJson: String? = null,
    val serviceCostTenge: Double = 0.0,
    val suspiciousTestCount: Int = 0,
    val rerunTestCount: Int = 0,
    val washTestCount: Int = 0,
    val unauthorizedReagentConsumptionJson: String? = null,
    val unauthorizedCostTenge: Double = 0.0,
    val inventoryStartJson: String? = null,
    val inventoryReceivedJson: String? = null,
    val inventoryEndExpectedJson: String? = null,
    val inventoryEndActualJson: String? = null,
    val discrepancyJson: String? = null,
    val discrepancyTotalTenge: Double = 0.0,
    val generatedAt: LocalDateTime = LocalDateTime.now(),
    val generatedBy: String? = null,
    @Version
    val version: Long? = null,
)
