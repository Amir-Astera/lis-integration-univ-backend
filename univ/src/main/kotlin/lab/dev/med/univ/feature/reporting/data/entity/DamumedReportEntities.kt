package lab.dev.med.univ.feature.reporting.data.entity

import lab.dev.med.univ.feature.reporting.domain.models.DamumedLabReportKind
import lab.dev.med.univ.feature.reporting.domain.models.DamumedReportParseStatus
import lab.dev.med.univ.feature.reporting.domain.models.DamumedReportNormalizationStatus
import lab.dev.med.univ.feature.reporting.domain.models.DamumedReportSourceMode
import org.springframework.data.annotation.Id
import org.springframework.data.annotation.Version
import org.springframework.data.domain.Persistable
import org.springframework.data.relational.core.mapping.Table
import java.time.LocalDateTime

@Table("damumed_report_source_settings")
data class DamumedReportSourceSettingsEntity(
    @Id
    val id: String,
    val mode: DamumedReportSourceMode,
    val updatedAt: LocalDateTime = LocalDateTime.now(),
    val updatedBy: String? = null,
    @Version
    val version: Long? = null,
)

@Table("damumed_operational_overview_snapshots")
data class DamumedOperationalOverviewSnapshotEntity(
    @Id
    val entityId: String,
    val snapshotKey: String,
    val payloadJson: String,
    val sourceSignature: String,
    val generatedAt: LocalDateTime = LocalDateTime.now(),
    @Version
    val version: Long? = null,
) : Persistable<String> {
    override fun getId(): String? = entityId

    override fun isNew(): Boolean = version == null
}

@Table("damumed_report_uploads")
data class DamumedReportUploadEntity(
    @Id
    val id: String,
    val reportKind: DamumedLabReportKind,
    val sourceMode: DamumedReportSourceMode,
    val originalFileName: String,
    val storedFileName: String,
    val storagePath: String,
    val format: String,
    val contentType: String? = null,
    val checksumSha256: String,
    val sizeBytes: Long,
    val uploadedAt: LocalDateTime = LocalDateTime.now(),
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
    @Version
    val version: Long? = null,
)
