package lab.dev.med.univ.feature.reporting.domain.models

data class DamumedReferralCountByMaterialProcessedView(
    val uploadId: String,
    val reportKind: DamumedLabReportKind,
    val normalizationStatus: DamumedReportNormalizationStatus,
    val normalizedSectionCount: Int,
    val normalizedFactCount: Int,
    val normalizedDimensionCount: Int,
    val periodText: String?,
    val bucketColumns: List<DamumedReferralCountByMaterialBucketView>,
    val materials: List<DamumedReferralCountByMaterialRowView>,
    val summary: DamumedReferralCountByMaterialSummaryView,
)

data class DamumedReferralCountByMaterialBucketView(
    val key: String,
    val label: String,
    val sourceColumnIndex: Int,
    val isTotal: Boolean,
)

data class DamumedReferralCountByMaterialRowView(
    val material: String,
    val sectionId: String?,
    val sourceRowIndex: Int,
    val cells: List<DamumedReferralCountByMaterialCellView>,
    val rowTotal: Double,
)

data class DamumedReferralCountByMaterialCellView(
    val bucketKey: String,
    val label: String,
    val numericValue: Double,
)

data class DamumedReferralCountByMaterialSummaryView(
    val materialCount: Int,
    val cells: List<DamumedReferralCountByMaterialCellView>,
    val grandTotal: Double,
)
