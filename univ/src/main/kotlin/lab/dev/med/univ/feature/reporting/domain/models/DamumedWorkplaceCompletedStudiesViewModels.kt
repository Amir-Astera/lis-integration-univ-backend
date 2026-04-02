package lab.dev.med.univ.feature.reporting.domain.models

data class DamumedWorkplaceCompletedStudiesProcessedView(
    val uploadId: String,
    val reportKind: DamumedLabReportKind,
    val normalizationStatus: DamumedReportNormalizationStatus,
    val normalizedSectionCount: Int,
    val normalizedFactCount: Int,
    val normalizedDimensionCount: Int,
    val periodText: String?,
    val departmentColumns: List<DamumedWorkplaceCompletedStudiesColumnView>,
    val workplaces: List<DamumedWorkplaceCompletedStudiesWorkplaceView>,
    val summary: DamumedWorkplaceCompletedStudiesSummaryView,
)

data class DamumedWorkplaceCompletedStudiesColumnView(
    val key: String,
    val departmentGroup: String?,
    val department: String?,
    val displayLabel: String,
    val metricKey: String,
    val isTotal: Boolean,
    val sourceColumnIndex: Int,
)

data class DamumedWorkplaceCompletedStudiesWorkplaceView(
    val workplace: String,
    val sectionId: String?,
    val rowStartIndex: Int?,
    val rowEndIndex: Int?,
    val services: List<DamumedWorkplaceCompletedStudiesServiceView>,
    val summary: DamumedWorkplaceCompletedStudiesSummaryView,
)

data class DamumedWorkplaceCompletedStudiesServiceView(
    val service: String,
    val sourceRowIndex: Int,
    val cells: List<DamumedWorkplaceCompletedStudiesCellView>,
    val completedValueTotal: Double,
    val reportedTotalValueTotal: Double,
)

data class DamumedWorkplaceCompletedStudiesCellView(
    val columnKey: String,
    val metricKey: String,
    val numericValue: Double,
)

data class DamumedWorkplaceCompletedStudiesSummaryView(
    val serviceCount: Int,
    val completedValueTotal: Double,
    val reportedTotalValueTotal: Double,
    val cells: List<DamumedWorkplaceCompletedStudiesCellView>,
)
