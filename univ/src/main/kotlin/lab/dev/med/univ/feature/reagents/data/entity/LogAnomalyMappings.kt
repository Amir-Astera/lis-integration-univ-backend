package lab.dev.med.univ.feature.reagents.data.entity

import lab.dev.med.univ.feature.reagents.domain.models.AnomalyType
import lab.dev.med.univ.feature.reagents.domain.models.CrossRefStatus
import lab.dev.med.univ.feature.reagents.domain.models.LogAnomalyDailySummary
import lab.dev.med.univ.feature.reagents.domain.models.LogAnomalyRecord

fun LogAnomalyRecordEntity.toModel() = LogAnomalyRecord(
    id = id,
    parsedSampleId = parsedSampleId,
    logUploadId = logUploadId,
    analyzerId = analyzerId,
    anomalyDate = anomalyDate,
    anomalyTimestamp = anomalyTimestamp,
    barcode = barcode,
    deviceSystemName = deviceSystemName,
    lisAnalyzerId = lisAnalyzerId,
    anomalyType = AnomalyType.valueOf(anomalyType),
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
    matchedDamumedFactId = matchedDamumedFactId,
    crossRefStatus = CrossRefStatus.valueOf(crossRefStatus),
    createdAt = createdAt,
)

fun LogAnomalyRecord.toEntity() = LogAnomalyRecordEntity(
    id = id,
    parsedSampleId = parsedSampleId,
    logUploadId = logUploadId,
    analyzerId = analyzerId,
    anomalyDate = anomalyDate,
    anomalyTimestamp = anomalyTimestamp,
    barcode = barcode,
    deviceSystemName = deviceSystemName,
    lisAnalyzerId = lisAnalyzerId,
    anomalyType = anomalyType.name,
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
    matchedDamumedFactId = matchedDamumedFactId,
    crossRefStatus = crossRefStatus.name,
    createdAt = createdAt,
    version = null,
)

fun LogAnomalyDailySummaryEntity.toModel() = LogAnomalyDailySummary(
    id = id,
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
    anomalyReagentsJson = anomalyReagentsJson,
    lastUpdatedAt = lastUpdatedAt,
)

fun LogAnomalyDailySummary.toEntity(version: Long? = null) = LogAnomalyDailySummaryEntity(
    id = id,
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
    anomalyReagentsJson = anomalyReagentsJson,
    lastUpdatedAt = lastUpdatedAt,
    version = version,
)
