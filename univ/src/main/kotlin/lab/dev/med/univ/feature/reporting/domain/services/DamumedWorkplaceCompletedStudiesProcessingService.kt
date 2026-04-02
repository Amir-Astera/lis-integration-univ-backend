package lab.dev.med.univ.feature.reporting.domain.services

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.withContext
import lab.dev.med.univ.feature.reporting.data.entity.DamumedNormalizedDimensionEntity
import lab.dev.med.univ.feature.reporting.data.entity.DamumedNormalizedFactDimensionEntity
import lab.dev.med.univ.feature.reporting.data.entity.DamumedNormalizedFactEntity
import lab.dev.med.univ.feature.reporting.data.entity.DamumedNormalizedSectionEntity
import lab.dev.med.univ.feature.reporting.data.entity.toEntity
import lab.dev.med.univ.feature.reporting.data.entity.toModel
import lab.dev.med.univ.feature.reporting.data.repository.DamumedNormalizedDimensionRepository
import lab.dev.med.univ.feature.reporting.data.repository.DamumedNormalizedFactDimensionRepository
import lab.dev.med.univ.feature.reporting.data.repository.DamumedNormalizedFactRepository
import lab.dev.med.univ.feature.reporting.data.repository.DamumedNormalizedSectionRepository
import lab.dev.med.univ.feature.reporting.data.repository.DamumedReportUploadRepository
import lab.dev.med.univ.feature.reporting.domain.errors.DamumedReportValidationException
import lab.dev.med.univ.feature.reporting.domain.models.DamumedLabReportKind
import lab.dev.med.univ.feature.reporting.domain.models.DamumedReportAxisProfile
import lab.dev.med.univ.feature.reporting.domain.models.DamumedReportAxisType
import lab.dev.med.univ.feature.reporting.domain.models.DamumedReportMetricProfile
import lab.dev.med.univ.feature.reporting.domain.models.DamumedReportNormalizationProfile
import lab.dev.med.univ.feature.reporting.domain.models.DamumedReportNormalizationStatus
import lab.dev.med.univ.feature.reporting.domain.models.DamumedReportSchemaCatalog
import lab.dev.med.univ.feature.reporting.domain.models.DamumedReportUpload
import org.apache.poi.ss.usermodel.Cell
import org.apache.poi.ss.usermodel.CellType
import org.apache.poi.ss.usermodel.DataFormatter
import org.apache.poi.ss.usermodel.FormulaEvaluator
import org.apache.poi.ss.usermodel.Row
import org.apache.poi.ss.usermodel.Sheet
import org.apache.poi.ss.usermodel.Workbook
import org.apache.poi.ss.usermodel.WorkbookFactory
import org.apache.poi.ss.util.CellRangeAddress
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import project.gigienist_reports.core.concurrency.ExcelLimiter
import java.nio.file.Files
import java.nio.file.Path
import java.time.LocalDateTime

interface DamumedWorkplaceCompletedStudiesProcessingService {
    suspend fun processParsedUpload(upload: DamumedReportUpload, workbook: Workbook): DamumedReportUpload

    suspend fun normalizeExistingUpload(upload: DamumedReportUpload): DamumedReportUpload
}

@Service
class DamumedWorkplaceCompletedStudiesProcessingServiceImpl(
    private val location: Path,
    private val excelLimiter: ExcelLimiter,
    private val uploadRepository: DamumedReportUploadRepository,
    private val normalizedSectionRepository: DamumedNormalizedSectionRepository,
    private val normalizedDimensionRepository: DamumedNormalizedDimensionRepository,
    private val normalizedFactRepository: DamumedNormalizedFactRepository,
    private val normalizedFactDimensionRepository: DamumedNormalizedFactDimensionRepository,
) : DamumedWorkplaceCompletedStudiesProcessingService {
    private val logger = LoggerFactory.getLogger(javaClass)
    private val dataFormatter = DataFormatter(true)

    override suspend fun processParsedUpload(upload: DamumedReportUpload, workbook: Workbook): DamumedReportUpload {
        if (upload.reportKind != DamumedLabReportKind.WORKPLACE_COMPLETED_STUDIES) {
            throw DamumedReportValidationException("Unsupported report kind for workplace direct processing.")
        }

        val started = upload.copy(
            normalizationStatus = DamumedReportNormalizationStatus.PROCESSING,
            normalizationStartedAt = LocalDateTime.now(),
            normalizationCompletedAt = null,
            normalizationErrorMessage = null,
            normalizedSectionCount = 0,
            normalizedFactCount = 0,
            normalizedDimensionCount = 0,
        ).persistUpload()

        return try {
            cleanupExistingNormalizedData(upload.id)
            val profile = DamumedReportSchemaCatalog.profileFor(upload.reportKind)
            val counters = parseAndPersist(started, workbook, profile)
            started.copy(
                normalizationStatus = DamumedReportNormalizationStatus.NORMALIZED,
                normalizationCompletedAt = LocalDateTime.now(),
                normalizedSectionCount = counters.sectionCount,
                normalizedFactCount = counters.factCount,
                normalizedDimensionCount = counters.dimensionCount,
            ).persistUpload()
        } catch (ex: Exception) {
            cleanupExistingNormalizedData(upload.id)
            started.copy(
                normalizationStatus = DamumedReportNormalizationStatus.FAILED,
                normalizationCompletedAt = LocalDateTime.now(),
                normalizationErrorMessage = ex.message?.take(4000),
            ).persistUpload()
        }
    }

    override suspend fun normalizeExistingUpload(upload: DamumedReportUpload): DamumedReportUpload {
        return excelLimiter.withPermit {
            withContext(Dispatchers.IO) {
                val workbookPath = resolveWorkbookPath(upload)
                if (!Files.exists(workbookPath)) {
                    throw DamumedReportValidationException("Stored report file not found.")
                }
                Files.newInputStream(workbookPath).use { input ->
                    WorkbookFactory.create(input).use { workbook ->
                        processParsedUpload(upload, workbook)
                    }
                }
            }
        }
    }

    private suspend fun parseAndPersist(
        upload: DamumedReportUpload,
        workbook: Workbook,
        profile: DamumedReportNormalizationProfile,
    ): NormalizationCounters {
        val evaluator = workbook.creationHelper.createFormulaEvaluator()
        val periodText = upload.detectedPeriodText ?: detectPeriodText(workbook, evaluator)
        val completedMetric = profile.metrics.firstOrNull { !it.representsTotal } ?: profile.metrics.first()
        val totalMetric = profile.metrics.firstOrNull { it.representsTotal } ?: completedMetric
        val aggregatedRecords = linkedMapOf<String, WorkplaceFactAccumulator>()
        val processedCells = mutableSetOf<String>()
        val processedFacts = mutableSetOf<String>()
        var foundBlockHeader = false

        repeat(workbook.numberOfSheets) { sheetIndex ->
            val sheet = workbook.getSheetAt(sheetIndex)
            val sheetId = "${upload.id}:sheet:$sheetIndex"
            val view = MergedAwareSheetView(sheet, evaluator)
            val compositeBlocks = detectCompositeBlocks(view)
            if (compositeBlocks.isNotEmpty()) {
                foundBlockHeader = true
            }
            compositeBlocks.forEachIndexed { index, compositeBlock ->
                val nextBlockStart = compositeBlocks.getOrNull(index + 1)?.firstHeaderRow ?: (view.maxRowIndex + 1)
                val dataStartRow = findDataStartRowAfter(view, compositeBlock.lastHeaderRow, nextBlockStart, compositeBlock.serviceColumnIndex)
                    ?: return@forEachIndexed
                val columns = buildColumnDefinitions(view, compositeBlock, dataStartRow)
                logger.info(
                    "Workplace report block detected: sheet={}, headerRows={}-{}, dataStartRow={}, columns={}, totalColumns={}, departmentColumns={}, groups={}, departments={}",
                    view.sheet.sheetName,
                    compositeBlock.firstHeaderRow,
                    compositeBlock.lastHeaderRow,
                    dataStartRow,
                    columns.size,
                    columns.count { it.isTotal },
                    columns.count { !it.isTotal },
                    columns.mapNotNull { it.departmentGroup }.distinct(),
                    columns.mapNotNull { it.department }.distinct().take(12),
                )
                if (columns.isEmpty()) {
                    return@forEachIndexed
                }
                parseBlockRows(
                    view = view,
                    sheetId = sheetId,
                    workplaceColumnIndex = compositeBlock.workplaceColumnIndex,
                    serviceColumnIndex = compositeBlock.serviceColumnIndex,
                    dataStartRow = dataStartRow,
                    endExclusiveRow = nextBlockStart,
                    columns = columns,
                    completedMetric = completedMetric,
                    totalMetric = totalMetric,
                    periodText = periodText,
                    accumulator = aggregatedRecords,
                    processedCells = processedCells,
                    processedFacts = processedFacts,
                )
            }
        }

        if (!foundBlockHeader) {
            throw DamumedReportValidationException("Workplace report headers were not detected in the workbook.")
        }

        if (aggregatedRecords.isEmpty()) {
            return NormalizationCounters(sectionCount = 0, factCount = 0, dimensionCount = 0)
        }

        return persistAggregatedRecords(upload, profile, aggregatedRecords.values.toList())
    }

    /**
     * Groups consecutive block header rows (within MAX_HEADER_GAP rows) into a single CompositeBlock.
     * This handles the common XLS layout where:
     *   Row N:   Рабочее место | Услуга | [Амбулатория merged] | [Стационар merged] | Всего
     *   Row N+1: Рабочее место | Услуга | отд1 | отд2 | ...    | отд_x | ...        |
     * Both rows are header rows but belong to the same logical block.
     */
    private fun detectCompositeBlocks(view: MergedAwareSheetView): List<CompositeBlock> {
        val rawHeaders = mutableListOf<BlockHeader>()
        for (rowIndex in 0..view.maxRowIndex) {
            findBlockHeader(view, rowIndex)?.let { rawHeaders += it }
        }
        if (rawHeaders.isEmpty()) return emptyList()

        val composites = mutableListOf<CompositeBlock>()
        var i = 0
        while (i < rawHeaders.size) {
            val first = rawHeaders[i]
            var lastRow = first.rowIndex
            var j = i + 1
            // Absorb consecutive header rows that are close together (part of same multi-row header)
            while (j < rawHeaders.size && rawHeaders[j].rowIndex - lastRow <= MAX_HEADER_GAP) {
                lastRow = rawHeaders[j].rowIndex
                j++
            }
            composites += CompositeBlock(
                firstHeaderRow = first.rowIndex,
                lastHeaderRow = lastRow,
                workplaceColumnIndex = first.workplaceColumnIndex,
                serviceColumnIndex = first.serviceColumnIndex,
            )
            i = j
        }
        return composites
    }

    private fun findBlockHeader(view: MergedAwareSheetView, rowIndex: Int): BlockHeader? {
        for (columnIndex in 0 until view.maxColumnIndex) {
            val current = normalizeText(view.textAt(rowIndex, columnIndex))
            val next = normalizeText(view.textAt(rowIndex, columnIndex + 1))
            if (current == "рабочее место" && next == "услуга") {
                val previousCurrent = if (rowIndex > 0) normalizeText(view.textAt(rowIndex - 1, columnIndex)) else ""
                val previousNext = if (rowIndex > 0) normalizeText(view.textAt(rowIndex - 1, columnIndex + 1)) else ""
                if (previousCurrent == "рабочее место" && previousNext == "услуга") {
                    continue
                }
                return BlockHeader(
                    rowIndex = rowIndex,
                    workplaceColumnIndex = columnIndex,
                    serviceColumnIndex = columnIndex + 1,
                )
            }
        }
        return null
    }

    private fun findDataStartRowAfter(view: MergedAwareSheetView, afterRow: Int, nextBlockStart: Int, serviceColumnIndex: Int): Int? {
        for (rowIndex in (afterRow + 1) until nextBlockStart) {
            if (findBlockHeader(view, rowIndex) != null) continue
            val serviceText = view.textAt(rowIndex, serviceColumnIndex)?.trim()
            if (serviceText.isNullOrBlank() || !isServiceValue(serviceText)) continue
            val hasNumericValue = ((serviceColumnIndex + 1)..view.maxColumnIndex).any { columnIndex ->
                val numericValue = view.numericAt(rowIndex, columnIndex)
                    ?: parseNumeric(view.textAt(rowIndex, columnIndex))
                numericValue != null
            }
            if (hasNumericValue) return rowIndex
        }
        return null
    }

    private fun buildColumnDefinitions(
        view: MergedAwareSheetView,
        compositeBlock: CompositeBlock,
        dataStartRow: Int,
    ): List<WorkplaceColumnDefinition> {
        val allHeaderRows = (compositeBlock.firstHeaderRow until dataStartRow).toList()
        val columns = mutableListOf<WorkplaceColumnDefinition>()
        var currentDepartmentGroup: String? = null
        var currentDepartment: String? = null

        for (columnIndex in (compositeBlock.serviceColumnIndex + 1)..view.maxColumnIndex) {
            val headerTexts = allHeaderRows
                .mapNotNull { rowIndex -> view.textAt(rowIndex, columnIndex)?.trim() }
                .filter { it.isNotBlank() }
                .distinct()

            if (headerTexts.isEmpty()) continue

            // Update currentDepartmentGroup whenever we encounter a group label
            val groupText = headerTexts.firstOrNull(::isDepartmentGroupHeaderText)
            if (groupText != null) {
                currentDepartmentGroup = groupText
            }

            val isTotal = headerTexts.any(::isTotalHeaderText)
            val explicitDepartment = headerTexts.asReversed().firstOrNull(::isDepartmentHeaderText)
            if (!isTotal && !explicitDepartment.isNullOrBlank()) {
                currentDepartment = explicitDepartment
            }
            val department = if (isTotal) null else currentDepartment
            if (!isTotal && department.isNullOrBlank()) continue

            columns += WorkplaceColumnDefinition(
                columnIndex = columnIndex,
                departmentGroup = currentDepartmentGroup,
                department = department,
                isTotal = isTotal,
                totalLabel = if (isTotal) "Всего" else null,
            )
        }
        return columns
    }

    private fun parseBlockRows(
        view: MergedAwareSheetView,
        sheetId: String,
        workplaceColumnIndex: Int,
        serviceColumnIndex: Int,
        dataStartRow: Int,
        endExclusiveRow: Int,
        columns: List<WorkplaceColumnDefinition>,
        completedMetric: DamumedReportMetricProfile,
        totalMetric: DamumedReportMetricProfile,
        periodText: String?,
        accumulator: MutableMap<String, WorkplaceFactAccumulator>,
        processedCells: MutableSet<String>,
        processedFacts: MutableSet<String>,
    ) {
        var currentWorkplace: String? = null
        for (rowIndex in dataStartRow until endExclusiveRow) {
            if (findBlockHeader(view, rowIndex) != null) {
                break
            }

            val workplaceText = view.textAt(rowIndex, workplaceColumnIndex)?.trim()
            if (!workplaceText.isNullOrBlank() && isWorkplaceValue(workplaceText)) {
                currentWorkplace = workplaceText
            }
            val workplace = currentWorkplace?.takeIf { it.isNotBlank() }
                ?: workplaceText?.takeIf(::isWorkplaceValue)
                ?: continue

            val service = view.textAt(rowIndex, serviceColumnIndex)?.trim()?.takeIf(::isServiceValue) ?: continue
            if (isAggregateServiceLabel(service)) {
                continue
            }

            columns.forEach { column ->
                // Проверяем, не обрабатывали ли уже эту ячейку
                val cellKey = "$sheetId:$rowIndex:${column.columnIndex}"
                if (processedCells.contains(cellKey)) {
                    return@forEach
                }
                
                val value = view.numericAt(rowIndex, column.columnIndex)
                    ?: parseNumeric(view.textAt(rowIndex, column.columnIndex))
                    ?: return@forEach
                if (value == 0.0) {
                    return@forEach
                }
                
                // Отмечаем ячейку как обработанную
                processedCells.add(cellKey)
                
                val metric = if (column.isTotal) totalMetric else completedMetric
                
                // Семантический ключ факта (без sheetId) для отслеживания дублей
                val factKey = listOf(
                    normalizeText(workplace),
                    normalizeText(service),
                    normalizeText(column.departmentGroup),
                    normalizeText(column.department),
                    metric.key,
                    column.isTotal.toString(),
                ).joinToString("::")
                
                // Проверяем, не обрабатывали ли уже этот факт
                if (processedFacts.contains(factKey)) {
                    logger.warn("Duplicate fact detected and skipped: workplace={}, service={}, departmentGroup={}, department={}, metric={}, value={}",
                        workplace, service, column.departmentGroup, column.department, metric.key, value)
                    return@forEach
                }
                processedFacts.add(factKey)
                
                // Ключ для accumulator (с sheetId)
                val key = listOf(
                    sheetId,
                    metric.key,
                    normalizeText(workplace),
                    normalizeText(service),
                    normalizeText(column.departmentGroup),
                    normalizeText(column.department),
                    column.isTotal.toString(),
                ).joinToString("::")
                val existing = accumulator[key]
                if (existing == null) {
                    accumulator[key] = WorkplaceFactAccumulator(
                        sheetId = sheetId,
                        sheetName = view.sheet.sheetName,
                        workplace = workplace,
                        service = service,
                        departmentGroup = column.departmentGroup,
                        department = column.department,
                        isTotal = column.isTotal,
                        metric = metric,
                        periodText = periodText,
                        numericValue = value,
                        rowIndex = rowIndex,
                        columnIndex = column.columnIndex,
                        workplaceColumnIndex = workplaceColumnIndex,
                        serviceColumnIndex = serviceColumnIndex,
                    )
                } else {
                    existing.numericValue += value
                    if (rowIndex < existing.rowIndex || (rowIndex == existing.rowIndex && column.columnIndex < existing.columnIndex)) {
                        existing.rowIndex = rowIndex
                        existing.columnIndex = column.columnIndex
                    }
                }
            }
        }
    }

    private suspend fun persistAggregatedRecords(
        upload: DamumedReportUpload,
        profile: DamumedReportNormalizationProfile,
        records: List<WorkplaceFactAccumulator>,
    ): NormalizationCounters {
        var sectionSequence = 0
        var dimensionSequence = 0
        var factSequence = 0
        val sectionIndex = linkedMapOf<String, SectionAccumulator>()
        val dimensionIndex = linkedMapOf<String, DamumedNormalizedDimensionEntity>()
        val sortedRecords = records.sortedWith(
            compareBy<WorkplaceFactAccumulator> { it.sheetId }
                .thenBy { it.rowIndex }
                .thenBy { it.columnIndex },
        )

        sortedRecords.forEach { record ->
            getOrCreateSection(
                upload = upload,
                profile = profile,
                record = record,
                sectionIndex = sectionIndex,
                nextSequence = { ++sectionSequence },
            )
        }

        sectionIndex.values.forEach { sectionAccumulator ->
            normalizedSectionRepository.save(sectionAccumulator.entity)
        }

        sortedRecords.forEach { record ->
            val section = getOrCreateSection(
                upload = upload,
                profile = profile,
                record = record,
                sectionIndex = sectionIndex,
                nextSequence = { ++sectionSequence },
            )
            val factId = "${upload.id}:fact:${++factSequence}"
            val axisValues = buildAxisValues(profile, record)
            normalizedFactRepository.save(
                DamumedNormalizedFactEntity(
                    entityId = factId,
                    uploadId = upload.id,
                    reportKind = upload.reportKind,
                    sheetId = record.sheetId,
                    sectionId = section.entity.entityId,
                    rowId = "${record.sheetId}:row:${record.rowIndex}",
                    cellId = "${record.sheetId}:row:${record.rowIndex}:cell:${record.columnIndex}",
                    metricKey = record.metric.key,
                    metricLabel = record.metric.aliases.firstOrNull() ?: record.metric.key,
                    numericValue = record.numericValue,
                    valueText = formatNumeric(record.numericValue),
                    formulaText = null,
                    periodText = record.periodText,
                    sourceRowIndex = record.rowIndex,
                    sourceColumnIndex = record.columnIndex,
                ),
            )
            persistFactDimensions(
                upload = upload,
                sheetId = record.sheetId,
                factId = factId,
                axisValues = axisValues,
                dimensionIndex = dimensionIndex,
                nextDimensionSequence = { ++dimensionSequence },
            )
        }

        return NormalizationCounters(
            sectionCount = sectionIndex.size,
            factCount = factSequence,
            dimensionCount = dimensionIndex.size,
        )
    }

    private suspend fun getOrCreateSection(
        upload: DamumedReportUpload,
        profile: DamumedReportNormalizationProfile,
        record: WorkplaceFactAccumulator,
        sectionIndex: MutableMap<String, SectionAccumulator>,
        nextSequence: () -> Int,
    ): SectionAccumulator {
        val sectionKey = "${record.sheetId}:workplace:${normalizeText(record.workplace)}"
        val existing = sectionIndex[sectionKey]
        if (existing != null) {
            val updated = existing.copy(
                entity = existing.entity.copy(
                    rowStartIndex = minOf(existing.entity.rowStartIndex ?: record.rowIndex, record.rowIndex),
                    rowEndIndex = maxOf(existing.entity.rowEndIndex ?: record.rowIndex, record.rowIndex),
                    columnStartIndex = minOf(existing.entity.columnStartIndex ?: record.columnIndex, record.columnIndex),
                    columnEndIndex = maxOf(existing.entity.columnEndIndex ?: record.columnIndex, record.columnIndex),
                ),
            )
            sectionIndex[sectionKey] = updated
            return updated
        }

        val created = SectionAccumulator(
            entity = DamumedNormalizedSectionEntity(
                entityId = "${upload.id}:section:${nextSequence()}",
                uploadId = upload.id,
                reportKind = upload.reportKind,
                sheetId = record.sheetId,
                sectionKey = sectionKey,
                sectionName = record.workplace,
                semanticRole = profile.semanticRole.name,
                anchorAxisKey = "workplace",
                anchorAxisValue = record.workplace,
                rowStartIndex = record.rowIndex,
                rowEndIndex = record.rowIndex,
                columnStartIndex = record.columnIndex,
                columnEndIndex = record.columnIndex,
            ),
        )
        sectionIndex[sectionKey] = created
        return created
    }

    private fun buildAxisValues(
        profile: DamumedReportNormalizationProfile,
        record: WorkplaceFactAccumulator,
    ): List<AxisValue> {
        val values = mutableListOf<AxisValue>()
        profile.dimensions.firstOrNull { it.type == DamumedReportAxisType.PERIOD }
            ?.let { axis ->
                record.periodText?.takeIf { it.isNotBlank() }?.let { value ->
                    values += AxisValue(
                        axis = axis,
                        rawValue = value,
                        normalizedValue = normalizeText(value),
                        displayValue = value,
                        sourceRowIndex = record.rowIndex,
                        sourceColumnIndex = record.columnIndex,
                        sourceScope = "upload",
                    )
                }
            }
        profile.dimensions.firstOrNull { it.type == DamumedReportAxisType.WORKPLACE }
            ?.let { axis ->
                values += AxisValue(
                    axis = axis,
                    rawValue = record.workplace,
                    normalizedValue = normalizeText(record.workplace),
                    displayValue = record.workplace,
                    sourceRowIndex = record.rowIndex,
                    sourceColumnIndex = record.workplaceColumnIndex,
                    sourceScope = "row",
                )
            }
        profile.dimensions.firstOrNull { it.type == DamumedReportAxisType.SERVICE }
            ?.let { axis ->
                values += AxisValue(
                    axis = axis,
                    rawValue = record.service,
                    normalizedValue = normalizeText(record.service),
                    displayValue = record.service,
                    sourceRowIndex = record.rowIndex,
                    sourceColumnIndex = record.serviceColumnIndex,
                    sourceScope = "row",
                )
            }
        profile.dimensions.firstOrNull { it.type == DamumedReportAxisType.DEPARTMENT_GROUP }
            ?.let { axis ->
                record.departmentGroup?.takeIf { it.isNotBlank() }?.let { value ->
                    values += AxisValue(
                        axis = axis,
                        rawValue = value,
                        normalizedValue = normalizeText(value),
                        displayValue = value,
                        sourceRowIndex = record.rowIndex,
                        sourceColumnIndex = record.columnIndex,
                        sourceScope = "header",
                    )
                }
            }
        profile.dimensions.firstOrNull { it.type == DamumedReportAxisType.DEPARTMENT }
            ?.let { axis ->
                record.department?.takeIf { it.isNotBlank() }?.let { value ->
                    values += AxisValue(
                        axis = axis,
                        rawValue = value,
                        normalizedValue = normalizeText(value),
                        displayValue = value,
                        sourceRowIndex = record.rowIndex,
                        sourceColumnIndex = record.columnIndex,
                        sourceScope = "header",
                    )
                }
            }
        profile.dimensions.firstOrNull { it.type == DamumedReportAxisType.TOTAL }
            ?.let { axis ->
                if (record.isTotal) {
                    values += AxisValue(
                        axis = axis,
                        rawValue = "Всего",
                        normalizedValue = normalizeText("Всего"),
                        displayValue = "Всего",
                        sourceRowIndex = record.rowIndex,
                        sourceColumnIndex = record.columnIndex,
                        sourceScope = "header",
                    )
                }
            }
        return values
    }

    private suspend fun persistFactDimensions(
        upload: DamumedReportUpload,
        sheetId: String,
        factId: String,
        axisValues: List<AxisValue>,
        dimensionIndex: MutableMap<String, DamumedNormalizedDimensionEntity>,
        nextDimensionSequence: () -> Int,
    ) {
        axisValues.forEach { axisValue ->
            val dimensionKey = "${axisValue.axis.key}:${axisValue.normalizedValue}"
            val dimensionEntity = dimensionIndex[dimensionKey] ?: DamumedNormalizedDimensionEntity(
                entityId = "${upload.id}:dim:${nextDimensionSequence()}",
                uploadId = upload.id,
                reportKind = upload.reportKind,
                axisKey = axisValue.axis.key,
                axisType = axisValue.axis.type.name,
                rawValue = axisValue.rawValue,
                normalizedValue = axisValue.normalizedValue,
                displayValue = axisValue.displayValue,
                sourceSheetId = sheetId,
                sourceRowIndex = axisValue.sourceRowIndex,
                sourceColumnIndex = axisValue.sourceColumnIndex,
            ).also { createdDimension ->
                normalizedDimensionRepository.save(createdDimension)
                dimensionIndex[dimensionKey] = createdDimension
            }
            normalizedFactDimensionRepository.save(
                DamumedNormalizedFactDimensionEntity(
                    entityId = "$factId:${axisValue.axis.key}",
                    factId = factId,
                    axisKey = axisValue.axis.key,
                    dimensionId = dimensionEntity.entityId,
                    rawValue = axisValue.rawValue,
                    normalizedValue = axisValue.normalizedValue,
                    sourceScope = axisValue.sourceScope,
                ),
            )
        }
    }

    private suspend fun cleanupExistingNormalizedData(uploadId: String) {
        val factIds = normalizedFactRepository.findAllByUploadIdOrderBySheetIdAscSourceRowIndexAscSourceColumnIndexAsc(uploadId)
            .toList()
            .map { it.entityId }
        if (factIds.isNotEmpty()) {
            factIds.forEach { factId ->
                normalizedFactDimensionRepository.deleteAllByFactId(factId)
            }
        }
        normalizedFactRepository.deleteAllByUploadId(uploadId)
        normalizedDimensionRepository.deleteAllByUploadId(uploadId)
        normalizedSectionRepository.deleteAllByUploadId(uploadId)
    }

    private suspend fun DamumedReportUpload.persistUpload(): DamumedReportUpload {
        return uploadRepository.save(this.toEntity()).toModel()
    }

    private fun detectPeriodText(workbook: Workbook, evaluator: FormulaEvaluator): String? {
        repeat(workbook.numberOfSheets) { sheetIndex ->
            val view = MergedAwareSheetView(workbook.getSheetAt(sheetIndex), evaluator)
            val maxRowIndex = minOf(view.maxRowIndex, 12)
            val maxColumnIndex = minOf(view.maxColumnIndex, 8)
            for (rowIndex in 0..maxRowIndex) {
                for (columnIndex in 0..maxColumnIndex) {
                    val value = view.textAt(rowIndex, columnIndex)?.trim().orEmpty()
                    if (value.isNotBlank() && looksLikePeriodText(value) && normalizeText(value) != "период") {
                        return value
                    }
                }
            }
        }
        return null
    }

    private fun resolveWorkbookPath(upload: DamumedReportUpload): Path {
        return location.resolve(upload.storagePath.replace('/', '\\')).toAbsolutePath().normalize()
    }

    private fun isDepartmentGroupHeaderText(text: String): Boolean {
        return normalizeText(text) in setOf("амбулатория", "стационар")
    }

    private fun isDepartmentHeaderText(text: String): Boolean {
        val normalized = normalizeText(text)
        return normalized.isNotBlank() &&
            normalized !in setOf("рабочее место", "услуга", "отделение", "период", "амбулатория", "стационар", "всего", "итого") ||
            normalized == "не определено" // Явно разрешаем "Не определено"
    }

    private fun isTotalHeaderText(text: String): Boolean {
        val normalized = normalizeText(text)
        return normalized == "всего" || normalized.startsWith("всего ") || normalized == "итого"
    }

    private fun isWorkplaceValue(text: String?): Boolean {
        val normalized = normalizeText(text)
        if (normalized.isBlank()) {
            return false
        }
        if (normalized in setOf("рабочее место", "услуга", "всего", "итого")) {
            return false
        }
        return !looksLikePeriodText(text.orEmpty())
    }

    private fun isServiceValue(text: String?): Boolean {
        val normalized = normalizeText(text)
        if (normalized.isBlank()) {
            return false
        }
        if (normalized in setOf("рабочее место", "услуга", "всего", "итого")) {
            return false
        }
        return !looksLikePeriodText(text.orEmpty())
    }

    private fun isAggregateServiceLabel(text: String): Boolean {
        return normalizeText(text) in setOf("всего", "итого")
    }

    private fun looksLikePeriodText(text: String): Boolean {
        val normalized = normalizeText(text)
        // Check for explicit period keywords
        if (normalized.contains("период")) {
            return true
        }
        // Check for date patterns like "с 01.01.2024" or "с 1 января" - must have date-like content after "с "
        if (normalized.contains("с ")) {
            val afterS = normalized.substringAfter("с ")
            // Must look like a date: contains digits AND date separators OR month names
            // BUT exclude service names that contain digits like "дифференцировкой 5 классов"
            if (afterS.any(Char::isDigit) && 
                (Regex("[./-]").containsMatchIn(afterS) || 
                 (afterS.contains("янв") || afterS.contains("фев") || afterS.contains("мар") ||
                  afterS.contains("апр") || afterS.contains("мая") || afterS.contains("июн") ||
                  afterS.contains("июл") || afterS.contains("авг") || afterS.contains("сен") ||
                  afterS.contains("окт") || afterS.contains("ноя") || afterS.contains("дек")) &&
                 // Exclude common service name patterns
                 !afterS.contains("класс") && !afterS.contains("анализ") && !afterS.contains("исслед") && !afterS.contains("тест"))) {
                return true
            }
        }
        // Check for date patterns like "по 01.01.2024"
        if (normalized.contains("по ")) {
            val afterPo = normalized.substringAfter("по ")
            if (afterPo.any(Char::isDigit) && 
                (Regex("[./-]").containsMatchIn(afterPo) || 
                 afterPo.contains("янв") || afterPo.contains("фев") || afterPo.contains("мар") ||
                 afterPo.contains("апр") || afterPo.contains("мая") || afterPo.contains("июн") ||
                 afterPo.contains("июл") || afterPo.contains("авг") || afterPo.contains("сен") ||
                 afterPo.contains("окт") || afterPo.contains("ноя") || afterPo.contains("дек")) &&
                 !afterPo.contains("класс") && !afterPo.contains("анализ") && !afterPo.contains("исслед") && !afterPo.contains("тест")) {
                return true
            }
        }
        // Standard date format patterns - be more strict
        if (Regex("^\\d{1,2}[./-]\\d{1,2}[./-]\\d{2,4}$").containsMatchIn(normalized) ||
            Regex("^20\\d{2}$").containsMatchIn(normalized)) {
            return true
        }
        return false
    }

    private fun normalizeText(value: String?): String {
        return value
            .orEmpty()
            .replace('\u00A0', ' ')
            .lowercase()
            .replace(Regex("\\s+"), " ")
            .trim(' ', ':', ';', '-', '\t')
    }

    private fun parseNumeric(value: String?): Double? {
        val normalized = value
            ?.replace('\u00A0', ' ')
            ?.replace(" ", "")
            ?.replace(",", ".")
            ?.trim()
            ?.removeSuffix("%")
            ?: return null
        return normalized.toDoubleOrNull()
    }

    private fun formatNumeric(value: Double): String {
        return if (value % 1.0 == 0.0) {
            value.toLong().toString()
        } else {
            value.toString()
        }
    }

    private data class WorkplaceColumnDefinition(
        val columnIndex: Int,
        val departmentGroup: String?,
        val department: String?,
        val isTotal: Boolean,
        val totalLabel: String?,
    )

    private data class BlockHeader(
        val rowIndex: Int,
        val workplaceColumnIndex: Int,
        val serviceColumnIndex: Int,
    )

    private data class CompositeBlock(
        val firstHeaderRow: Int,
        val lastHeaderRow: Int,
        val workplaceColumnIndex: Int,
        val serviceColumnIndex: Int,
    )

    companion object {
        private const val MAX_HEADER_GAP = 5
    }

    private data class WorkplaceFactAccumulator(
        val sheetId: String,
        val sheetName: String,
        val workplace: String,
        val service: String,
        val departmentGroup: String?,
        val department: String?,
        val isTotal: Boolean,
        val metric: DamumedReportMetricProfile,
        val periodText: String?,
        var numericValue: Double,
        var rowIndex: Int,
        var columnIndex: Int,
        val workplaceColumnIndex: Int,
        val serviceColumnIndex: Int,
    )

    private data class AxisValue(
        val axis: DamumedReportAxisProfile,
        val rawValue: String,
        val normalizedValue: String,
        val displayValue: String,
        val sourceRowIndex: Int,
        val sourceColumnIndex: Int,
        val sourceScope: String,
    )

    private data class SectionAccumulator(
        val entity: DamumedNormalizedSectionEntity,
    )

    private data class NormalizationCounters(
        val sectionCount: Int,
        val factCount: Int,
        val dimensionCount: Int,
    )

    private inner class MergedAwareSheetView(
        val sheet: Sheet,
        private val evaluator: FormulaEvaluator,
    ) {
        private val mergedRegionsByRow: Map<Int, List<CellRangeAddress>> = mutableMapOf<Int, MutableList<CellRangeAddress>>()
            .apply {
                repeat(sheet.numMergedRegions) { regionIndex ->
                    val region = sheet.getMergedRegion(regionIndex)
                    for (rowIndex in region.firstRow..region.lastRow) {
                        getOrPut(rowIndex) { mutableListOf() }.add(region)
                    }
                }
            }

        val maxRowIndex: Int = sheet.lastRowNum.coerceAtLeast(0)

        val maxColumnIndex: Int = buildMaxColumnIndex()

        fun textAt(rowIndex: Int, columnIndex: Int): String? {
            val cell = resolvedCell(rowIndex, columnIndex) ?: return null
            val formatted = runCatching { dataFormatter.formatCellValue(cell, evaluator) }
                .getOrElse { dataFormatter.formatCellValue(cell) }
                .trim()
            if (formatted.isNotBlank()) {
                return formatted
            }
            return when (cell.cellType) {
                CellType.STRING -> cell.stringCellValue?.trim()?.takeIf { it.isNotBlank() }
                CellType.NUMERIC -> cell.numericCellValue.toString()
                CellType.BOOLEAN -> cell.booleanCellValue.toString()
                CellType.FORMULA -> runCatching { cell.stringCellValue }.getOrNull()?.trim()?.takeIf { it.isNotBlank() }
                else -> null
            }
        }

        fun numericAt(rowIndex: Int, columnIndex: Int): Double? {
            val cell = resolvedCell(rowIndex, columnIndex) ?: return null
            return when (cell.cellType) {
                CellType.NUMERIC -> cell.numericCellValue
                CellType.FORMULA -> {
                    val evaluated = runCatching { evaluator.evaluate(cell) }.getOrNull() ?: return null
                    if (evaluated.cellType == CellType.NUMERIC) evaluated.numberValue else null
                }

                else -> null
            }
        }

        private fun resolvedCell(rowIndex: Int, columnIndex: Int): Cell? {
            val direct = sheet.getRow(rowIndex)
                ?.getCell(columnIndex, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL)
            if (direct != null && direct.cellType != CellType.BLANK) {
                return direct
            }
            val region = mergedRegionsByRow[rowIndex]
                .orEmpty()
                .firstOrNull { candidate -> columnIndex in candidate.firstColumn..candidate.lastColumn }
                ?: return null
            return sheet.getRow(region.firstRow)
                ?.getCell(region.firstColumn, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL)
        }

        private fun buildMaxColumnIndex(): Int {
            var result = 0
            for (rowIndex in 0..maxRowIndex) {
                val row = sheet.getRow(rowIndex)
                val lastCell = row?.lastCellNum?.toInt()?.minus(1) ?: -1
                result = maxOf(result, lastCell)
            }
            repeat(sheet.numMergedRegions) { regionIndex ->
                val region = sheet.getMergedRegion(regionIndex)
                result = maxOf(result, region.lastColumn)
            }
            return result.coerceAtLeast(0)
        }
    }
}
