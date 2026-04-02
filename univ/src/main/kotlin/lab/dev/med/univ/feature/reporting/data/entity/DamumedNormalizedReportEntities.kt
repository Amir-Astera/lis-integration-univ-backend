package lab.dev.med.univ.feature.reporting.data.entity

import lab.dev.med.univ.feature.reporting.domain.models.DamumedLabReportKind
import org.springframework.data.annotation.Id
import org.springframework.data.domain.Persistable
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table

@Table("damumed_report_normalized_sections")
data class DamumedNormalizedSectionEntity(
    @Id
    @Column("id")
    val entityId: String,
    val uploadId: String,
    val reportKind: DamumedLabReportKind,
    val sheetId: String,
    val sectionKey: String,
    val sectionName: String,
    val semanticRole: String,
    val anchorAxisKey: String?,
    val anchorAxisValue: String?,
    val rowStartIndex: Int?,
    val rowEndIndex: Int?,
    val columnStartIndex: Int?,
    val columnEndIndex: Int?,
) : Persistable<String> {
    override fun getId(): String? = entityId

    override fun isNew(): Boolean = true
}

@Table("damumed_report_normalized_dimensions")
data class DamumedNormalizedDimensionEntity(
    @Id
    @Column("id")
    val entityId: String,
    val uploadId: String,
    val reportKind: DamumedLabReportKind,
    val axisKey: String,
    val axisType: String,
    val rawValue: String,
    val normalizedValue: String,
    val displayValue: String,
    val sourceSheetId: String?,
    val sourceRowIndex: Int?,
    val sourceColumnIndex: Int?,
) : Persistable<String> {
    override fun getId(): String? = entityId

    override fun isNew(): Boolean = true
}

@Table("damumed_report_normalized_facts")
data class DamumedNormalizedFactEntity(
    @Id
    @Column("id")
    val entityId: String,
    val uploadId: String,
    val reportKind: DamumedLabReportKind,
    val sheetId: String,
    val sectionId: String,
    val rowId: String?,
    val cellId: String?,
    val metricKey: String,
    val metricLabel: String,
    val numericValue: Double?,
    val valueText: String?,
    val formulaText: String?,
    val periodText: String?,
    val sourceRowIndex: Int,
    val sourceColumnIndex: Int,
) : Persistable<String> {
    override fun getId(): String? = entityId

    override fun isNew(): Boolean = true
}

@Table("damumed_report_normalized_fact_dimensions")
data class DamumedNormalizedFactDimensionEntity(
    @Id
    @Column("id")
    val entityId: String,
    val factId: String,
    val axisKey: String,
    val dimensionId: String,
    val rawValue: String,
    val normalizedValue: String,
    val sourceScope: String,
) : Persistable<String> {
    override fun getId(): String? = entityId

    override fun isNew(): Boolean = true
}
