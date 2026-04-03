package lab.dev.med.univ.feature.reagents.domain.models

import java.time.LocalDate
import java.time.LocalDateTime

enum class AnomalyType {
    NO_LIS_ORDER,   // Sample ran without any LIS registration
    SUSPICIOUS,     // Explicit "THERE IS NO SR" from driver
    ERROR,          // Error-level log event for sample
    XML_RESULT,     // Appeared in errors.xml (had results but no LIS flow)
}

enum class CrossRefStatus {
    NOT_CHECKED,
    NO_MATCH,
    MATCHED,
    PARTIAL_MATCH,
}

data class LogAnomalyRecord(
    val id: String,
    val parsedSampleId: String? = null,
    val logUploadId: String,
    val analyzerId: String? = null,
    val anomalyDate: LocalDate,
    val anomalyTimestamp: LocalDateTime? = null,
    val barcode: String? = null,
    val deviceSystemName: String? = null,
    val lisAnalyzerId: Int? = null,
    val anomalyType: AnomalyType,
    val classificationReason: String? = null,
    val serviceId: Int? = null,
    val serviceName: String? = null,
    val serviceCategory: String? = null,
    val testMode: String? = null,
    val wbcValue: Double? = null,
    val rbcValue: Double? = null,
    val hgbValue: Double? = null,
    val pltValue: Double? = null,
    val estimatedReagentsJson: String? = null,
    val matchedDamumedFactId: String? = null,
    val crossRefStatus: CrossRefStatus = CrossRefStatus.NOT_CHECKED,
    val createdAt: LocalDateTime = LocalDateTime.now(),
)

data class LogAnomalyDailySummary(
    val id: String,
    val summaryDate: LocalDate,
    val analyzerId: String? = null,
    val totalSamples: Int = 0,
    val legitimateCount: Int = 0,
    val anomalyCount: Int = 0,
    val suspiciousCount: Int = 0,
    val noLisOrderCount: Int = 0,
    val errorCount: Int = 0,
    val washTestCount: Int = 0,
    val damumedCompletedCount: Int? = null,
    val anomalyReagentsJson: String? = null,
    val lastUpdatedAt: LocalDateTime = LocalDateTime.now(),
)

enum class AnalyticsPeriod {
    DAY, WEEK, MONTH;

    fun toDays(): Long = when (this) {
        DAY -> 1L
        WEEK -> 7L
        MONTH -> 30L
    }
}

// DTO views used by the analytics service layer

data class AnomalyReagentEntry(
    val reagentName: String,
    val unitType: String,
    val totalQuantity: Double,
    val analyzerId: String? = null,
    val analyzerName: String? = null,
)

data class AnomalyServiceEntry(
    val serviceName: String,
    val serviceCategory: String?,
    val anomalyCount: Int,
    val legitimateCount: Int = 0,
    val totalCount: Int = 0,
    val analyzerId: String?,
    val analyzerName: String?,
    val reagents: List<AnomalyReagentEntry>,
    val reagentsLegitimate: List<AnomalyReagentEntry> = emptyList(),
)

data class AnalyzerAnomalySummary(
    val analyzerId: String?,
    val analyzerName: String?,
    val anomalyCount: Int,
    val legitimateCount: Int,
    val totalSamples: Int = 0,
    val washTestCount: Int = 0,
    val damumedCompletedCount: Int?,
)

data class LegitimateServiceEntry(
    val serviceName: String,
    val serviceId: Int?,
    val analyzerId: String?,
    val analyzerName: String?,
    val count: Int,
    val uniquePatients: Int = 0,
    val reagents: List<AnomalyReagentEntry> = emptyList(),
)

/** Aggregated service fact from Damumed LIS report */
data class DamumedServiceStat(
    val serviceName: String,
    val completedCount: Int,
    val category: String? = null,
    val analyzerCount: Int = 0,
)

data class LogAnalyticsResult(
    val periodFrom: LocalDate,
    val periodTo: LocalDate,
    val totalSamples: Int,
    val totalAnomalies: Int,
    val totalLegitimate: Int,
    val totalWashTests: Int = 0,
    val uniquePatients: Int = 0,
    val avgServicesPerPatient: Double = 0.0,
    /** Total completed services reported by the LIS (Damumed) — taken from the latest normalized report */
    val damumedTotalCompleted: Int = 0,
    /** Total service-bearing samples found in analyzer logs for the period */
    val totalAnalyzerServiceCount: Int = 0,
    /** Per-service breakdown from the latest Damumed LIS report */
    val damumedServiceStats: List<DamumedServiceStat> = emptyList(),
    val byDay: List<LogAnomalyDailySummary>,
    val byAnalyzer: List<AnalyzerAnomalySummary>,
    val anomalyServices: List<AnomalyServiceEntry>,
    val legitimateServices: List<LegitimateServiceEntry> = emptyList(),
    val topReagents: List<AnomalyReagentEntry>,
    val topReagentsLegitimate: List<AnomalyReagentEntry> = emptyList(),
    val records: List<LogAnomalyRecord>,
)
