package lab.dev.med.univ.feature.reagents.data.entity

import org.springframework.data.annotation.Id
import org.springframework.data.annotation.Version
import org.springframework.data.relational.core.mapping.Table
import java.time.LocalDate
import java.time.LocalDateTime

@Table("log_anomaly_records")
data class LogAnomalyRecordEntity(
    @Id
    val id: String,
    val parsedSampleId: String? = null,
    val logUploadId: String,
    val analyzerId: String? = null,
    val anomalyDate: LocalDate,
    val anomalyTimestamp: LocalDateTime? = null,
    val barcode: String? = null,
    val deviceSystemName: String? = null,
    val lisAnalyzerId: Int? = null,
    val anomalyType: String,
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
    val crossRefStatus: String = "NOT_CHECKED",
    val createdAt: LocalDateTime = LocalDateTime.now(),
    @Version
    val version: Long? = null,
)

@Table("log_anomaly_daily_summary")
data class LogAnomalyDailySummaryEntity(
    @Id
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
    @Version
    val version: Long? = null,
)
