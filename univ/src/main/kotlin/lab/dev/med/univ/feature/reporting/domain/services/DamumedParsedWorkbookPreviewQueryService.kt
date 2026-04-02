package lab.dev.med.univ.feature.reporting.domain.services

import kotlinx.coroutines.flow.toList
import lab.dev.med.univ.feature.reporting.data.repository.DamumedParsedCellRepository
import lab.dev.med.univ.feature.reporting.data.repository.DamumedParsedRowRepository
import lab.dev.med.univ.feature.reporting.data.repository.DamumedParsedSheetRepository
import lab.dev.med.univ.feature.reporting.data.repository.DamumedParsedWorkbookRepository
import lab.dev.med.univ.feature.reporting.data.repository.DamumedReportUploadRepository
import lab.dev.med.univ.feature.reporting.domain.errors.DamumedReportValidationException
import lab.dev.med.univ.feature.reporting.domain.models.DamumedParsedCellPreview
import lab.dev.med.univ.feature.reporting.domain.models.DamumedParsedRowPreview
import lab.dev.med.univ.feature.reporting.domain.models.DamumedParsedSheetPreview
import lab.dev.med.univ.feature.reporting.domain.models.DamumedParsedWorkbookPreview
import org.springframework.stereotype.Service

interface DamumedParsedWorkbookPreviewQueryService {
    suspend fun getPreview(uploadId: String, maxRowsPerSheet: Int = 25): DamumedParsedWorkbookPreview
}

@Service
class DamumedParsedWorkbookPreviewQueryServiceImpl(
    private val uploadRepository: DamumedReportUploadRepository,
    private val workbookRepository: DamumedParsedWorkbookRepository,
    private val sheetRepository: DamumedParsedSheetRepository,
    private val rowRepository: DamumedParsedRowRepository,
    private val cellRepository: DamumedParsedCellRepository,
) : DamumedParsedWorkbookPreviewQueryService {
    override suspend fun getPreview(uploadId: String, maxRowsPerSheet: Int): DamumedParsedWorkbookPreview {
        val normalizedMaxRowsPerSheet = maxRowsPerSheet.coerceAtLeast(1)
        val upload = uploadRepository.findById(uploadId)
            ?: throw DamumedReportValidationException("Report upload not found.")
        val workbook = workbookRepository.findById(uploadId)
            ?: throw DamumedReportValidationException("Parsed workbook not found for this upload.")
        val sheets = sheetRepository.findAllByUploadIdOrderBySheetIndexAsc(uploadId).toList()

        return DamumedParsedWorkbookPreview(
            uploadId = workbook.uploadId,
            reportKind = upload.reportKind,
            workbookFormat = workbook.workbookFormat,
            sheetCount = workbook.sheetCount,
            activeSheetIndex = workbook.activeSheetIndex,
            firstVisibleSheetIndex = workbook.firstVisibleSheetIndex,
            parsedAt = workbook.parsedAt,
            sheets = sheets.map { sheet ->
                val rows = rowRepository
                    .findAllBySheetIdOrderByRowIndexAsc(sheet.entityId)
                    .toList()
                    .take(normalizedMaxRowsPerSheet)
                val allowedRowIds = rows.map { it.entityId }.toSet()
                val cells = cellRepository
                    .findAllBySheetIdOrderByRowIndexAscColumnIndexAsc(sheet.entityId)
                    .toList()
                    .filter { it.rowId in allowedRowIds }
                    .groupBy { it.rowId }

                DamumedParsedSheetPreview(
                    id = sheet.entityId,
                    sheetIndex = sheet.sheetIndex,
                    sheetName = sheet.sheetName,
                    hidden = sheet.hidden,
                    veryHidden = sheet.veryHidden,
                    firstRowIndex = sheet.firstRowIndex,
                    lastRowIndex = sheet.lastRowIndex,
                    physicalRowCount = sheet.physicalRowCount,
                    mergedRegionCount = sheet.mergedRegionCount,
                    rows = rows.map { row ->
                        DamumedParsedRowPreview(
                            id = row.entityId,
                            rowIndex = row.rowIndex,
                            firstCellIndex = row.firstCellIndex,
                            lastCellIndex = row.lastCellIndex,
                            physicalCellCount = row.physicalCellCount,
                            zeroHeight = row.zeroHeight,
                            cells = cells[row.entityId].orEmpty().map { cell ->
                                DamumedParsedCellPreview(
                                    id = cell.entityId,
                                    rowIndex = cell.rowIndex,
                                    columnIndex = cell.columnIndex,
                                    cellReference = cell.cellReference,
                                    cellType = cell.cellType,
                                    rawValueText = cell.rawValueText,
                                    formattedValueText = cell.formattedValueText,
                                    formulaText = cell.formulaText,
                                    numericValue = cell.numericValue,
                                    booleanValue = cell.booleanValue,
                                    dateValue = cell.dateValue,
                                    mergedRegionId = cell.mergedRegionId,
                                )
                            },
                        )
                    },
                )
            },
        )
    }
}
