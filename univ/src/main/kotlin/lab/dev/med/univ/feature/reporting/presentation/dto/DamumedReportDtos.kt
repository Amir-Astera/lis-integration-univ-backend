package lab.dev.med.univ.feature.reporting.presentation.dto

import lab.dev.med.univ.feature.reporting.domain.models.DamumedLabReportKind
import lab.dev.med.univ.feature.reporting.domain.models.DamumedReportParseStatus
import lab.dev.med.univ.feature.reporting.domain.models.DamumedReportNormalizationStatus
import lab.dev.med.univ.feature.reporting.domain.models.DamumedReportSourceMode
import lab.dev.med.univ.feature.reporting.domain.models.DamumedReportSourceSettings
import lab.dev.med.univ.feature.reporting.domain.models.DamumedReportUpload
import java.time.LocalDateTime

data class UpdateDamumedReportSourceModeDto(
    val mode: DamumedReportSourceMode,
)

data class DamumedReportSourceSettingsResponseDto(
    val mode: DamumedReportSourceMode,
    val updatedAt: LocalDateTime,
    val updatedBy: String? = null,
)

data class DamumedReportKindOptionDto(
    val code: DamumedLabReportKind,
    val displayName: String,
)

data class DamumedReportUploadResponseDto(
    val id: String,
    val reportKind: DamumedLabReportKind,
    val reportDisplayName: String,
    val sourceMode: DamumedReportSourceMode,
    val originalFileName: String,
    val storedFileName: String,
    val storagePath: String,
    val format: String,
    val contentType: String? = null,
    val checksumSha256: String,
    val sizeBytes: Long,
    val uploadedAt: LocalDateTime,
    val uploadedBy: String? = null,
    val parseStatus: DamumedReportParseStatus,
    val parseStartedAt: LocalDateTime? = null,
    val parseCompletedAt: LocalDateTime? = null,
    val parseErrorMessage: String? = null,
    val parsedSheetCount: Int,
    val parsedRowCount: Int,
    val parsedCellCount: Int,
    val parsedMergedRegionCount: Int,
    val detectedReportTitle: String? = null,
    val detectedPeriodText: String? = null,
    val normalizationStatus: DamumedReportNormalizationStatus,
    val normalizationStartedAt: LocalDateTime? = null,
    val normalizationCompletedAt: LocalDateTime? = null,
    val normalizationErrorMessage: String? = null,
    val normalizedSectionCount: Int,
    val normalizedFactCount: Int,
    val normalizedDimensionCount: Int,
)

fun DamumedReportSourceSettings.toResponseDto() = DamumedReportSourceSettingsResponseDto(
    mode = mode,
    updatedAt = updatedAt,
    updatedBy = updatedBy,
)

fun DamumedLabReportKind.toOptionDto() = DamumedReportKindOptionDto(
    code = this,
    displayName = displayName,
)

fun DamumedReportUpload.toResponseDto() = DamumedReportUploadResponseDto(
    id = id,
    reportKind = reportKind,
    reportDisplayName = reportKind.displayName,
    sourceMode = sourceMode,
    originalFileName = originalFileName,
    storedFileName = storedFileName,
    storagePath = storagePath,
    format = format,
    contentType = contentType,
    checksumSha256 = checksumSha256,
    sizeBytes = sizeBytes,
    uploadedAt = uploadedAt,
    uploadedBy = uploadedBy,
    parseStatus = parseStatus,
    parseStartedAt = parseStartedAt,
    parseCompletedAt = parseCompletedAt,
    parseErrorMessage = parseErrorMessage,
    parsedSheetCount = parsedSheetCount,
    parsedRowCount = parsedRowCount,
    parsedCellCount = parsedCellCount,
    parsedMergedRegionCount = parsedMergedRegionCount,
    detectedReportTitle = detectedReportTitle,
    detectedPeriodText = detectedPeriodText,
    normalizationStatus = normalizationStatus,
    normalizationStartedAt = normalizationStartedAt,
    normalizationCompletedAt = normalizationCompletedAt,
    normalizationErrorMessage = normalizationErrorMessage,
    normalizedSectionCount = normalizedSectionCount,
    normalizedFactCount = normalizedFactCount,
    normalizedDimensionCount = normalizedDimensionCount,
)
