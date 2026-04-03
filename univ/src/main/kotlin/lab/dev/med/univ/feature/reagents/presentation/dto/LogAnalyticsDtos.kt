package lab.dev.med.univ.feature.reagents.presentation.dto

import lab.dev.med.univ.feature.reagents.domain.models.AnalyzerAnomalySummary
import lab.dev.med.univ.feature.reagents.domain.models.AnomalyReagentEntry
import lab.dev.med.univ.feature.reagents.domain.models.AnomalyServiceEntry
import lab.dev.med.univ.feature.reagents.domain.models.AnomalyType
import lab.dev.med.univ.feature.reagents.domain.models.CrossRefStatus
import lab.dev.med.univ.feature.reagents.domain.models.DamumedServiceStat
import lab.dev.med.univ.feature.reagents.domain.models.LegitimateServiceEntry
import lab.dev.med.univ.feature.reagents.domain.models.LogAnalyticsResult
import lab.dev.med.univ.feature.reagents.domain.models.LogAnomalyDailySummary
import lab.dev.med.univ.feature.reagents.domain.models.LogAnomalyRecord
import java.time.LocalDate
import java.time.LocalDateTime

data class LogAnomalyRecordDto(
    val id: String,
    val parsedSampleId: String?,
    val logUploadId: String,
    val analyzerId: String?,
    val anomalyDate: LocalDate,
    val anomalyTimestamp: LocalDateTime?,
    val barcode: String?,
    val deviceSystemName: String?,
    val lisAnalyzerId: Int?,
    val anomalyType: AnomalyType,
    val classificationReason: String?,
    val serviceId: Int?,
    val serviceName: String?,
    val serviceCategory: String?,
    val testMode: String?,
    val wbcValue: Double?,
    val rbcValue: Double?,
    val hgbValue: Double?,
    val pltValue: Double?,
    val estimatedReagentsJson: String?,
    val crossRefStatus: CrossRefStatus,
)

data class LogAnomalyDailySummaryDto(
    val summaryDate: LocalDate,
    val analyzerId: String?,
    val totalSamples: Int,
    val legitimateCount: Int,
    val anomalyCount: Int,
    val suspiciousCount: Int,
    val noLisOrderCount: Int,
    val errorCount: Int,
    val washTestCount: Int,
    val damumedCompletedCount: Int?,
)

data class AnalyzerAnomalySummaryDto(
    val analyzerId: String?,
    val analyzerName: String?,
    val anomalyCount: Int,
    val legitimateCount: Int,
    val totalSamples: Int,
    val washTestCount: Int,
    val damumedCompletedCount: Int?,
)

data class LegitimateServiceEntryDto(
    val serviceName: String,
    val serviceId: Int?,
    val analyzerId: String?,
    val analyzerName: String?,
    val count: Int,
    val uniquePatients: Int,
    val reagents: List<AnomalyReagentEntryDto>,
)

data class AnomalyReagentEntryDto(
    val reagentName: String,
    val unitType: String,
    val totalQuantity: Double,
    val analyzerId: String?,
    val analyzerName: String?,
)

data class AnomalyServiceEntryDto(
    val serviceName: String,
    val serviceCategory: String?,
    val anomalyCount: Int,
    val analyzerId: String?,
    val analyzerName: String?,
    val reagents: List<AnomalyReagentEntryDto>,
)

data class DamumedServiceStatDto(
    val serviceName: String,
    val completedCount: Int,
    val category: String?,
    val analyzerCount: Int,
)

data class LogAnalyticsResultDto(
    val periodFrom: LocalDate,
    val periodTo: LocalDate,
    val totalSamples: Int,
    val totalAnomalies: Int,
    val totalLegitimate: Int,
    val totalWashTests: Int,
    val uniquePatients: Int,
    val avgServicesPerPatient: Double,
    val damumedTotalCompleted: Int,
    val totalAnalyzerServiceCount: Int,
    val damumedServiceStats: List<DamumedServiceStatDto>,
    val byDay: List<LogAnomalyDailySummaryDto>,
    val byAnalyzer: List<AnalyzerAnomalySummaryDto>,
    val anomalyServices: List<AnomalyServiceEntryDto>,
    val legitimateServices: List<LegitimateServiceEntryDto>,
    val topReagents: List<AnomalyReagentEntryDto>,
    val topReagentsLegitimate: List<AnomalyReagentEntryDto>,
    val records: List<LogAnomalyRecordDto>,
)

fun LogAnomalyRecord.toDto() = LogAnomalyRecordDto(
    id = id,
    parsedSampleId = parsedSampleId,
    logUploadId = logUploadId,
    analyzerId = analyzerId,
    anomalyDate = anomalyDate,
    anomalyTimestamp = anomalyTimestamp,
    barcode = barcode,
    deviceSystemName = deviceSystemName,
    lisAnalyzerId = lisAnalyzerId,
    anomalyType = anomalyType,
    classificationReason = classificationReason,
    serviceId = serviceId,
    serviceName = serviceName,
    serviceCategory = serviceCategory,
    testMode = testMode,
    wbcValue = wbcValue,
    rbcValue = rbcValue,
    hgbValue = hgbValue,
    pltValue = pltValue,
    estimatedReagentsJson = estimatedReagentsJson,
    crossRefStatus = crossRefStatus,
)

fun LogAnomalyDailySummary.toDto() = LogAnomalyDailySummaryDto(
    summaryDate = summaryDate,
    analyzerId = analyzerId,
    totalSamples = totalSamples,
    legitimateCount = legitimateCount,
    anomalyCount = anomalyCount,
    suspiciousCount = suspiciousCount,
    noLisOrderCount = noLisOrderCount,
    errorCount = errorCount,
    washTestCount = washTestCount,
    damumedCompletedCount = damumedCompletedCount,
)

fun AnalyzerAnomalySummary.toDto() = AnalyzerAnomalySummaryDto(
    analyzerId = analyzerId,
    analyzerName = analyzerName,
    anomalyCount = anomalyCount,
    legitimateCount = legitimateCount,
    totalSamples = totalSamples,
    washTestCount = washTestCount,
    damumedCompletedCount = damumedCompletedCount,
)

fun LegitimateServiceEntry.toDto() = LegitimateServiceEntryDto(
    serviceName = serviceName,
    serviceId = serviceId,
    analyzerId = analyzerId,
    analyzerName = analyzerName,
    count = count,
    uniquePatients = uniquePatients,
    reagents = reagents.map { it.toDto() },
)

fun AnomalyReagentEntry.toDto() = AnomalyReagentEntryDto(
    reagentName = reagentName,
    unitType = unitType,
    totalQuantity = totalQuantity,
    analyzerId = analyzerId,
    analyzerName = analyzerName,
)

fun AnomalyServiceEntry.toDto() = AnomalyServiceEntryDto(
    serviceName = serviceName,
    serviceCategory = serviceCategory,
    anomalyCount = anomalyCount,
    analyzerId = analyzerId,
    analyzerName = analyzerName,
    reagents = reagents.map { it.toDto() },
)

fun DamumedServiceStat.toDto() = DamumedServiceStatDto(
    serviceName = serviceName,
    completedCount = completedCount,
    category = category,
    analyzerCount = analyzerCount,
)

fun LogAnalyticsResult.toDto() = LogAnalyticsResultDto(
    periodFrom = periodFrom,
    periodTo = periodTo,
    totalSamples = totalSamples,
    totalAnomalies = totalAnomalies,
    totalLegitimate = totalLegitimate,
    totalWashTests = totalWashTests,
    uniquePatients = uniquePatients,
    avgServicesPerPatient = avgServicesPerPatient,
    damumedTotalCompleted = damumedTotalCompleted,
    totalAnalyzerServiceCount = totalAnalyzerServiceCount,
    damumedServiceStats = damumedServiceStats.map { it.toDto() },
    byDay = byDay.map { it.toDto() },
    byAnalyzer = byAnalyzer.map { it.toDto() },
    anomalyServices = anomalyServices.map { it.toDto() },
    legitimateServices = legitimateServices.map { it.toDto() },
    topReagents = topReagents.map { it.toDto() },
    topReagentsLegitimate = topReagentsLegitimate.map { it.toDto() },
    records = records.map { it.toDto() },
)
