package lab.dev.med.univ.feature.reporting.domain.models

data class DamumedNormalizedReportPreview(
    val uploadId: String,
    val reportKind: DamumedLabReportKind,
    val normalizationStatus: DamumedReportNormalizationStatus,
    val normalizedSectionCount: Int,
    val normalizedFactCount: Int,
    val normalizedDimensionCount: Int,
    val sections: List<DamumedNormalizedSectionPreview>,
    val dimensions: List<DamumedNormalizedDimensionPreview>,
    val facts: List<DamumedNormalizedFactPreview>,
)

data class DamumedNormalizedSectionPreview(
    val id: String,
    val sheetId: String,
    val sectionKey: String,
    val sectionName: String,
    val semanticRole: String,
    val anchorAxisKey: String?,
    val anchorAxisValue: String?,
    val rowStartIndex: Int?,
    val rowEndIndex: Int?,
)

data class DamumedNormalizedDimensionPreview(
    val id: String,
    val axisKey: String,
    val axisType: String,
    val rawValue: String,
    val normalizedValue: String,
    val displayValue: String,
)

data class DamumedNormalizedFactPreview(
    val id: String,
    val sheetId: String,
    val sectionId: String,
    val metricKey: String,
    val metricLabel: String,
    val numericValue: Double?,
    val valueText: String?,
    val formulaText: String?,
    val periodText: String?,
    val sourceRowIndex: Int,
    val sourceColumnIndex: Int,
    val dimensions: List<DamumedNormalizedFactDimensionPreview>,
)

data class DamumedNormalizedFactDimensionPreview(
    val axisKey: String,
    val dimensionId: String,
    val rawValue: String,
    val normalizedValue: String,
    val sourceScope: String,
)
