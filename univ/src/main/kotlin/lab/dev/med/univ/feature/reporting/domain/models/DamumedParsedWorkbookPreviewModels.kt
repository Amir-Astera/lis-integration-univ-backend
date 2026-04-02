package lab.dev.med.univ.feature.reporting.domain.models

import java.time.LocalDateTime

data class DamumedParsedWorkbookPreview(
    val uploadId: String,
    val reportKind: DamumedLabReportKind,
    val workbookFormat: String,
    val sheetCount: Int,
    val activeSheetIndex: Int?,
    val firstVisibleSheetIndex: Int?,
    val parsedAt: LocalDateTime,
    val sheets: List<DamumedParsedSheetPreview>,
)

data class DamumedParsedSheetPreview(
    val id: String,
    val sheetIndex: Int,
    val sheetName: String,
    val hidden: Boolean,
    val veryHidden: Boolean,
    val firstRowIndex: Int?,
    val lastRowIndex: Int?,
    val physicalRowCount: Int,
    val mergedRegionCount: Int,
    val rows: List<DamumedParsedRowPreview>,
)

data class DamumedParsedRowPreview(
    val id: String,
    val rowIndex: Int,
    val firstCellIndex: Int?,
    val lastCellIndex: Int?,
    val physicalCellCount: Int,
    val zeroHeight: Boolean,
    val cells: List<DamumedParsedCellPreview>,
)

data class DamumedParsedCellPreview(
    val id: String,
    val rowIndex: Int,
    val columnIndex: Int,
    val cellReference: String,
    val cellType: String,
    val rawValueText: String?,
    val formattedValueText: String?,
    val formulaText: String?,
    val numericValue: Double?,
    val booleanValue: Boolean?,
    val dateValue: LocalDateTime?,
    val mergedRegionId: String?,
)
