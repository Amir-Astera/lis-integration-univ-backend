package lab.dev.med.univ.feature.reporting.data.entity

import lab.dev.med.univ.feature.reporting.domain.models.DamumedLabReportKind
import org.springframework.data.annotation.Id
import org.springframework.data.domain.Persistable
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import java.time.LocalDateTime

@Table("damumed_report_parsed_workbooks")
data class DamumedParsedWorkbookEntity(
    @Id
    val uploadId: String,
    val reportKind: DamumedLabReportKind,
    val workbookFormat: String,
    val sheetCount: Int,
    val activeSheetIndex: Int?,
    val firstVisibleSheetIndex: Int?,
    val parsedAt: LocalDateTime,
) : Persistable<String> {
    override fun getId(): String? = uploadId

    override fun isNew(): Boolean = true
}

@Table("damumed_report_parsed_sheets")
data class DamumedParsedSheetEntity(
    @Id
    @Column("id")
    val entityId: String,
    val uploadId: String,
    val sheetIndex: Int,
    val sheetName: String,
    val hidden: Boolean,
    val veryHidden: Boolean,
    val firstRowIndex: Int?,
    val lastRowIndex: Int?,
    val physicalRowCount: Int,
    val mergedRegionCount: Int,
    val defaultColumnWidth: Int?,
    val defaultRowHeight: Short?,
) : Persistable<String> {
    override fun getId(): String? = entityId

    override fun isNew(): Boolean = true
}

@Table("damumed_report_parsed_rows")
data class DamumedParsedRowEntity(
    @Id
    @Column("id")
    val entityId: String,
    val uploadId: String,
    val sheetId: String,
    val rowIndex: Int,
    val firstCellIndex: Int?,
    val lastCellIndex: Int?,
    val physicalCellCount: Int,
    val height: Short?,
    val zeroHeight: Boolean,
    val outlineLevel: Short?,
) : Persistable<String> {
    override fun getId(): String? = entityId

    override fun isNew(): Boolean = true
}

@Table("damumed_report_parsed_cells")
data class DamumedParsedCellEntity(
    @Id
    @Column("id")
    val entityId: String,
    val uploadId: String,
    val sheetId: String,
    val rowId: String,
    val rowIndex: Int,
    val columnIndex: Int,
    val cellReference: String,
    val cellType: String,
    val cachedFormulaResultType: String?,
    val rawValueText: String?,
    val formattedValueText: String?,
    val formulaText: String?,
    val numericValue: Double?,
    val booleanValue: Boolean?,
    val errorCode: Int?,
    val isDateFormatted: Boolean,
    val dateValue: LocalDateTime?,
    val styleIndex: Short?,
    val dataFormat: Short?,
    val dataFormatString: String?,
    val commentText: String?,
    val hyperlinkAddress: String?,
    val mergedRegionId: String?,
) : Persistable<String> {
    override fun getId(): String? = entityId

    override fun isNew(): Boolean = true
}

@Table("damumed_report_parsed_merged_regions")
data class DamumedParsedMergedRegionEntity(
    @Id
    @Column("id")
    val entityId: String,
    val uploadId: String,
    val sheetId: String,
    val regionIndex: Int,
    val firstRow: Int,
    val lastRow: Int,
    val firstColumn: Int,
    val lastColumn: Int,
    val firstCellReference: String,
    val lastCellReference: String,
) : Persistable<String> {
    override fun getId(): String? = entityId

    override fun isNew(): Boolean = true
}

