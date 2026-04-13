package lab.dev.med.univ.feature.reagents.presentation.dto

import lab.dev.med.univ.feature.reagents.domain.models.AnalyzerLogUpload
import lab.dev.med.univ.feature.reagents.domain.models.ParsedAnalyzerSample
import lab.dev.med.univ.feature.reagents.domain.models.SampleClassification
import lab.dev.med.univ.feature.reagents.domain.usecases.BatchAnalyzerLogResult
import java.time.LocalDateTime

data class AnalyzerLogUploadResponseDto(
    val id: String,
    val analyzerId: String?,
    val sourceType: String,
    val originalFileName: String,
    val fileSizeBytes: Long,
    val parseStatus: String,
    val parseStartedAt: LocalDateTime?,
    val parseCompletedAt: LocalDateTime?,
    val parseErrorMessage: String?,
    val totalLinesParsed: Int,
    val totalSamplesFound: Int,
    val legitimateSamples: Int,
    val unauthorizedSamples: Int,
    val washTestSamples: Int,
    val rerunSamples: Int,
    val logPeriodStart: LocalDateTime?,
    val logPeriodEnd: LocalDateTime?,
    val uploadedAt: LocalDateTime,
    val uploadedBy: String?,
)

data class ParsedAnalyzerSampleResponseDto(
    val id: String,
    val logUploadId: String,
    val analyzerId: String?,
    val sampleTimestamp: LocalDateTime,
    val barcode: String,
    val deviceSystemName: String?,
    val deviceName: String?,
    val lisAnalyzerId: Int?,
    val testMode: String?,
    val bloodMode: String?,
    val takeMode: String?,
    val orderResearchId: Long?,
    val orderId: Long?,
    val serviceId: Int?,
    val serviceName: String?,
    val hasLisOrder: Boolean,
    val sampleRequestCount: Int,
    val wbcValue: Double?,
    val rbcValue: Double?,
    val hgbValue: Double?,
    val pltValue: Double?,
    val classification: SampleClassification,
    val classificationReason: String?,
)

fun AnalyzerLogUpload.toResponseDto() = AnalyzerLogUploadResponseDto(
    id = id,
    analyzerId = analyzerId,
    sourceType = sourceType.name,
    originalFileName = originalFileName,
    fileSizeBytes = fileSizeBytes,
    parseStatus = parseStatus.name,
    parseStartedAt = parseStartedAt,
    parseCompletedAt = parseCompletedAt,
    parseErrorMessage = parseErrorMessage,
    totalLinesParsed = totalLinesParsed,
    totalSamplesFound = totalSamplesFound,
    legitimateSamples = legitimateSamples,
    unauthorizedSamples = unauthorizedSamples,
    washTestSamples = washTestSamples,
    rerunSamples = rerunSamples,
    logPeriodStart = logPeriodStart,
    logPeriodEnd = logPeriodEnd,
    uploadedAt = uploadedAt,
    uploadedBy = uploadedBy,
)

fun ParsedAnalyzerSample.toResponseDto() = ParsedAnalyzerSampleResponseDto(
    id = id,
    logUploadId = logUploadId,
    analyzerId = analyzerId,
    sampleTimestamp = sampleTimestamp,
    barcode = barcode,
    deviceSystemName = deviceSystemName,
    deviceName = deviceName,
    lisAnalyzerId = lisAnalyzerId,
    testMode = testMode,
    bloodMode = bloodMode,
    takeMode = takeMode,
    orderResearchId = orderResearchId,
    orderId = orderId,
    serviceId = serviceId,
    serviceName = serviceName,
    hasLisOrder = hasLisOrder,
    sampleRequestCount = sampleRequestCount,
    wbcValue = wbcValue,
    rbcValue = rbcValue,
    hgbValue = hgbValue,
    pltValue = pltValue,
    classification = classification,
    classificationReason = classificationReason,
)

data class BatchAnalyzerLogResultDto(
    val uploadId: String,
    val originalFileName: String,
    val success: Boolean,
    val errorMessage: String? = null,
    val upload: AnalyzerLogUploadResponseDto? = null,
)

fun BatchAnalyzerLogResult.toDto() = BatchAnalyzerLogResultDto(
    uploadId = uploadId,
    originalFileName = originalFileName,
    success = success,
    errorMessage = errorMessage,
    upload = upload?.toResponseDto(),
)
