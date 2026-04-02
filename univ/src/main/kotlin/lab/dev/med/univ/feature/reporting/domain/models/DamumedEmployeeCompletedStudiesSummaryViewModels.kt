package lab.dev.med.univ.feature.reporting.domain.models

data class DamumedEmployeeCompletedStudiesSummaryProcessedView(
    val uploadId: String,
    val reportKind: DamumedLabReportKind,
    val normalizationStatus: DamumedReportNormalizationStatus,
    val normalizedSectionCount: Int,
    val normalizedFactCount: Int,
    val normalizedDimensionCount: Int,
    val periodText: String?,
    val bucketColumns: List<DamumedEmployeeCompletedStudiesBucketView>,
    val employees: List<DamumedEmployeeCompletedStudiesEmployeeView>,
    val summary: DamumedEmployeeCompletedStudiesSummaryTotalsView,
)

data class DamumedEmployeeCompletedStudiesBucketView(
    val key: String,
    val label: String,
    val metricKey: String,
    val metricLabel: String,
    val sourceColumnIndex: Int,
    val isTotal: Boolean,
)

data class DamumedEmployeeCompletedStudiesEmployeeView(
    val employee: String,
    val sectionId: String?,
    val sourceRowIndex: Int,
    val cells: List<DamumedEmployeeCompletedStudiesCellView>,
    val completedServiceCountTotal: Double,
    val completedPatientCountTotal: Double,
)

data class DamumedEmployeeCompletedStudiesCellView(
    val bucketKey: String,
    val metricKey: String,
    val numericValue: Double,
)

data class DamumedEmployeeCompletedStudiesSummaryTotalsView(
    val employeeCount: Int,
    val completedServiceCountTotal: Double,
    val completedPatientCountTotal: Double,
    val cells: List<DamumedEmployeeCompletedStudiesCellView>,
)
