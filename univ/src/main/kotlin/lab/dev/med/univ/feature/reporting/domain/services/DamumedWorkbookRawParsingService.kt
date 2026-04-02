package lab.dev.med.univ.feature.reporting.domain.services

import lab.dev.med.univ.feature.reporting.data.entity.DamumedParsedCellEntity
import lab.dev.med.univ.feature.reporting.data.entity.DamumedParsedMergedRegionEntity
import lab.dev.med.univ.feature.reporting.data.entity.DamumedParsedRowEntity
import lab.dev.med.univ.feature.reporting.data.entity.DamumedParsedSheetEntity
import lab.dev.med.univ.feature.reporting.data.entity.DamumedParsedWorkbookEntity
import lab.dev.med.univ.feature.reporting.data.entity.toEntity
import lab.dev.med.univ.feature.reporting.data.entity.toModel
import lab.dev.med.univ.feature.reporting.data.repository.DamumedParsedCellRepository
import lab.dev.med.univ.feature.reporting.data.repository.DamumedParsedMergedRegionRepository
import lab.dev.med.univ.feature.reporting.data.repository.DamumedParsedRowRepository
import lab.dev.med.univ.feature.reporting.data.repository.DamumedParsedSheetRepository
import lab.dev.med.univ.feature.reporting.data.repository.DamumedParsedWorkbookRepository
import lab.dev.med.univ.feature.reporting.data.repository.DamumedReportUploadRepository
import lab.dev.med.univ.feature.reporting.domain.models.DamumedLabReportKind
import lab.dev.med.univ.feature.reporting.domain.models.DamumedReportParseStatus
import lab.dev.med.univ.feature.reporting.domain.models.DamumedReportUpload
import org.apache.poi.hssf.usermodel.HSSFWorkbook
import org.apache.poi.ss.usermodel.Cell
import org.apache.poi.ss.usermodel.CellType
import org.apache.poi.ss.usermodel.DataFormatter
import org.apache.poi.ss.usermodel.DateUtil
import org.apache.poi.ss.usermodel.FormulaEvaluator
import org.apache.poi.ss.usermodel.Sheet
import org.apache.poi.ss.usermodel.Workbook
import org.apache.poi.ss.util.CellReference
import org.springframework.stereotype.Service
import java.time.LocalDateTime
import java.time.ZoneId

interface DamumedWorkbookRawParsingService {
    suspend fun parseAndPersist(upload: DamumedReportUpload, workbook: Workbook): DamumedReportUpload
}

@Service
class DamumedWorkbookRawParsingServiceImpl(
    private val uploadRepository: DamumedReportUploadRepository,
    private val workbookRepository: DamumedParsedWorkbookRepository,
    private val sheetRepository: DamumedParsedSheetRepository,
    private val rowRepository: DamumedParsedRowRepository,
    private val cellRepository: DamumedParsedCellRepository,
    private val mergedRegionRepository: DamumedParsedMergedRegionRepository,
    private val workplaceCompletedStudiesProcessingService: DamumedWorkplaceCompletedStudiesProcessingService,
    private val workbookNormalizationService: DamumedWorkbookNormalizationService,
) : DamumedWorkbookRawParsingService {
    private val dataFormatter = DataFormatter(true)

    override suspend fun parseAndPersist(upload: DamumedReportUpload, workbook: Workbook): DamumedReportUpload {
        cleanupExistingParsedData(upload.id)

        val started = upload.copy(
            parseStatus = DamumedReportParseStatus.PROCESSING,
            parseStartedAt = LocalDateTime.now(),
            parseCompletedAt = null,
            parseErrorMessage = null,
        ).persistUpload()

        return try {
            val evaluator = workbook.creationHelper.createFormulaEvaluator()
            val parsedAt = LocalDateTime.now()
            val workbookEntity = DamumedParsedWorkbookEntity(
                uploadId = upload.id,
                reportKind = upload.reportKind,
                workbookFormat = if (workbook is HSSFWorkbook) "xls" else "xlsx",
                sheetCount = workbook.numberOfSheets,
                activeSheetIndex = workbook.activeSheetIndex,
                firstVisibleSheetIndex = workbook.firstVisibleTab,
                parsedAt = parsedAt,
            )
            workbookRepository.save(workbookEntity)

            var parsedSheetCount = 0
            var parsedRowCount = 0
            var parsedCellCount = 0
            var parsedMergedRegionCount = 0
            val titleCandidates = mutableListOf<String>()
            val periodCandidates = mutableListOf<String>()

            repeat(workbook.numberOfSheets) { sheetIndex ->
                val sheet = workbook.getSheetAt(sheetIndex)
                val sheetId = "${upload.id}:sheet:$sheetIndex"
                val sheetEntity = DamumedParsedSheetEntity(
                    entityId = sheetId,
                    uploadId = upload.id,
                    sheetIndex = sheetIndex,
                    sheetName = sheet.sheetName,
                    hidden = workbook.isSheetHidden(sheetIndex),
                    veryHidden = workbook.isSheetVeryHidden(sheetIndex),
                    firstRowIndex = sheet.firstRowNum.takeIf { it >= 0 },
                    lastRowIndex = sheet.lastRowNum.takeIf { it >= 0 },
                    physicalRowCount = sheet.physicalNumberOfRows,
                    mergedRegionCount = sheet.numMergedRegions,
                    defaultColumnWidth = sheet.defaultColumnWidth,
                    defaultRowHeight = sheet.defaultRowHeight,
                )
                sheetRepository.save(sheetEntity)
                parsedSheetCount += 1

                repeat(sheet.numMergedRegions) { regionIndex ->
                    val region = sheet.getMergedRegion(regionIndex)
                    val regionId = "${sheetId}:merge:$regionIndex"
                    mergedRegionRepository.save(
                        DamumedParsedMergedRegionEntity(
                            entityId = regionId,
                            uploadId = upload.id,
                            sheetId = sheetId,
                            regionIndex = regionIndex,
                            firstRow = region.firstRow,
                            lastRow = region.lastRow,
                            firstColumn = region.firstColumn,
                            lastColumn = region.lastColumn,
                            firstCellReference = CellReference(region.firstRow, region.firstColumn).formatAsString(),
                            lastCellReference = CellReference(region.lastRow, region.lastColumn).formatAsString(),
                        ),
                    )
                    parsedMergedRegionCount += 1
                }

                sheet.forEach { row ->
                    val rowId = "${sheetId}:row:${row.rowNum}"
                    val firstCellIndex = row.firstCellNum.takeIf { it >= 0 }?.toInt()
                    val lastCellExclusive = row.lastCellNum.takeIf { it >= 0 }?.toInt()
                    rowRepository.save(
                        DamumedParsedRowEntity(
                            entityId = rowId,
                            uploadId = upload.id,
                            sheetId = sheetId,
                            rowIndex = row.rowNum,
                            firstCellIndex = firstCellIndex,
                            lastCellIndex = lastCellExclusive,
                            physicalCellCount = row.physicalNumberOfCells,
                            height = row.height,
                            zeroHeight = row.zeroHeight,
                            outlineLevel = row.outlineLevel.toShort(),
                        ),
                    )
                    parsedRowCount += 1

                    if (firstCellIndex != null && lastCellExclusive != null) {
                        for (columnIndex in firstCellIndex until lastCellExclusive) {
                            val entity = row.getCell(columnIndex, org.apache.poi.ss.usermodel.Row.MissingCellPolicy.RETURN_BLANK_AS_NULL)
                                .toEntity(upload.id, sheet, rowId, evaluator, columnIndex)
                            cellRepository.save(entity)
                            parsedCellCount += 1

                            val normalized = entity.formattedValueText?.trim().orEmpty()
                            if (titleCandidates.size < 10 && row.rowNum <= 6 && normalized.isNotBlank()) {
                                titleCandidates += normalized
                            }
                            if (periodCandidates.size < 10 && normalized.contains("период", ignoreCase = true)) {
                                periodCandidates += normalized
                            }
                        }
                    }
                }
            }

            val parsedUpload = started.copy(
                parseStatus = DamumedReportParseStatus.PARSED,
                parseCompletedAt = LocalDateTime.now(),
                parsedSheetCount = parsedSheetCount,
                parsedRowCount = parsedRowCount,
                parsedCellCount = parsedCellCount,
                parsedMergedRegionCount = parsedMergedRegionCount,
                detectedReportTitle = detectTitle(titleCandidates),
                detectedPeriodText = periodCandidates.firstOrNull(),
            ).persistUpload()
            when (parsedUpload.reportKind) {
                DamumedLabReportKind.WORKPLACE_COMPLETED_STUDIES -> {
                    workplaceCompletedStudiesProcessingService.processParsedUpload(parsedUpload, workbook)
                }

                else -> workbookNormalizationService.normalize(parsedUpload)
            }
        } catch (ex: Exception) {
            cleanupExistingParsedData(upload.id)
            started.copy(
                parseStatus = DamumedReportParseStatus.FAILED,
                parseCompletedAt = LocalDateTime.now(),
                parseErrorMessage = ex.message?.take(4000),
            ).persistUpload()
        }
    }

    private suspend fun DamumedReportUpload.persistUpload(): DamumedReportUpload {
        return uploadRepository.save(this.toEntity()).let { it.toModel() }
    }

    private suspend fun cleanupExistingParsedData(uploadId: String) {
        cellRepository.deleteAllByUploadId(uploadId)
        rowRepository.deleteAllByUploadId(uploadId)
        mergedRegionRepository.deleteAllByUploadId(uploadId)
        sheetRepository.deleteAllByUploadId(uploadId)
        workbookRepository.deleteById(uploadId)
    }

    private fun Cell?.toEntity(
        uploadId: String,
        sheet: Sheet,
        rowId: String,
        evaluator: FormulaEvaluator,
        columnIndexOverride: Int,
    ): DamumedParsedCellEntity {
        if (this == null) {
            val rowIndex = rowId.substringAfterLast(':').toInt()
            val sheetId = rowId.substringBeforeLast(":row:")
            return DamumedParsedCellEntity(
                entityId = "$rowId:cell:$columnIndexOverride",
                uploadId = uploadId,
                sheetId = sheetId,
                rowId = rowId,
                rowIndex = rowIndex,
                columnIndex = columnIndexOverride,
                cellReference = CellReference(rowIndex, columnIndexOverride).formatAsString(),
                cellType = "MISSING",
                cachedFormulaResultType = null,
                rawValueText = null,
                formattedValueText = null,
                formulaText = null,
                numericValue = null,
                booleanValue = null,
                errorCode = null,
                isDateFormatted = false,
                dateValue = null,
                styleIndex = null,
                dataFormat = null,
                dataFormatString = null,
                commentText = null,
                hyperlinkAddress = null,
                mergedRegionId = findMergedRegionId(sheet, rowIndex, columnIndexOverride, sheetId),
            )
        }

        val sheetId = rowId.substringBeforeLast(":row:")
        val evaluatedType = if (cellType == CellType.FORMULA) {
            runCatching { evaluator.evaluateFormulaCell(this) }.getOrElse { cachedFormulaResultType }
        } else {
            null
        }
        val style = cellStyle
        val isDate = runCatching { DateUtil.isCellDateFormatted(this) }.getOrDefault(false)
        val formatted = runCatching { dataFormatter.formatCellValue(this, evaluator) }.getOrElse { dataFormatter.formatCellValue(this) }
        val commentText = cellComment?.string?.string
        val hyperlinkAddress = hyperlink?.address
        val mergedRegionId = findMergedRegionId(sheet, rowIndex, columnIndex, sheetId)

        return DamumedParsedCellEntity(
            entityId = "$rowId:cell:$columnIndex",
            uploadId = uploadId,
            sheetId = sheetId,
            rowId = rowId,
            rowIndex = rowIndex,
            columnIndex = columnIndex,
            cellReference = CellReference(rowIndex, columnIndex).formatAsString(),
            cellType = cellType.name,
            cachedFormulaResultType = evaluatedType?.name,
            rawValueText = rawValueText(evaluatedType),
            formattedValueText = formatted.takeIf { it.isNotBlank() },
            formulaText = if (cellType == CellType.FORMULA) cellFormula else null,
            numericValue = numericValue(evaluatedType),
            booleanValue = booleanValue(evaluatedType),
            errorCode = errorCodeValue(evaluatedType),
            isDateFormatted = isDate,
            dateValue = dateValue(isDate),
            styleIndex = style?.index,
            dataFormat = style?.dataFormat,
            dataFormatString = style?.dataFormatString,
            commentText = commentText,
            hyperlinkAddress = hyperlinkAddress,
            mergedRegionId = mergedRegionId,
        )
    }

    private fun findMergedRegionId(sheet: Sheet, rowIndex: Int, columnIndex: Int, sheetId: String): String? {
        repeat(sheet.numMergedRegions) { regionIndex ->
            val region = sheet.getMergedRegion(regionIndex)
            if (rowIndex in region.firstRow..region.lastRow && columnIndex in region.firstColumn..region.lastColumn) {
                return "$sheetId:merge:$regionIndex"
            }
        }
        return null
    }

    private fun Cell.rawValueText(evaluatedType: CellType?): String? {
        return when (cellType) {
            CellType.STRING -> stringCellValue
            CellType.NUMERIC -> numericCellValue.toString()
            CellType.BOOLEAN -> booleanCellValue.toString()
            CellType.ERROR -> errorCellValue.toString()
            CellType.BLANK -> null
            CellType.FORMULA -> when (evaluatedType) {
                CellType.STRING -> runCatching { stringCellValue }.getOrNull()
                CellType.NUMERIC -> runCatching { numericCellValue.toString() }.getOrNull()
                CellType.BOOLEAN -> runCatching { booleanCellValue.toString() }.getOrNull()
                CellType.ERROR -> runCatching { errorCellValue.toString() }.getOrNull()
                else -> null
            }
            else -> null
        }
    }

    private fun Cell.numericValue(evaluatedType: CellType?): Double? {
        return when {
            cellType == CellType.NUMERIC -> numericCellValue
            cellType == CellType.FORMULA && evaluatedType == CellType.NUMERIC -> runCatching { numericCellValue }.getOrNull()
            else -> null
        }
    }

    private fun Cell.booleanValue(evaluatedType: CellType?): Boolean? {
        return when {
            cellType == CellType.BOOLEAN -> booleanCellValue
            cellType == CellType.FORMULA && evaluatedType == CellType.BOOLEAN -> runCatching { booleanCellValue }.getOrNull()
            else -> null
        }
    }

    private fun Cell.errorCodeValue(evaluatedType: CellType?): Int? {
        return when {
            cellType == CellType.ERROR -> errorCellValue.toInt()
            cellType == CellType.FORMULA && evaluatedType == CellType.ERROR -> runCatching { errorCellValue.toInt() }.getOrNull()
            else -> null
        }
    }

    private fun Cell.dateValue(isDate: Boolean): LocalDateTime? {
        if (!isDate) {
            return null
        }
        return runCatching {
            localDateTimeCellValue
        }.getOrElse {
            dateCellValue.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime()
        }
    }

    private fun detectTitle(titleCandidates: List<String>): String? {
        return titleCandidates
            .firstOrNull { candidate ->
                candidate.length > 6 && !candidate.contains("период", ignoreCase = true)
            }
            ?.take(1000)
    }
}
