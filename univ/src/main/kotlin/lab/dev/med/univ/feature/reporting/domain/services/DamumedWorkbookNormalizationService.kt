package lab.dev.med.univ.feature.reporting.domain.services

import kotlinx.coroutines.flow.toList
import lab.dev.med.univ.feature.reporting.data.entity.DamumedNormalizedDimensionEntity
import lab.dev.med.univ.feature.reporting.data.entity.DamumedNormalizedFactDimensionEntity
import lab.dev.med.univ.feature.reporting.data.entity.DamumedNormalizedFactEntity
import lab.dev.med.univ.feature.reporting.data.entity.DamumedNormalizedSectionEntity
import lab.dev.med.univ.feature.reporting.data.entity.DamumedParsedCellEntity
import lab.dev.med.univ.feature.reporting.data.entity.DamumedParsedMergedRegionEntity
import lab.dev.med.univ.feature.reporting.data.entity.DamumedParsedRowEntity
import lab.dev.med.univ.feature.reporting.data.entity.DamumedParsedSheetEntity
import lab.dev.med.univ.feature.reporting.data.entity.toEntity
import lab.dev.med.univ.feature.reporting.data.entity.toModel
import lab.dev.med.univ.feature.reporting.data.repository.DamumedNormalizedBatchRepository
import lab.dev.med.univ.feature.reporting.data.repository.DamumedNormalizedDimensionRepository
import lab.dev.med.univ.feature.reporting.data.repository.DamumedNormalizedFactDimensionRepository
import lab.dev.med.univ.feature.reporting.data.repository.DamumedNormalizedFactRepository
import lab.dev.med.univ.feature.reporting.data.repository.DamumedNormalizedSectionRepository
import lab.dev.med.univ.feature.reporting.data.repository.DamumedParsedCellRepository
import lab.dev.med.univ.feature.reporting.data.repository.DamumedParsedMergedRegionRepository
import lab.dev.med.univ.feature.reporting.data.repository.DamumedParsedRowRepository
import lab.dev.med.univ.feature.reporting.data.repository.DamumedParsedSheetRepository
import lab.dev.med.univ.feature.reporting.data.repository.DamumedReportUploadRepository
import lab.dev.med.univ.feature.reporting.domain.models.DamumedLabReportKind
import lab.dev.med.univ.feature.reporting.domain.models.DamumedReportAxisProfile
import lab.dev.med.univ.feature.reporting.domain.models.DamumedReportAxisType
import lab.dev.med.univ.feature.reporting.domain.models.DamumedReportMetricProfile
import lab.dev.med.univ.feature.reporting.domain.models.DamumedReportNormalizationProfile
import lab.dev.med.univ.feature.reporting.domain.models.DamumedReportNormalizationStatus
import lab.dev.med.univ.feature.reporting.domain.models.DamumedReportSchemaCatalog
import lab.dev.med.univ.feature.reporting.domain.models.DamumedReportUpload
import lab.dev.med.univ.feature.reporting.domain.errors.DamumedReportValidationException
import org.springframework.stereotype.Service
import java.time.LocalDateTime

interface DamumedWorkbookNormalizationService {
    suspend fun normalize(upload: DamumedReportUpload): DamumedReportUpload
}

@Service
class DamumedWorkbookNormalizationServiceImpl(
    private val uploadRepository: DamumedReportUploadRepository,
    private val parsedSheetRepository: DamumedParsedSheetRepository,
    private val parsedRowRepository: DamumedParsedRowRepository,
    private val parsedCellRepository: DamumedParsedCellRepository,
    private val parsedMergedRegionRepository: DamumedParsedMergedRegionRepository,
    private val normalizedSectionRepository: DamumedNormalizedSectionRepository,
    private val normalizedDimensionRepository: DamumedNormalizedDimensionRepository,
    private val normalizedFactRepository: DamumedNormalizedFactRepository,
    private val normalizedFactDimensionRepository: DamumedNormalizedFactDimensionRepository,
    private val batchRepository: DamumedNormalizedBatchRepository,
) : DamumedWorkbookNormalizationService {
    override suspend fun normalize(upload: DamumedReportUpload): DamumedReportUpload {
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

            val profile = DamumedReportSchemaCatalog.profileFor(started.reportKind)
            val sheets = parsedSheetRepository.findAllByUploadIdOrderBySheetIndexAsc(started.id).toList()
            if (sheets.isEmpty()) {
                return started.copy(
                    normalizationStatus = DamumedReportNormalizationStatus.NORMALIZED,
                    normalizationCompletedAt = LocalDateTime.now(),
                ).persistUpload()
            }

            val snapshots = buildList {
                for (sheet in sheets) {
                    add(buildSheetSnapshot(sheet))
                }
            }
            val counters = when (started.reportKind) {
                DamumedLabReportKind.WORKPLACE_COMPLETED_STUDIES -> normalizeWorkplaceCompletedStudies(started, profile, snapshots)
                DamumedLabReportKind.REFERRAL_REGISTRATION_JOURNAL -> normalizeReferralRegistrationJournal(started, profile, snapshots)
                DamumedLabReportKind.COMPLETED_LAB_STUDIES_JOURNAL -> normalizeCompletedLabStudiesJournal(started, profile, snapshots)
                DamumedLabReportKind.POSITIVE_RESULTS_JOURNAL -> normalizePositiveResultsJournal(started, profile, snapshots)
                DamumedLabReportKind.REJECT_LOG -> normalizeRejectLog(started, profile, snapshots)
                DamumedLabReportKind.GOBMP_COMPLETED_SERVICES -> normalizeGobmpCompletedServices(started, profile, snapshots)
                DamumedLabReportKind.REFERRAL_COUNT_BY_MATERIAL -> normalizeReferralCountByMaterial(started, profile, snapshots)
                DamumedLabReportKind.EMPLOYEE_COMPLETED_STUDIES_SUMMARY -> normalizeEmployeeCompletedStudiesSummary(started, profile, snapshots)
                else -> normalizeGenericReport(started, profile, snapshots)
            }

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

    private suspend fun normalizeGenericReport(
        upload: DamumedReportUpload,
        profile: DamumedReportNormalizationProfile,
        snapshots: List<SheetSnapshot>,
    ): NormalizationCounters {
        var sectionSequence = 0
        var dimensionSequence = 0
        var factSequence = 0
        val dimensionIndex = linkedMapOf<String, DamumedNormalizedDimensionEntity>()
        val sectionIndex = linkedMapOf<String, SectionAccumulator>()

        snapshots.forEach { snapshot ->
            snapshot.rows.forEach { row ->
                val rowCells = snapshot.cellsByRow[row.rowIndex].orEmpty()
                rowCells
                    .filter { it.sourceColumnIndex != null }
                    .filter { isPotentialFactCell(it.cell) }
                    .forEach { projectedCell ->
                        val sourceColumnIndex = projectedCell.sourceColumnIndex ?: return@forEach
                        val headerTexts = snapshot.collectHeaderTexts(row.rowIndex, sourceColumnIndex)
                        val rowTexts = snapshot.collectRowTexts(row.rowIndex, sourceColumnIndex)
                        val metric = resolveMetric(profile, headerTexts, rowTexts, projectedCell.cell) ?: return@forEach
                        val axisValues = resolveAxisValues(profile, snapshot, row.rowIndex, sourceColumnIndex, headerTexts, rowTexts, upload)
                        val sectionAccumulator = getOrCreateSection(
                            upload = upload,
                            profile = profile,
                            sheet = snapshot.sheet,
                            rowIndex = row.rowIndex,
                            columnIndex = sourceColumnIndex,
                            axisValues = axisValues,
                            sectionIndex = sectionIndex,
                            nextSequence = { ++sectionSequence },
                        )
                        val factId = "${upload.id}:fact:${++factSequence}"
                        persistNormalizedFact(
                            upload = upload,
                            sheet = snapshot.sheet,
                            factId = factId,
                            sectionId = sectionAccumulator.entity.entityId,
                            metric = metric,
                            numericValue = projectedCell.cell.numericValue ?: parseNumeric(projectedCell.cell.formattedValueText),
                            valueText = projectedCell.cell.formattedValueText ?: projectedCell.cell.rawValueText,
                            formulaText = projectedCell.cell.formulaText,
                            periodText = axisValues["period"]?.displayValue ?: upload.detectedPeriodText,
                            rowId = projectedCell.cell.rowId,
                            cellId = projectedCell.cell.entityId,
                            sourceRowIndex = row.rowIndex,
                            sourceColumnIndex = sourceColumnIndex,
                            axisValues = axisValues,
                            dimensionIndex = dimensionIndex,
                            nextDimensionSequence = { ++dimensionSequence },
                        )
                    }
            }
        }

        sectionIndex.values.forEach { accumulator ->
            normalizedSectionRepository.save(accumulator.entity)
        }

        return NormalizationCounters(
            sectionCount = sectionIndex.size,
            factCount = factSequence,
            dimensionCount = dimensionIndex.size,
        )
    }

    private suspend fun normalizeRejectLog(
        upload: DamumedReportUpload,
        profile: DamumedReportNormalizationProfile,
        snapshots: List<SheetSnapshot>,
    ): NormalizationCounters {
        var sectionSequence = 0
        var dimensionSequence = 0
        var factSequence = 0
        val dimensionIndex = linkedMapOf<String, DamumedNormalizedDimensionEntity>()
        val sectionIndex = linkedMapOf<String, SectionAccumulator>()
        val rowFacts = mutableListOf<RegistryRowFactAccumulator>()
        val metric = profile.metrics.firstOrNull { it.key == "rejected_count" }
            ?: throw DamumedReportValidationException("Reject log metric profile is not configured.")

        snapshots.forEach { snapshot ->
            val layout = detectRejectLogLayout(snapshot) ?: return@forEach
            for (rowIndex in layout.dataStartRow..layout.dataEndRow) {
                if (!isRejectLogDataRow(snapshot, layout, rowIndex)) {
                    continue
                }
                val axisValues = buildRejectLogAxisValues(
                    profile = profile,
                    snapshot = snapshot,
                    layout = layout,
                    rowIndex = rowIndex,
                    periodText = upload.detectedPeriodText,
                )
                val orderNumber = axisValues["order_number"]?.displayValue ?: continue
                val sourceColumnIndex = layout.axisColumnIndex("order_number") ?: 2
                val sectionAccumulator = getOrCreateSection(
                    upload = upload,
                    profile = profile,
                    sheet = snapshot.sheet,
                    rowIndex = rowIndex,
                    columnIndex = sourceColumnIndex,
                    axisValues = axisValues,
                    sectionIndex = sectionIndex,
                    nextSequence = { ++sectionSequence },
                )

                val anchorCell = layout.axisCell(snapshot, rowIndex, "order_number")
                    ?: layout.axisCell(snapshot, rowIndex, "registry_number")
                rowFacts += RegistryRowFactAccumulator(
                    sheet = snapshot.sheet,
                    rowId = anchorCell?.rowId ?: "${snapshot.sheet.entityId}:row:$rowIndex",
                    cellId = anchorCell?.entityId,
                    metric = metric,
                    numericValue = 1.0,
                    valueText = orderNumber,
                    formulaText = null,
                    periodText = upload.detectedPeriodText,
                    sourceRowIndex = rowIndex,
                    sourceColumnIndex = sourceColumnIndex,
                    axisValues = axisValues,
                    sectionId = sectionAccumulator.entity.entityId,
                )
            }
        }

        sectionIndex.values
            .sortedWith(
                compareBy<SectionAccumulator> { it.entity.sheetId }
                    .thenBy { it.entity.rowStartIndex ?: Int.MAX_VALUE }
                    .thenBy { it.entity.columnStartIndex ?: Int.MAX_VALUE },
            )
            .forEach { accumulator ->
                normalizedSectionRepository.save(accumulator.entity)
            }

        rowFacts
            .sortedWith(
                compareBy<RegistryRowFactAccumulator> { it.sheet.sheetIndex }
                    .thenBy { it.sourceRowIndex }
                    .thenBy { it.sourceColumnIndex },
            )
            .forEach { rowFact ->
                val factId = "${upload.id}:fact:${++factSequence}"
                persistNormalizedFact(
                    upload = upload,
                    sheet = rowFact.sheet,
                    factId = factId,
                    sectionId = rowFact.sectionId
                        ?: throw DamumedReportValidationException("Reject log section was not prepared for fact persistence."),
                    metric = rowFact.metric,
                    numericValue = rowFact.numericValue,
                    valueText = rowFact.valueText,
                    formulaText = rowFact.formulaText,
                    periodText = rowFact.periodText,
                    rowId = rowFact.rowId,
                    cellId = rowFact.cellId,
                    sourceRowIndex = rowFact.sourceRowIndex,
                    sourceColumnIndex = rowFact.sourceColumnIndex,
                    axisValues = rowFact.axisValues,
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

    private suspend fun normalizeWorkplaceCompletedStudies(
        upload: DamumedReportUpload,
        profile: DamumedReportNormalizationProfile,
        snapshots: List<SheetSnapshot>,
    ): NormalizationCounters {
        var sectionSequence = 0
        var dimensionSequence = 0
        var factSequence = 0
        val dimensionIndex = linkedMapOf<String, DamumedNormalizedDimensionEntity>()
        val sectionIndex = linkedMapOf<String, SectionAccumulator>()
        val rowFacts = mutableListOf<StructuredFactAccumulator>()
        val completedMetric = profile.metrics.firstOrNull { !it.representsTotal } ?: profile.metrics.first()
        val totalMetric = profile.metrics.firstOrNull { it.representsTotal } ?: completedMetric

        snapshots.forEach { snapshot ->
            val periodText = resolveWorkplaceReportPeriod(snapshot, upload)
            detectWorkplaceBlocks(snapshot).forEach { block ->
                var currentWorkplace: String? = null
                for (rowIndex in block.dataStartRow..block.dataEndRow) {
                    if (isWorkplaceBlockHeader(snapshot, rowIndex)) {
                        break
                    }

                    val workplaceText = snapshot.visibleTextAt(rowIndex, 0)?.trim()
                    val serviceText = snapshot.visibleTextAt(rowIndex, 1)?.trim()
                    if (!workplaceText.isNullOrBlank() && isWorkplaceReportWorkplaceValue(workplaceText)) {
                        currentWorkplace = workplaceText
                    }

                    val workplace = currentWorkplace?.takeIf { it.isNotBlank() }
                        ?: workplaceText?.takeIf(::isWorkplaceReportWorkplaceValue)
                        ?: continue
                    val service = serviceText?.takeIf(::isWorkplaceReportServiceValue) ?: continue
                    if (isAggregateServiceLabel(service)) {
                        continue
                    }

                    block.columns.forEach { column ->
                        val cell = snapshot.rawCellAt(rowIndex, column.columnIndex)
                        val visibleText = snapshot.visibleTextAt(rowIndex, column.columnIndex)
                        val numericValue = cell?.numericValue
                            ?: parseNumeric(cell?.formattedValueText ?: cell?.rawValueText ?: visibleText)
                            ?: 0.0
                        // Skip zero values to reduce noise
                        if (numericValue == 0.0) {
                            return@forEach
                        }
                        val metric = if (column.isTotal) totalMetric else completedMetric
                        val axisValues = buildWorkplaceAxisValues(
                            profile = profile,
                            periodText = periodText,
                            workplace = workplace,
                            service = service,
                            column = column,
                            sourceRowIndex = rowIndex,
                        )
                        val rowId = cell?.rowId ?: "${snapshot.sheet.entityId}:row:$rowIndex"
                        val valueText = cell?.formattedValueText ?: cell?.rawValueText ?: visibleText

                        // Create separate fact for each cell - NO aggregation
                        rowFacts += StructuredFactAccumulator(
                            sheet = snapshot.sheet,
                            rowId = rowId,
                            cellId = cell?.entityId,
                            metric = metric,
                            numericValue = numericValue,
                            valueText = valueText,
                            formulaText = cell?.formulaText,
                            periodText = periodText,
                            sourceRowIndex = rowIndex,
                            sourceColumnIndex = column.columnIndex,
                            axisValues = axisValues,
                        )
                    }
                }
            }
        }

        rowFacts
            .sortedWith(
                compareBy<StructuredFactAccumulator> { it.sheet.sheetIndex }
                    .thenBy { it.sourceRowIndex }
                    .thenBy { it.sourceColumnIndex },
            )
            .forEach { rowFact ->
                val sectionAccumulator = getOrCreateSection(
                    upload = upload,
                    profile = profile,
                    sheet = rowFact.sheet,
                    rowIndex = rowFact.sourceRowIndex,
                    columnIndex = rowFact.sourceColumnIndex,
                    axisValues = rowFact.axisValues,
                    sectionIndex = sectionIndex,
                    nextSequence = { ++sectionSequence },
                )
                val factId = "${upload.id}:fact:${++factSequence}"
                persistNormalizedFact(
                    upload = upload,
                    sheet = rowFact.sheet,
                    factId = factId,
                    sectionId = sectionAccumulator.entity.entityId,
                    metric = rowFact.metric,
                    numericValue = rowFact.numericValue,
                    valueText = rowFact.valueText,
                    formulaText = rowFact.formulaText,
                    periodText = rowFact.periodText,
                    rowId = rowFact.rowId,
                    cellId = rowFact.cellId,
                    sourceRowIndex = rowFact.sourceRowIndex,
                    sourceColumnIndex = rowFact.sourceColumnIndex,
                    axisValues = rowFact.axisValues,
                    dimensionIndex = dimensionIndex,
                    nextDimensionSequence = { ++dimensionSequence },
                )
            }

        sectionIndex.values.forEach { accumulator ->
            normalizedSectionRepository.save(accumulator.entity)
        }

        return NormalizationCounters(
            sectionCount = sectionIndex.size,
            factCount = factSequence,
            dimensionCount = dimensionIndex.size,
        )
    }

    private suspend fun normalizeReferralCountByMaterial(
        upload: DamumedReportUpload,
        profile: DamumedReportNormalizationProfile,
        snapshots: List<SheetSnapshot>,
    ): NormalizationCounters {
        var sectionSequence = 0
        var dimensionSequence = 0
        var factSequence = 0
        val dimensionIndex = linkedMapOf<String, DamumedNormalizedDimensionEntity>()

        snapshots.forEach { snapshot ->
            val layout = detectReferralCountByMaterialLayout(snapshot)
            val periodText = upload.detectedPeriodText
            val metric = profile.metrics.firstOrNull()
                ?: throw DamumedReportValidationException("Referral count by material metric is not configured.")

            for (rowIndex in layout.dataStartRow..layout.dataEndRow) {
                val material = snapshot.visibleTextAt(rowIndex, layout.materialColumnIndex)?.trim().orEmpty()
                if (material.isBlank()) {
                    continue
                }
                if (normalizeText(material) == normalizeText("Итого")) {
                    continue
                }

                val sectionId = "${upload.id}:section:${++sectionSequence}"
                normalizedSectionRepository.save(
                    DamumedNormalizedSectionEntity(
                        entityId = sectionId,
                        uploadId = upload.id,
                        reportKind = upload.reportKind,
                        sheetId = snapshot.sheet.entityId,
                        sectionKey = "${snapshot.sheet.entityId}:material:${normalizeText(material)}",
                        sectionName = material,
                        semanticRole = profile.semanticRole.name,
                        anchorAxisKey = "material",
                        anchorAxisValue = material,
                        rowStartIndex = rowIndex,
                        rowEndIndex = rowIndex,
                        columnStartIndex = layout.bucketColumns.minOf { it.columnIndex },
                        columnEndIndex = layout.bucketColumns.maxOf { it.columnIndex },
                    ),
                )

                layout.bucketColumns.forEach { bucket ->
                    val cell = snapshot.rawCellAt(rowIndex, bucket.columnIndex)
                    val visibleText = snapshot.visibleTextAt(rowIndex, bucket.columnIndex)
                    val numericValue = cell?.numericValue
                        ?: parseNumeric(cell?.formattedValueText ?: cell?.rawValueText ?: visibleText)
                        ?: 0.0
                    val axisValues = buildReferralCountByMaterialAxisValues(
                        profile = profile,
                        periodText = periodText,
                        material = material,
                        bucket = bucket,
                        rowIndex = rowIndex,
                    )
                    val factId = "${upload.id}:fact:${++factSequence}"
                    persistNormalizedFact(
                        upload = upload,
                        sheet = snapshot.sheet,
                        factId = factId,
                        sectionId = sectionId,
                        metric = metric,
                        numericValue = numericValue,
                        valueText = cell?.formattedValueText ?: cell?.rawValueText ?: visibleText,
                        formulaText = cell?.formulaText,
                        periodText = periodText,
                        rowId = cell?.rowId ?: "${snapshot.sheet.entityId}:row:$rowIndex",
                        cellId = cell?.entityId,
                        sourceRowIndex = rowIndex,
                        sourceColumnIndex = bucket.columnIndex,
                        axisValues = axisValues,
                        dimensionIndex = dimensionIndex,
                        nextDimensionSequence = { ++dimensionSequence },
                    )
                }
            }
        }

        return NormalizationCounters(
            sectionCount = sectionSequence,
            factCount = factSequence,
            dimensionCount = dimensionIndex.size,
        )
    }

    private suspend fun normalizeReferralRegistrationJournal(
        upload: DamumedReportUpload,
        profile: DamumedReportNormalizationProfile,
        snapshots: List<SheetSnapshot>,
    ): NormalizationCounters {
        var sectionSequence = 0
        var dimensionSequence = 0
        var factSequence = 0
        val dimensionIndex = linkedMapOf<String, DamumedNormalizedDimensionEntity>()
        val sectionIndex = linkedMapOf<String, SectionAccumulator>()
        val rowFacts = mutableListOf<RegistryRowFactAccumulator>()
        val metric = profile.metrics.firstOrNull()
            ?: throw DamumedReportValidationException("Referral registration journal metric profile is not configured.")

        snapshots.forEach { snapshot ->
            val layout = detectReferralRegistrationJournalLayout(snapshot, profile) ?: return@forEach
            for (rowIndex in layout.dataStartRow..layout.dataEndRow) {
                if (!isReferralRegistrationJournalDataRow(snapshot, layout, rowIndex)) {
                    continue
                }
                val referralNumber = layout.value(snapshot, rowIndex, "referral_number") ?: continue
                val axisValues = buildReferralRegistrationJournalAxisValues(
                    profile = profile,
                    snapshot = snapshot,
                    layout = layout,
                    rowIndex = rowIndex,
                    periodText = upload.detectedPeriodText,
                )
                if (!axisValues.containsKey("referral_number")) {
                    continue
                }

                val sourceColumnIndex = layout.firstColumnIndex("referral_number") ?: 1
                val rowFact = RegistryRowFactAccumulator(
                    sheet = snapshot.sheet,
                    rowId = "${snapshot.sheet.entityId}:row:$rowIndex",
                    cellId = layout.firstCell(snapshot, rowIndex, "referral_number")?.entityId,
                    metric = metric,
                    numericValue = 1.0,
                    valueText = referralNumber,
                    formulaText = null,
                    periodText = upload.detectedPeriodText,
                    sourceRowIndex = rowIndex,
                    sourceColumnIndex = sourceColumnIndex,
                    axisValues = axisValues,
                )
                rowFacts += rowFact

                val sectionAccumulator = getOrCreateSection(
                    upload = upload,
                    profile = profile,
                    sheet = snapshot.sheet,
                    rowIndex = rowIndex,
                    columnIndex = sourceColumnIndex,
                    axisValues = axisValues,
                    sectionIndex = sectionIndex,
                    nextSequence = { ++sectionSequence },
                )
                rowFact.sectionId = sectionAccumulator.entity.entityId
            }
        }

        sectionIndex.values
            .sortedWith(
                compareBy<SectionAccumulator> { it.entity.sheetId }
                    .thenBy { it.entity.rowStartIndex ?: Int.MAX_VALUE }
                    .thenBy { it.entity.columnStartIndex ?: Int.MAX_VALUE },
            )
            .forEach { accumulator ->
                normalizedSectionRepository.save(accumulator.entity)
            }

        rowFacts
            .sortedWith(
                compareBy<RegistryRowFactAccumulator> { it.sheet.sheetIndex }
                    .thenBy { it.sourceRowIndex }
                    .thenBy { it.sourceColumnIndex },
            )
            .forEach { rowFact ->
                val factId = "${upload.id}:fact:${++factSequence}"
                persistNormalizedFact(
                    upload = upload,
                    sheet = rowFact.sheet,
                    factId = factId,
                    sectionId = rowFact.sectionId
                        ?: throw DamumedReportValidationException("Referral registration journal section was not prepared for fact persistence."),
                    metric = rowFact.metric,
                    numericValue = rowFact.numericValue,
                    valueText = rowFact.valueText,
                    formulaText = rowFact.formulaText,
                    periodText = rowFact.periodText,
                    rowId = rowFact.rowId,
                    cellId = rowFact.cellId,
                    sourceRowIndex = rowFact.sourceRowIndex,
                    sourceColumnIndex = rowFact.sourceColumnIndex,
                    axisValues = rowFact.axisValues,
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

    private suspend fun normalizePositiveResultsJournal(
        upload: DamumedReportUpload,
        profile: DamumedReportNormalizationProfile,
        snapshots: List<SheetSnapshot>,
    ): NormalizationCounters {
        var sectionSequence = 0
        var dimensionSequence = 0
        var factSequence = 0
        val dimensionIndex = linkedMapOf<String, DamumedNormalizedDimensionEntity>()
        val sectionIndex = linkedMapOf<String, SectionAccumulator>()
        val rowFacts = mutableListOf<RegistryRowFactAccumulator>()
        val completedMetric = profile.metrics.firstOrNull { it.key == "completed_service_count" }
            ?: throw DamumedReportValidationException("Positive results journal completed service metric profile is not configured.")
        val positiveMetric = profile.metrics.firstOrNull { it.key == "positive_result_count" }
            ?: throw DamumedReportValidationException("Positive results journal positive result metric profile is not configured.")
        val patientMetric = profile.metrics.firstOrNull { it.key == "patient_count" }
        val serviceCostMetric = profile.metrics.firstOrNull { it.key == "service_cost" }
        val totalCostMetric = profile.metrics.firstOrNull { it.key == "service_total_cost" }

        snapshots.forEach { snapshot ->
            val layout = detectPositiveResultsJournalLayout(snapshot) ?: return@forEach
            for (rowIndex in layout.dataStartRow..layout.dataEndRow) {
                if (!isPositiveResultsJournalDataRow(snapshot, layout, rowIndex)) {
                    continue
                }
                val axisValues = buildPositiveResultsJournalAxisValues(
                    profile = profile,
                    snapshot = snapshot,
                    layout = layout,
                    rowIndex = rowIndex,
                    periodText = upload.detectedPeriodText,
                )
                val registryNumber = axisValues["registry_number"]?.displayValue ?: continue
                val sourceColumnIndex = layout.axisColumnIndex("registry_number") ?: 0
                val sectionAccumulator = getOrCreateSection(
                    upload = upload,
                    profile = profile,
                    sheet = snapshot.sheet,
                    rowIndex = rowIndex,
                    columnIndex = sourceColumnIndex,
                    axisValues = axisValues,
                    sectionIndex = sectionIndex,
                    nextSequence = { ++sectionSequence },
                )

                val completedCell = layout.metricCell(snapshot, rowIndex, "completed_service_count")
                val completedValue = completedCell?.numericValue
                    ?: parseNumeric(completedCell?.formattedValueText ?: completedCell?.rawValueText)
                    ?: 1.0
                rowFacts += RegistryRowFactAccumulator(
                    sheet = snapshot.sheet,
                    rowId = completedCell?.rowId ?: "${snapshot.sheet.entityId}:row:$rowIndex",
                    cellId = completedCell?.entityId ?: layout.axisCell(snapshot, rowIndex, "registry_number")?.entityId,
                    metric = completedMetric,
                    numericValue = completedValue,
                    valueText = completedCell?.formattedValueText ?: completedCell?.rawValueText ?: registryNumber,
                    formulaText = completedCell?.formulaText,
                    periodText = upload.detectedPeriodText,
                    sourceRowIndex = rowIndex,
                    sourceColumnIndex = layout.metricColumnIndex("completed_service_count") ?: sourceColumnIndex,
                    axisValues = axisValues,
                    sectionId = sectionAccumulator.entity.entityId,
                )

                val positiveCell = layout.metricCell(snapshot, rowIndex, "positive_result_count")
                val positiveValue = positiveCell?.numericValue
                    ?: parseNumeric(positiveCell?.formattedValueText ?: positiveCell?.rawValueText)
                    ?: 0.0
                rowFacts += RegistryRowFactAccumulator(
                    sheet = snapshot.sheet,
                    rowId = positiveCell?.rowId ?: "${snapshot.sheet.entityId}:row:$rowIndex",
                    cellId = positiveCell?.entityId ?: layout.axisCell(snapshot, rowIndex, "registry_number")?.entityId,
                    metric = positiveMetric,
                    numericValue = positiveValue,
                    valueText = positiveCell?.formattedValueText ?: positiveCell?.rawValueText,
                    formulaText = positiveCell?.formulaText,
                    periodText = upload.detectedPeriodText,
                    sourceRowIndex = rowIndex,
                    sourceColumnIndex = layout.metricColumnIndex("positive_result_count") ?: sourceColumnIndex,
                    axisValues = axisValues,
                    sectionId = sectionAccumulator.entity.entityId,
                )

                val patientCell = layout.metricCell(snapshot, rowIndex, "patient_count")
                val patientValue = patientCell?.numericValue
                    ?: parseNumeric(patientCell?.formattedValueText ?: patientCell?.rawValueText)
                if (patientMetric != null && patientValue != null) {
                    val metricCell = patientCell
                        ?: throw DamumedReportValidationException("Positive results journal patient count cell was not resolved for a numeric metric value.")
                    rowFacts += RegistryRowFactAccumulator(
                        sheet = snapshot.sheet,
                        rowId = metricCell.rowId ?: "${snapshot.sheet.entityId}:row:$rowIndex",
                        cellId = metricCell.entityId,
                        metric = patientMetric,
                        numericValue = patientValue,
                        valueText = metricCell.formattedValueText ?: metricCell.rawValueText,
                        formulaText = metricCell.formulaText,
                        periodText = upload.detectedPeriodText,
                        sourceRowIndex = rowIndex,
                        sourceColumnIndex = layout.metricColumnIndex("patient_count") ?: sourceColumnIndex,
                        axisValues = axisValues,
                        sectionId = sectionAccumulator.entity.entityId,
                    )
                }

                val serviceCostCell = layout.metricCell(snapshot, rowIndex, "service_cost")
                val serviceCostValue = serviceCostCell?.numericValue
                    ?: parseNumeric(serviceCostCell?.formattedValueText ?: serviceCostCell?.rawValueText)
                if (serviceCostMetric != null && serviceCostValue != null) {
                    val metricCell = serviceCostCell
                        ?: throw DamumedReportValidationException("Positive results journal service cost cell was not resolved for a numeric metric value.")
                    rowFacts += RegistryRowFactAccumulator(
                        sheet = snapshot.sheet,
                        rowId = metricCell.rowId ?: "${snapshot.sheet.entityId}:row:$rowIndex",
                        cellId = metricCell.entityId,
                        metric = serviceCostMetric,
                        numericValue = serviceCostValue,
                        valueText = metricCell.formattedValueText ?: metricCell.rawValueText,
                        formulaText = metricCell.formulaText,
                        periodText = upload.detectedPeriodText,
                        sourceRowIndex = rowIndex,
                        sourceColumnIndex = layout.metricColumnIndex("service_cost") ?: sourceColumnIndex,
                        axisValues = axisValues,
                        sectionId = sectionAccumulator.entity.entityId,
                    )
                }

                val totalCostCell = layout.metricCell(snapshot, rowIndex, "service_total_cost")
                val totalCostValue = totalCostCell?.numericValue
                    ?: parseNumeric(totalCostCell?.formattedValueText ?: totalCostCell?.rawValueText)
                if (totalCostMetric != null && totalCostValue != null) {
                    val metricCell = totalCostCell
                        ?: throw DamumedReportValidationException("Positive results journal total cost cell was not resolved for a numeric metric value.")
                    rowFacts += RegistryRowFactAccumulator(
                        sheet = snapshot.sheet,
                        rowId = metricCell.rowId ?: "${snapshot.sheet.entityId}:row:$rowIndex",
                        cellId = metricCell.entityId,
                        metric = totalCostMetric,
                        numericValue = totalCostValue,
                        valueText = metricCell.formattedValueText ?: metricCell.rawValueText,
                        formulaText = metricCell.formulaText,
                        periodText = upload.detectedPeriodText,
                        sourceRowIndex = rowIndex,
                        sourceColumnIndex = layout.metricColumnIndex("service_total_cost") ?: sourceColumnIndex,
                        axisValues = axisValues,
                        sectionId = sectionAccumulator.entity.entityId,
                    )
                }
            }
        }

        sectionIndex.values
            .sortedWith(
                compareBy<SectionAccumulator> { it.entity.sheetId }
                    .thenBy { it.entity.rowStartIndex ?: Int.MAX_VALUE }
                    .thenBy { it.entity.columnStartIndex ?: Int.MAX_VALUE },
            )
            .forEach { accumulator ->
                normalizedSectionRepository.save(accumulator.entity)
            }

        rowFacts
            .sortedWith(
                compareBy<RegistryRowFactAccumulator> { it.sheet.sheetIndex }
                    .thenBy { it.sourceRowIndex }
                    .thenBy { it.sourceColumnIndex },
            )
            .forEach { rowFact ->
                val factId = "${upload.id}:fact:${++factSequence}"
                persistNormalizedFact(
                    upload = upload,
                    sheet = rowFact.sheet,
                    factId = factId,
                    sectionId = rowFact.sectionId
                        ?: throw DamumedReportValidationException("Positive results journal section was not prepared for fact persistence."),
                    metric = rowFact.metric,
                    numericValue = rowFact.numericValue,
                    valueText = rowFact.valueText,
                    formulaText = rowFact.formulaText,
                    periodText = rowFact.periodText,
                    rowId = rowFact.rowId,
                    cellId = rowFact.cellId,
                    sourceRowIndex = rowFact.sourceRowIndex,
                    sourceColumnIndex = rowFact.sourceColumnIndex,
                    axisValues = rowFact.axisValues,
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

    private suspend fun normalizeGobmpCompletedServices(
        upload: DamumedReportUpload,
        profile: DamumedReportNormalizationProfile,
        snapshots: List<SheetSnapshot>,
    ): NormalizationCounters {
        var sectionSequence = 0
        var dimensionSequence = 0
        var factSequence = 0
        val dimensionIndex = linkedMapOf<String, DamumedNormalizedDimensionEntity>()
        val sectionIndex = linkedMapOf<String, SectionAccumulator>()
        val rowFacts = mutableListOf<RegistryRowFactAccumulator>()
        val completedMetric = profile.metrics.firstOrNull { it.key == "completed_count" }
            ?: throw DamumedReportValidationException("GOBMP completed services metric profile is not configured.")
        val unitPriceMetric = profile.metrics.firstOrNull { it.key == "unit_price" }
        val totalPriceMetric = profile.metrics.firstOrNull { it.key == "total_price" }

        snapshots.forEach { snapshot ->
            detectGobmpCompletedServicesBlocks(snapshot).forEach { block ->
                for (rowIndex in block.dataStartRow..block.dataEndRow) {
                    if (!isGobmpCompletedServicesDataRow(snapshot, block, rowIndex)) {
                        continue
                    }
                    val axisValues = buildGobmpCompletedServicesAxisValues(
                        profile = profile,
                        snapshot = snapshot,
                        block = block,
                        rowIndex = rowIndex,
                        periodText = upload.detectedPeriodText,
                    )
                    val registryNumber = axisValues["registry_number"]?.displayValue ?: continue
                    val sourceColumnIndex = block.axisColumnIndex("registry_number") ?: 0
                    val sectionAccumulator = getOrCreateSection(
                        upload = upload,
                        profile = profile,
                        sheet = snapshot.sheet,
                        rowIndex = rowIndex,
                        columnIndex = sourceColumnIndex,
                        axisValues = axisValues,
                        sectionIndex = sectionIndex,
                        nextSequence = { ++sectionSequence },
                    )

                    val completedCell = block.metricCell(snapshot, rowIndex, "completed_count")
                    val completedCount = completedCell?.numericValue
                        ?: parseNumeric(completedCell?.formattedValueText ?: completedCell?.rawValueText)
                        ?: 1.0
                    rowFacts += RegistryRowFactAccumulator(
                        sheet = snapshot.sheet,
                        rowId = completedCell?.rowId ?: "${snapshot.sheet.entityId}:row:$rowIndex",
                        cellId = completedCell?.entityId ?: block.axisCell(snapshot, rowIndex, "registry_number")?.entityId,
                        metric = completedMetric,
                        numericValue = completedCount,
                        valueText = completedCell?.formattedValueText ?: completedCell?.rawValueText ?: registryNumber,
                        formulaText = completedCell?.formulaText,
                        periodText = upload.detectedPeriodText,
                        sourceRowIndex = rowIndex,
                        sourceColumnIndex = block.metricColumnIndex("completed_count") ?: sourceColumnIndex,
                        axisValues = axisValues,
                        sectionId = sectionAccumulator.entity.entityId,
                    )

                    val unitPriceCell = block.metricCell(snapshot, rowIndex, "unit_price")
                    val unitPrice = unitPriceCell?.numericValue
                        ?: parseNumeric(unitPriceCell?.formattedValueText ?: unitPriceCell?.rawValueText)
                    if (unitPriceMetric != null && unitPrice != null) {
                        val metricCell = unitPriceCell
                            ?: throw DamumedReportValidationException("GOBMP completed services unit price cell was not resolved for a numeric metric value.")
                        rowFacts += RegistryRowFactAccumulator(
                            sheet = snapshot.sheet,
                            rowId = metricCell.rowId ?: "${snapshot.sheet.entityId}:row:$rowIndex",
                            cellId = metricCell.entityId,
                            metric = unitPriceMetric,
                            numericValue = unitPrice,
                            valueText = metricCell.formattedValueText ?: metricCell.rawValueText,
                            formulaText = metricCell.formulaText,
                            periodText = upload.detectedPeriodText,
                            sourceRowIndex = rowIndex,
                            sourceColumnIndex = block.metricColumnIndex("unit_price") ?: sourceColumnIndex,
                            axisValues = axisValues,
                            sectionId = sectionAccumulator.entity.entityId,
                        )
                    }

                    val totalPriceCell = block.metricCell(snapshot, rowIndex, "total_price")
                    val totalPrice = totalPriceCell?.numericValue
                        ?: parseNumeric(totalPriceCell?.formattedValueText ?: totalPriceCell?.rawValueText)
                    if (totalPriceMetric != null && totalPrice != null) {
                        val metricCell = totalPriceCell
                            ?: throw DamumedReportValidationException("GOBMP completed services total price cell was not resolved for a numeric metric value.")
                        rowFacts += RegistryRowFactAccumulator(
                            sheet = snapshot.sheet,
                            rowId = metricCell.rowId ?: "${snapshot.sheet.entityId}:row:$rowIndex",
                            cellId = metricCell.entityId,
                            metric = totalPriceMetric,
                            numericValue = totalPrice,
                            valueText = metricCell.formattedValueText ?: metricCell.rawValueText,
                            formulaText = metricCell.formulaText,
                            periodText = upload.detectedPeriodText,
                            sourceRowIndex = rowIndex,
                            sourceColumnIndex = block.metricColumnIndex("total_price") ?: sourceColumnIndex,
                            axisValues = axisValues,
                            sectionId = sectionAccumulator.entity.entityId,
                        )
                    }
                }
            }
        }

        sectionIndex.values
            .sortedWith(
                compareBy<SectionAccumulator> { it.entity.sheetId }
                    .thenBy { it.entity.rowStartIndex ?: Int.MAX_VALUE }
                    .thenBy { it.entity.columnStartIndex ?: Int.MAX_VALUE },
            )
            .forEach { accumulator ->
                normalizedSectionRepository.save(accumulator.entity)
            }

        rowFacts
            .sortedWith(
                compareBy<RegistryRowFactAccumulator> { it.sheet.sheetIndex }
                    .thenBy { it.sourceRowIndex }
                    .thenBy { it.sourceColumnIndex },
            )
            .forEach { rowFact ->
                val factId = "${upload.id}:fact:${++factSequence}"
                persistNormalizedFact(
                    upload = upload,
                    sheet = rowFact.sheet,
                    factId = factId,
                    sectionId = rowFact.sectionId
                        ?: throw DamumedReportValidationException("GOBMP completed services section was not prepared for fact persistence."),
                    metric = rowFact.metric,
                    numericValue = rowFact.numericValue,
                    valueText = rowFact.valueText,
                    formulaText = rowFact.formulaText,
                    periodText = rowFact.periodText,
                    rowId = rowFact.rowId,
                    cellId = rowFact.cellId,
                    sourceRowIndex = rowFact.sourceRowIndex,
                    sourceColumnIndex = rowFact.sourceColumnIndex,
                    axisValues = rowFact.axisValues,
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

    private suspend fun normalizeCompletedLabStudiesJournal(
        upload: DamumedReportUpload,
        profile: DamumedReportNormalizationProfile,
        snapshots: List<SheetSnapshot>,
    ): NormalizationCounters {
        val sectionIndex = linkedMapOf<String, DamumedNormalizedSectionEntity>()
        val dimensionIndex = linkedMapOf<String, DamumedNormalizedDimensionEntity>()
        val facts = mutableListOf<DamumedNormalizedFactEntity>()
        val factDimensions = mutableListOf<DamumedNormalizedFactDimensionEntity>()
        
        var sectionSequence = 0
        var dimensionSequence = 0
        var factSequence = 0
        
        val completedMetric = profile.metrics.firstOrNull { it.key == "completed_count" }
            ?: profile.metrics.firstOrNull()
            ?: throw DamumedReportValidationException("Completed lab studies journal metric profile is not configured.")
        val totalCostMetric = profile.metrics.firstOrNull { it.key == "service_total_cost" }

        snapshots.forEach { snapshot ->
            val layout = detectCompletedLabStudiesJournalLayout(snapshot) ?: return@forEach
            
            for (rowIndex in layout.dataStartRow..layout.dataEndRow) {
                if (!isCompletedLabStudiesJournalDataRow(snapshot, layout, rowIndex)) {
                    continue
                }
                
                val axisValues = buildCompletedLabStudiesJournalAxisValues(
                    profile = profile,
                    snapshot = snapshot,
                    layout = layout,
                    rowIndex = rowIndex,
                    periodText = upload.detectedPeriodText,
                )
                val referralNumber = axisValues["referral_number"]?.displayValue ?: continue
                val sourceColumnIndex = layout.axisColumnIndex("referral_number") ?: layout.axisColumnIndex("service") ?: 0
                
                // Get or create section
                val sectionKey = "${snapshot.sheet.entityId}:row:$rowIndex"
                val section = sectionIndex.getOrPut(sectionKey) {
                    DamumedNormalizedSectionEntity(
                        entityId = "${upload.id}:section:${++sectionSequence}",
                        uploadId = upload.id,
                        reportKind = upload.reportKind,
                        sheetId = snapshot.sheet.entityId,
                        sectionKey = sectionKey,
                        sectionName = referralNumber,
                        semanticRole = profile.semanticRole.name,
                        anchorAxisKey = "referral_number",
                        anchorAxisValue = referralNumber,
                        rowStartIndex = rowIndex,
                        rowEndIndex = rowIndex,
                        columnStartIndex = sourceColumnIndex,
                        columnEndIndex = sourceColumnIndex,
                    )
                }

                // Process completed_count metric
                val completedCell = layout.metricCell(snapshot, rowIndex, "completed_count")
                val completedQuantity = completedCell?.numericValue
                    ?: parseNumeric(completedCell?.formattedValueText ?: completedCell?.rawValueText)
                    ?: 1.0
                
                val factId = "${upload.id}:fact:${++factSequence}"
                facts.add(
                    DamumedNormalizedFactEntity(
                        entityId = factId,
                        uploadId = upload.id,
                        reportKind = upload.reportKind,
                        sheetId = snapshot.sheet.entityId,
                        sectionId = section.entityId,
                        rowId = completedCell?.rowId ?: "${snapshot.sheet.entityId}:row:$rowIndex",
                        cellId = completedCell?.entityId ?: layout.axisCell(snapshot, rowIndex, "referral_number")?.entityId,
                        metricKey = completedMetric.key,
                        metricLabel = completedMetric.aliases.firstOrNull() ?: completedMetric.key,
                        numericValue = completedQuantity,
                        valueText = completedCell?.formattedValueText
                            ?: completedCell?.rawValueText
                            ?: axisValues["service"]?.displayValue
                            ?: referralNumber,
                        formulaText = completedCell?.formulaText,
                        periodText = upload.detectedPeriodText,
                        sourceRowIndex = rowIndex,
                        sourceColumnIndex = layout.metricColumnIndex("completed_count") ?: sourceColumnIndex,
                    )
                )

                // Process dimensions for completed_count fact
                axisValues.values.forEach { axisValue ->
                    val dimensionKey = "${axisValue.axis.key}:${axisValue.normalizedValue}"
                    val dimension = dimensionIndex.getOrPut(dimensionKey) {
                        DamumedNormalizedDimensionEntity(
                            entityId = "${upload.id}:dim:${++dimensionSequence}",
                            uploadId = upload.id,
                            reportKind = upload.reportKind,
                            axisKey = axisValue.axis.key,
                            axisType = axisValue.axis.type.name,
                            rawValue = axisValue.rawValue,
                            normalizedValue = axisValue.normalizedValue,
                            displayValue = axisValue.displayValue,
                            sourceSheetId = snapshot.sheet.entityId,
                            sourceRowIndex = axisValue.sourceRowIndex,
                            sourceColumnIndex = axisValue.sourceColumnIndex,
                        )
                    }
                    factDimensions.add(
                        DamumedNormalizedFactDimensionEntity(
                            entityId = "$factId:${axisValue.axis.key}",
                            factId = factId,
                            axisKey = axisValue.axis.key,
                            dimensionId = dimension.entityId,
                            rawValue = axisValue.rawValue,
                            normalizedValue = axisValue.normalizedValue,
                            sourceScope = axisValue.sourceScope,
                        )
                    )
                }

                // Process total_cost metric if present
                val totalCostCell = layout.metricCell(snapshot, rowIndex, "service_total_cost")
                val totalCost = totalCostCell?.numericValue
                    ?: parseNumeric(totalCostCell?.formattedValueText ?: totalCostCell?.rawValueText)
                
                if (totalCostMetric != null && totalCost != null) {
                    val costFactId = "${upload.id}:fact:${++factSequence}"
                    facts.add(
                        DamumedNormalizedFactEntity(
                            entityId = costFactId,
                            uploadId = upload.id,
                            reportKind = upload.reportKind,
                            sheetId = snapshot.sheet.entityId,
                            sectionId = section.entityId,
                            rowId = totalCostCell?.rowId ?: "${snapshot.sheet.entityId}:row:$rowIndex",
                            cellId = totalCostCell?.entityId,
                            metricKey = totalCostMetric.key,
                            metricLabel = totalCostMetric.aliases.firstOrNull() ?: totalCostMetric.key,
                            numericValue = totalCost,
                            valueText = totalCostCell?.formattedValueText ?: totalCostCell?.rawValueText,
                            formulaText = totalCostCell?.formulaText,
                            periodText = upload.detectedPeriodText,
                            sourceRowIndex = rowIndex,
                            sourceColumnIndex = layout.metricColumnIndex("service_total_cost") ?: sourceColumnIndex,
                        )
                    )
                    
                    // Reuse same dimensions for cost fact
                    axisValues.values.forEach { axisValue ->
                        val dimensionKey = "${axisValue.axis.key}:${axisValue.normalizedValue}"
                        val dimension = dimensionIndex[dimensionKey]!! // Already created above
                        factDimensions.add(
                            DamumedNormalizedFactDimensionEntity(
                                entityId = "$costFactId:${axisValue.axis.key}",
                                factId = costFactId,
                                axisKey = axisValue.axis.key,
                                dimensionId = dimension.entityId,
                                rawValue = axisValue.rawValue,
                                normalizedValue = axisValue.normalizedValue,
                                sourceScope = axisValue.sourceScope,
                            )
                        )
                    }
                }
            }
        }

        // Batch save all entities
        batchRepository.batchInsertSections(sectionIndex.values.toList())
        batchRepository.batchInsertDimensions(dimensionIndex.values.toList())
        batchRepository.batchInsertFacts(facts)
        batchRepository.batchInsertFactDimensions(factDimensions)

        return NormalizationCounters(
            sectionCount = sectionIndex.size,
            factCount = factSequence,
            dimensionCount = dimensionIndex.size,
        )
    }

    private suspend fun normalizeEmployeeCompletedStudiesSummary(
        upload: DamumedReportUpload,
        profile: DamumedReportNormalizationProfile,
        snapshots: List<SheetSnapshot>,
    ): NormalizationCounters {
        var sectionSequence = 0
        var dimensionSequence = 0
        var factSequence = 0
        val dimensionIndex = linkedMapOf<String, DamumedNormalizedDimensionEntity>()
        val sectionIndex = linkedMapOf<String, SectionAccumulator>()
        val aggregatedFacts = linkedMapOf<String, StructuredFactAccumulator>()

        snapshots.forEach { snapshot ->
            val periodText = upload.detectedPeriodText
            detectEmployeeCompletedStudiesBlocks(snapshot).forEach { block ->
                var currentEmployee: String? = null
                for (rowIndex in block.dataStartRow..block.dataEndRow) {
                    val employeeText = snapshot.visibleTextAt(rowIndex, block.employeeColumnIndex)?.trim()
                    if (isEmployeeCompletedStudiesEmployeeValue(employeeText)) {
                        currentEmployee = employeeText
                    }
                    val employee = currentEmployee?.takeIf { it.isNotBlank() } ?: continue
                    val metricText = snapshot.visibleTextAt(rowIndex, block.metricColumnIndex)?.trim()
                    val metric = resolveEmployeeCompletedStudiesMetric(profile, metricText) ?: continue

                    block.bucketColumns.forEach { bucket ->
                        val cell = snapshot.rawCellAt(rowIndex, bucket.columnIndex)
                        val visibleText = snapshot.visibleTextAt(rowIndex, bucket.columnIndex)
                        val numericValue = cell?.numericValue
                            ?: parseNumeric(cell?.formattedValueText ?: cell?.rawValueText ?: visibleText)
                            ?: 0.0
                        val axisValues = buildEmployeeCompletedStudiesAxisValues(
                            profile = profile,
                            periodText = periodText,
                            employee = employee,
                            bucket = bucket,
                            rowIndex = rowIndex,
                        )
                        val factKey = buildStructuredFactKey(snapshot.sheet.entityId, metric.key, axisValues)
                        val rowId = cell?.rowId ?: "${snapshot.sheet.entityId}:row:$rowIndex"
                        val valueText = cell?.formattedValueText ?: cell?.rawValueText ?: visibleText

                        val existing = aggregatedFacts[factKey]
                        if (existing == null) {
                            aggregatedFacts[factKey] = StructuredFactAccumulator(
                                sheet = snapshot.sheet,
                                rowId = rowId,
                                cellId = cell?.entityId,
                                metric = metric,
                                numericValue = numericValue,
                                valueText = valueText,
                                formulaText = cell?.formulaText,
                                periodText = periodText,
                                sourceRowIndex = rowIndex,
                                sourceColumnIndex = bucket.columnIndex,
                                axisValues = axisValues,
                            )
                        } else {
                            existing.numericValue += numericValue
                            if (existing.valueText.isNullOrBlank() && !valueText.isNullOrBlank()) {
                                existing.valueText = valueText
                            }
                            if (existing.formulaText.isNullOrBlank() && !cell?.formulaText.isNullOrBlank()) {
                                existing.formulaText = cell?.formulaText
                            }
                            if (
                                rowIndex < existing.sourceRowIndex ||
                                (rowIndex == existing.sourceRowIndex && bucket.columnIndex < existing.sourceColumnIndex)
                            ) {
                                existing.rowId = rowId
                                existing.cellId = cell?.entityId
                                existing.sourceRowIndex = rowIndex
                                existing.sourceColumnIndex = bucket.columnIndex
                            }
                        }
                    }
                }
            }
        }

        val orderedFacts = aggregatedFacts.values
            .sortedWith(
                compareBy<StructuredFactAccumulator> { it.sheet.sheetIndex }
                    .thenBy { it.sourceRowIndex }
                    .thenBy { it.sourceColumnIndex },
            )

        orderedFacts.forEach { aggregate ->
            val sectionAccumulator = getOrCreateSection(
                upload = upload,
                profile = profile,
                sheet = aggregate.sheet,
                rowIndex = aggregate.sourceRowIndex,
                columnIndex = aggregate.sourceColumnIndex,
                axisValues = aggregate.axisValues,
                sectionIndex = sectionIndex,
                nextSequence = { ++sectionSequence },
            )
            aggregate.sectionId = sectionAccumulator.entity.entityId
        }

        sectionIndex.values
            .sortedWith(
                compareBy<SectionAccumulator> { it.entity.sheetId }
                    .thenBy { it.entity.rowStartIndex ?: Int.MAX_VALUE }
                    .thenBy { it.entity.columnStartIndex ?: Int.MAX_VALUE },
            )
            .forEach { accumulator ->
                normalizedSectionRepository.save(accumulator.entity)
            }

        orderedFacts.forEach { aggregate ->
            val factId = "${upload.id}:fact:${++factSequence}"
            persistNormalizedFact(
                upload = upload,
                sheet = aggregate.sheet,
                factId = factId,
                sectionId = aggregate.sectionId
                    ?: throw DamumedReportValidationException("Employee completed studies section was not prepared for fact persistence."),
                metric = aggregate.metric,
                numericValue = aggregate.numericValue,
                valueText = aggregate.valueText,
                formulaText = aggregate.formulaText,
                periodText = aggregate.periodText,
                rowId = aggregate.rowId,
                cellId = aggregate.cellId,
                sourceRowIndex = aggregate.sourceRowIndex,
                sourceColumnIndex = aggregate.sourceColumnIndex,
                axisValues = aggregate.axisValues,
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

    private suspend fun persistNormalizedFact(
        upload: DamumedReportUpload,
        sheet: DamumedParsedSheetEntity,
        factId: String,
        sectionId: String,
        metric: DamumedReportMetricProfile,
        numericValue: Double?,
        valueText: String?,
        formulaText: String?,
        periodText: String?,
        rowId: String?,
        cellId: String?,
        sourceRowIndex: Int,
        sourceColumnIndex: Int,
        axisValues: Map<String, AxisValue>,
        dimensionIndex: MutableMap<String, DamumedNormalizedDimensionEntity>,
        nextDimensionSequence: () -> Int,
    ) {
        normalizedFactRepository.save(
            DamumedNormalizedFactEntity(
                entityId = factId,
                uploadId = upload.id,
                reportKind = upload.reportKind,
                sheetId = sheet.entityId,
                sectionId = sectionId,
                rowId = rowId,
                cellId = cellId,
                metricKey = metric.key,
                metricLabel = metric.aliases.firstOrNull() ?: metric.key,
                numericValue = numericValue,
                valueText = valueText,
                formulaText = formulaText,
                periodText = periodText,
                sourceRowIndex = sourceRowIndex,
                sourceColumnIndex = sourceColumnIndex,
            ),
        )
        persistFactDimensions(
            upload = upload,
            sheet = sheet,
            factId = factId,
            axisValues = axisValues,
            dimensionIndex = dimensionIndex,
            nextDimensionSequence = nextDimensionSequence,
        )
    }

    private suspend fun persistFactDimensions(
        upload: DamumedReportUpload,
        sheet: DamumedParsedSheetEntity,
        factId: String,
        axisValues: Map<String, AxisValue>,
        dimensionIndex: MutableMap<String, DamumedNormalizedDimensionEntity>,
        nextDimensionSequence: () -> Int,
    ) {
        axisValues.values.forEach { axisValue ->
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
                sourceSheetId = sheet.entityId,
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

    private fun buildWorkplaceAxisValues(
        profile: DamumedReportNormalizationProfile,
        periodText: String?,
        workplace: String,
        service: String,
        column: WorkplaceBlockColumn,
        sourceRowIndex: Int,
    ): Map<String, AxisValue> {
        val values = linkedMapOf<String, AxisValue>()
        profile.dimensions.firstOrNull { it.key == "period" }?.let { axis ->
            periodText?.takeIf { it.isNotBlank() }?.let { value ->
                values[axis.key] = AxisValue(
                    axis = axis,
                    rawValue = value,
                    normalizedValue = normalizeText(value),
                    displayValue = value,
                    sourceRowIndex = sourceRowIndex,
                    sourceColumnIndex = column.columnIndex,
                    sourceScope = "upload",
                )
            }
        }
        profile.dimensions.firstOrNull { it.key == "workplace" }?.let { axis ->
            values[axis.key] = AxisValue(
                axis = axis,
                rawValue = workplace,
                normalizedValue = normalizeText(workplace),
                displayValue = workplace,
                sourceRowIndex = sourceRowIndex,
                sourceColumnIndex = column.columnIndex,
                sourceScope = "row",
            )
        }
        profile.dimensions.firstOrNull { it.key == "service" }?.let { axis ->
            values[axis.key] = AxisValue(
                axis = axis,
                rawValue = service,
                normalizedValue = normalizeText(service),
                displayValue = service,
                sourceRowIndex = sourceRowIndex,
                sourceColumnIndex = column.columnIndex,
                sourceScope = "row",
            )
        }
        if (!column.isTotal) {
            profile.dimensions.firstOrNull { it.key == "department_group" }?.let { axis ->
                column.departmentGroup?.takeIf { it.isNotBlank() }?.let { value ->
                    values[axis.key] = AxisValue(
                        axis = axis,
                        rawValue = value,
                        normalizedValue = normalizeText(value),
                        displayValue = value,
                        sourceRowIndex = sourceRowIndex,
                        sourceColumnIndex = column.columnIndex,
                        sourceScope = "header",
                    )
                }
            }
            profile.dimensions.firstOrNull { it.key == "department" }?.let { axis ->
                column.department?.takeIf { it.isNotBlank() }?.let { value ->
                    values[axis.key] = AxisValue(
                        axis = axis,
                        rawValue = value,
                        normalizedValue = normalizeText(value),
                        displayValue = value,
                        sourceRowIndex = sourceRowIndex,
                        sourceColumnIndex = column.columnIndex,
                        sourceScope = "header",
                    )
                }
            }
        }
        if (column.isTotal) {
            profile.dimensions.firstOrNull { it.key == "total" }?.let { axis ->
                values[axis.key] = AxisValue(
                    axis = axis,
                    rawValue = "Всего",
                    normalizedValue = normalizeText("Всего"),
                    displayValue = "Всего",
                    sourceRowIndex = sourceRowIndex,
                    sourceColumnIndex = column.columnIndex,
                    sourceScope = "header",
                )
            }
        }
        return values
    }

    private fun detectRejectLogLayout(snapshot: SheetSnapshot): RejectLogLayout? {
        val headerRowIndex = snapshot.rows
            .map { it.rowIndex }
            .firstOrNull { rowIndex -> isRejectLogHeaderRow(snapshot, rowIndex) }
            ?: return null
        val maxRowIndex = snapshot.rows.maxOfOrNull { it.rowIndex } ?: return null
        return RejectLogLayout(
            headerRowIndex = headerRowIndex,
            dataStartRow = headerRowIndex + 1,
            dataEndRow = maxRowIndex,
            axisColumnIndexByKey = mapOf(
                "registry_number" to 0,
                "order_number" to 2,
                "patient" to 3,
                "planned_completion_at" to 4,
                "sender_organization" to 5,
                "reject_reason" to 7,
                "action_taken" to 8,
                "referring_doctor" to 9,
                "registered_by" to 10,
                "photo_flag" to 11,
            ),
        )
    }

    private fun isRejectLogHeaderRow(snapshot: SheetSnapshot, rowIndex: Int): Boolean {
        val registryNumber = normalizeText(snapshot.visibleTextAt(rowIndex, 0))
        val orderNumber = normalizeText(snapshot.visibleTextAt(rowIndex, 2))
        val patient = normalizeText(snapshot.visibleTextAt(rowIndex, 3))
        val rejectReason = normalizeText(snapshot.visibleTextAt(rowIndex, 7))
        val registeredBy = normalizeText(snapshot.visibleTextAt(rowIndex, 10))
        return registryNumber == normalizeText("№") &&
            orderNumber.contains("номер") &&
            patient.contains("фио") &&
            rejectReason.contains("брак") &&
            registeredBy.contains("зарегистрировавшего")
    }

    private fun isRejectLogDataRow(
        snapshot: SheetSnapshot,
        layout: RejectLogLayout,
        rowIndex: Int,
    ): Boolean {
        val rawRegistryCell = layout.axisCell(snapshot, rowIndex, "registry_number")
        val rawOrderCell = layout.axisCell(snapshot, rowIndex, "order_number")
        val registryNumber = layout.axisValue(snapshot, rowIndex, "registry_number")
        val orderNumber = layout.axisValue(snapshot, rowIndex, "order_number")
        val patient = layout.axisValue(snapshot, rowIndex, "patient")
        val rejectReason = layout.axisValue(snapshot, rowIndex, "reject_reason")
        return (rawRegistryCell?.cellType != "MISSING" || rawOrderCell?.cellType != "MISSING") &&
            !registryNumber.isNullOrBlank() &&
            registryNumber.any(Char::isDigit) &&
            !orderNumber.isNullOrBlank() &&
            !patient.isNullOrBlank() &&
            !rejectReason.isNullOrBlank()
    }

    private fun buildRejectLogAxisValues(
        profile: DamumedReportNormalizationProfile,
        snapshot: SheetSnapshot,
        layout: RejectLogLayout,
        rowIndex: Int,
        periodText: String?,
    ): Map<String, AxisValue> {
        val values = linkedMapOf<String, AxisValue>()
        profile.dimensions.firstOrNull { it.key == "period" }?.let { axis ->
            periodText?.takeIf { it.isNotBlank() }?.let { value ->
                values[axis.key] = AxisValue(
                    axis = axis,
                    rawValue = value,
                    normalizedValue = normalizeText(value),
                    displayValue = value,
                    sourceRowIndex = rowIndex,
                    sourceColumnIndex = layout.axisColumnIndex("order_number") ?: 2,
                    sourceScope = "upload",
                )
            }
        }
        profile.dimensions
            .filter { it.key != "period" }
            .forEach { axis ->
                val value = layout.axisValue(snapshot, rowIndex, axis.key) ?: return@forEach
                val columnIndex = layout.axisColumnIndex(axis.key) ?: return@forEach
                values[axis.key] = AxisValue(
                    axis = axis,
                    rawValue = value,
                    normalizedValue = normalizeText(value),
                    displayValue = value,
                    sourceRowIndex = rowIndex,
                    sourceColumnIndex = columnIndex,
                    sourceScope = "row",
                )
            }
        return values
    }

    private fun buildStructuredFactKey(
        sheetId: String,
        metricKey: String,
        axisValues: Map<String, AxisValue>,
    ): String {
        val axisKey = axisValues.values
            .sortedBy { it.axis.key }
            .joinToString(separator = "|") { axisValue ->
                "${axisValue.axis.key}=${axisValue.normalizedValue}"
            }
        return "$sheetId::$metricKey::$axisKey"
    }

    private fun detectReferralRegistrationJournalLayout(
        snapshot: SheetSnapshot,
        profile: DamumedReportNormalizationProfile,
    ): ReferralRegistrationJournalLayout? {
        val headerRowIndex = snapshot.rows
            .map { it.rowIndex }
            .firstOrNull { rowIndex -> isReferralRegistrationJournalHeaderRow(snapshot, rowIndex) }
            ?: return null
        val maxRowIndex = snapshot.rows.maxOfOrNull { it.rowIndex } ?: return null
        val columnsByAxisKey = linkedMapOf<String, List<Int>>()

        profile.dimensions
            .filter { it.key != "period" }
            .forEach { axis ->
                val columns = (0..snapshot.maxColumnIndex())
                    .filter { columnIndex ->
                        val headerText = snapshot.visibleTextAt(headerRowIndex, columnIndex)?.trim().orEmpty()
                        headerText.isNotBlank() && axis.aliases.any { alias -> normalizeText(headerText) == normalizeText(alias) }
                    }
                if (columns.isNotEmpty()) {
                    columnsByAxisKey[axis.key] = columns
                }
            }

        if (!columnsByAxisKey.containsKey("referral_number") || !columnsByAxisKey.containsKey("service")) {
            return null
        }

        return ReferralRegistrationJournalLayout(
            headerRowIndex = headerRowIndex,
            dataStartRow = headerRowIndex + 1,
            dataEndRow = maxRowIndex,
            columnsByAxisKey = columnsByAxisKey,
        )
    }

    private fun isReferralRegistrationJournalHeaderRow(snapshot: SheetSnapshot, rowIndex: Int): Boolean {
        val referralNumber = normalizeText(snapshot.visibleTextAt(rowIndex, 1))
        val patientName = normalizeText(snapshot.visibleTextAt(rowIndex, 3))
        val service = normalizeText(snapshot.visibleTextAt(rowIndex, 15))
        return referralNumber == normalizeText("№ Направления") &&
            patientName == normalizeText("ФИО пациента") &&
            service == normalizeText("Наименование услуги")
    }

    private fun isReferralRegistrationJournalDataRow(
        snapshot: SheetSnapshot,
        layout: ReferralRegistrationJournalLayout,
        rowIndex: Int,
    ): Boolean {
        val referralNumber = layout.value(snapshot, rowIndex, "referral_number")
        val service = layout.value(snapshot, rowIndex, "service")
        val patientName = layout.value(snapshot, rowIndex, "patient_name")
        return !referralNumber.isNullOrBlank() && (!service.isNullOrBlank() || !patientName.isNullOrBlank())
    }

    private fun buildReferralRegistrationJournalAxisValues(
        profile: DamumedReportNormalizationProfile,
        snapshot: SheetSnapshot,
        layout: ReferralRegistrationJournalLayout,
        rowIndex: Int,
        periodText: String?,
    ): Map<String, AxisValue> {
        val values = linkedMapOf<String, AxisValue>()
        profile.dimensions.firstOrNull { it.key == "period" }?.let { axis ->
            periodText?.takeIf { it.isNotBlank() }?.let { value ->
                values[axis.key] = AxisValue(
                    axis = axis,
                    rawValue = value,
                    normalizedValue = normalizeText(value),
                    displayValue = value,
                    sourceRowIndex = rowIndex,
                    sourceColumnIndex = layout.firstColumnIndex(axis.key) ?: 0,
                    sourceScope = "upload",
                )
            }
        }
        profile.dimensions
            .filter { it.key != "period" }
            .forEach { axis ->
                val value = layout.value(snapshot, rowIndex, axis.key) ?: return@forEach
                values[axis.key] = AxisValue(
                    axis = axis,
                    rawValue = value,
                    normalizedValue = normalizeText(value),
                    displayValue = value,
                    sourceRowIndex = rowIndex,
                    sourceColumnIndex = layout.firstColumnIndex(axis.key) ?: 0,
                    sourceScope = "row",
                )
            }
        return values
    }

    private fun detectCompletedLabStudiesJournalLayout(snapshot: SheetSnapshot): CompletedLabStudiesJournalLayout? {
        val headerRowIndex = snapshot.rows
            .map { it.rowIndex }
            .firstOrNull { rowIndex -> isCompletedLabStudiesJournalHeaderRow(snapshot, rowIndex) }
            ?: return null
        val maxRowIndex = snapshot.rows.maxOfOrNull { it.rowIndex } ?: return null
        return CompletedLabStudiesJournalLayout(
            headerRowIndex = headerRowIndex,
            dataStartRow = headerRowIndex + 1,
            dataEndRow = maxRowIndex,
            axisColumnIndexByKey = mapOf(
                "registry_number" to 0,
                "referral_status" to 1,
                "patient_iin" to 2,
                "patient_name" to 3,
                "patient_rpn_id" to 4,
                "birth_date" to 6,
                "sample_collected_at" to 7,
                "emergency_flag" to 8,
                "organization" to 9,
                "referring_employee" to 10,
                "department" to 11,
                "medical_record_number" to 12,
                "diagnosis" to 14,
                "service_category" to 15,
                "service" to 16,
                "referral_number" to 17,
                "completed_at" to 18,
                "result_text" to 19,
                "performer" to 20,
                "funding_source" to 21,
                "service_price" to 22,
            ),
            metricColumnIndexByKey = mapOf(
                "completed_count" to 23,
                "service_total_cost" to 24,
            ),
        )
    }

    private fun isCompletedLabStudiesJournalHeaderRow(snapshot: SheetSnapshot, rowIndex: Int): Boolean {
        val registryNumber = normalizeText(snapshot.visibleTextAt(rowIndex, 0))
        val patientIin = normalizeText(snapshot.visibleTextAt(rowIndex, 2))
        val patientName = normalizeText(snapshot.visibleTextAt(rowIndex, 3))
        val service = normalizeText(snapshot.visibleTextAt(rowIndex, 16))
        return registryNumber == normalizeText("№") &&
            patientIin == normalizeText("ИИН") &&
            patientName == normalizeText("ФИО пациента") &&
            (service.contains("услуг") || service.contains("исслед"))
    }

    private fun isCompletedLabStudiesJournalDataRow(
        snapshot: SheetSnapshot,
        layout: CompletedLabStudiesJournalLayout,
        rowIndex: Int,
    ): Boolean {
        val registryNumber = layout.axisValue(snapshot, rowIndex, "registry_number")
        val referralNumber = layout.axisValue(snapshot, rowIndex, "referral_number")
        val service = layout.axisValue(snapshot, rowIndex, "service")
        return !registryNumber.isNullOrBlank() &&
            registryNumber.any(Char::isDigit) &&
            !referralNumber.isNullOrBlank() &&
            !service.isNullOrBlank()
    }

    private fun buildCompletedLabStudiesJournalAxisValues(
        profile: DamumedReportNormalizationProfile,
        snapshot: SheetSnapshot,
        layout: CompletedLabStudiesJournalLayout,
        rowIndex: Int,
        periodText: String?,
    ): Map<String, AxisValue> {
        val values = linkedMapOf<String, AxisValue>()
        profile.dimensions.firstOrNull { it.key == "period" }?.let { axis ->
            periodText?.takeIf { it.isNotBlank() }?.let { value ->
                values[axis.key] = AxisValue(
                    axis = axis,
                    rawValue = value,
                    normalizedValue = normalizeText(value),
                    displayValue = value,
                    sourceRowIndex = rowIndex,
                    sourceColumnIndex = layout.axisColumnIndex("referral_number") ?: 0,
                    sourceScope = "upload",
                )
            }
        }
        profile.dimensions
            .filter { it.key != "period" }
            .forEach { axis ->
                val value = layout.axisValue(snapshot, rowIndex, axis.key) ?: return@forEach
                val columnIndex = layout.axisColumnIndex(axis.key) ?: return@forEach
                values[axis.key] = AxisValue(
                    axis = axis,
                    rawValue = value,
                    normalizedValue = normalizeText(value),
                    displayValue = value,
                    sourceRowIndex = rowIndex,
                    sourceColumnIndex = columnIndex,
                    sourceScope = "row",
                )
            }
        return values
    }

    private fun detectPositiveResultsJournalLayout(snapshot: SheetSnapshot): PositiveResultsJournalLayout? {
        val headerRowIndex = snapshot.rows
            .map { it.rowIndex }
            .firstOrNull { rowIndex -> isPositiveResultsJournalHeaderRow(snapshot, rowIndex) }
            ?: return null
        val maxRowIndex = snapshot.rows.maxOfOrNull { it.rowIndex } ?: return null
        return PositiveResultsJournalLayout(
            headerRowIndex = headerRowIndex,
            dataStartRow = headerRowIndex + 1,
            dataEndRow = maxRowIndex,
            axisColumnIndexByKey = mapOf(
                "registry_number" to 0,
                "service_code" to 1,
                "service" to 2,
                "result_parameter" to 3,
            ),
            metricColumnIndexByKey = mapOf(
                "completed_service_count" to 5,
                "positive_result_count" to 7,
                "patient_count" to 8,
                "service_cost" to 9,
                "service_total_cost" to 10,
            ),
        )
    }

    private fun isPositiveResultsJournalHeaderRow(snapshot: SheetSnapshot, rowIndex: Int): Boolean {
        val registryNumber = normalizeText(snapshot.visibleTextAt(rowIndex, 0))
        val serviceCode = normalizeText(snapshot.visibleTextAt(rowIndex, 1))
        val service = normalizeText(snapshot.visibleTextAt(rowIndex, 2))
        val parameter = normalizeText(snapshot.visibleTextAt(rowIndex, 3))
        val completed = normalizeText(snapshot.visibleTextAt(rowIndex, 5))
        val positive = normalizeText(snapshot.visibleTextAt(rowIndex, 7))
        return registryNumber == normalizeText("№") &&
            serviceCode == normalizeText("Код услуги") &&
            service.contains("наименование") &&
            parameter.contains("параметр") &&
            completed.contains("выполненных") &&
            positive.contains("положительных")
    }

    private fun isPositiveResultsJournalDataRow(
        snapshot: SheetSnapshot,
        layout: PositiveResultsJournalLayout,
        rowIndex: Int,
    ): Boolean {
        val registryNumber = layout.axisValue(snapshot, rowIndex, "registry_number")
        val serviceCode = layout.axisValue(snapshot, rowIndex, "service_code")
        val service = layout.axisValue(snapshot, rowIndex, "service")
        val resultParameter = layout.axisValue(snapshot, rowIndex, "result_parameter")
        return !registryNumber.isNullOrBlank() &&
            registryNumber.any(Char::isDigit) &&
            !serviceCode.isNullOrBlank() &&
            !service.isNullOrBlank() &&
            !resultParameter.isNullOrBlank()
    }

    private fun buildPositiveResultsJournalAxisValues(
        profile: DamumedReportNormalizationProfile,
        snapshot: SheetSnapshot,
        layout: PositiveResultsJournalLayout,
        rowIndex: Int,
        periodText: String?,
    ): Map<String, AxisValue> {
        val values = linkedMapOf<String, AxisValue>()
        profile.dimensions.firstOrNull { it.key == "period" }?.let { axis ->
            periodText?.takeIf { it.isNotBlank() }?.let { value ->
                values[axis.key] = AxisValue(
                    axis = axis,
                    rawValue = value,
                    normalizedValue = normalizeText(value),
                    displayValue = value,
                    sourceRowIndex = rowIndex,
                    sourceColumnIndex = layout.axisColumnIndex("registry_number") ?: 0,
                    sourceScope = "upload",
                )
            }
        }
        profile.dimensions
            .filter { it.key != "period" }
            .forEach { axis ->
                val value = layout.axisValue(snapshot, rowIndex, axis.key) ?: return@forEach
                val columnIndex = layout.axisColumnIndex(axis.key) ?: return@forEach
                values[axis.key] = AxisValue(
                    axis = axis,
                    rawValue = value,
                    normalizedValue = normalizeText(value),
                    displayValue = value,
                    sourceRowIndex = rowIndex,
                    sourceColumnIndex = columnIndex,
                    sourceScope = "row",
                )
            }
        return values
    }

    private fun detectGobmpCompletedServicesBlocks(snapshot: SheetSnapshot): List<GobmpCompletedServicesBlock> {
        val maxRowIndex = snapshot.rows.maxOfOrNull { it.rowIndex } ?: return emptyList()
        val blocks = mutableListOf<GobmpCompletedServicesBlock>()
        var rowIndex = 0
        while (rowIndex <= maxRowIndex) {
            if (!isGobmpCompletedServicesHeaderRow(snapshot, rowIndex)) {
                rowIndex += 1
                continue
            }
            val dataStartRow = rowIndex + 1
            var dataEndRow = maxRowIndex
            for (probeRow in dataStartRow..maxRowIndex) {
                if (probeRow > rowIndex && isGobmpCompletedServicesHeaderRow(snapshot, probeRow)) {
                    dataEndRow = probeRow - 1
                    break
                }
            }
            blocks += GobmpCompletedServicesBlock(
                headerRowIndex = rowIndex,
                dataStartRow = dataStartRow,
                dataEndRow = dataEndRow,
                axisColumnIndexByKey = mapOf(
                    "registry_number" to 0,
                    "referring_branch" to 2,
                    "executor_organization" to 4,
                    "laboratory" to 7,
                    "department" to 8,
                    "service_code" to 9,
                    "service" to 10,
                    "service_registry_id" to 20,
                ),
                metricColumnIndexByKey = mapOf(
                    "completed_count" to 16,
                    "unit_price" to 18,
                    "total_price" to 19,
                ),
            )
            rowIndex = dataEndRow + 1
        }
        return blocks
    }

    private fun isGobmpCompletedServicesHeaderRow(snapshot: SheetSnapshot, rowIndex: Int): Boolean {
        val registryNumber = normalizeText(snapshot.visibleTextAt(rowIndex, 0))
        val serviceCode = normalizeText(snapshot.visibleTextAt(rowIndex, 9))
        val service = normalizeText(snapshot.visibleTextAt(rowIndex, 10))
        val quantity = normalizeText(snapshot.visibleTextAt(rowIndex, 16))
        return registryNumber == normalizeText("№") &&
            serviceCode == normalizeText("Код услуги") &&
            (service.contains("наименование") || service.contains("услуги")) &&
            quantity.contains("количество")
    }

    private fun isGobmpCompletedServicesDataRow(
        snapshot: SheetSnapshot,
        block: GobmpCompletedServicesBlock,
        rowIndex: Int,
    ): Boolean {
        val registryNumber = block.axisValue(snapshot, rowIndex, "registry_number")
        val serviceCode = block.axisValue(snapshot, rowIndex, "service_code")
        val service = block.axisValue(snapshot, rowIndex, "service")
        return !registryNumber.isNullOrBlank() &&
            registryNumber.any(Char::isDigit) &&
            !serviceCode.isNullOrBlank() &&
            !service.isNullOrBlank()
    }

    private fun buildGobmpCompletedServicesAxisValues(
        profile: DamumedReportNormalizationProfile,
        snapshot: SheetSnapshot,
        block: GobmpCompletedServicesBlock,
        rowIndex: Int,
        periodText: String?,
    ): Map<String, AxisValue> {
        val values = linkedMapOf<String, AxisValue>()
        profile.dimensions.firstOrNull { it.key == "period" }?.let { axis ->
            periodText?.takeIf { it.isNotBlank() }?.let { value ->
                values[axis.key] = AxisValue(
                    axis = axis,
                    rawValue = value,
                    normalizedValue = normalizeText(value),
                    displayValue = value,
                    sourceRowIndex = rowIndex,
                    sourceColumnIndex = block.axisColumnIndex("registry_number") ?: 0,
                    sourceScope = "upload",
                )
            }
        }
        profile.dimensions
            .filter { it.key != "period" }
            .forEach { axis ->
                val value = block.axisValue(snapshot, rowIndex, axis.key) ?: return@forEach
                val columnIndex = block.axisColumnIndex(axis.key) ?: return@forEach
                values[axis.key] = AxisValue(
                    axis = axis,
                    rawValue = value,
                    normalizedValue = normalizeText(value),
                    displayValue = value,
                    sourceRowIndex = rowIndex,
                    sourceColumnIndex = columnIndex,
                    sourceScope = "row",
                )
            }
        return values
    }

    private fun detectEmployeeCompletedStudiesBlocks(snapshot: SheetSnapshot): List<EmployeeCompletedStudiesBlock> {
        val maxRowIndex = snapshot.rows.maxOfOrNull { it.rowIndex } ?: return emptyList()
        val blocks = mutableListOf<EmployeeCompletedStudiesBlock>()
        var rowIndex = 0
        while (rowIndex <= maxRowIndex) {
            if (!isEmployeeCompletedStudiesBlockHeader(snapshot, rowIndex)) {
                rowIndex += 1
                continue
            }
            val dataStartRow = findEmployeeCompletedStudiesDataStartRow(snapshot, rowIndex)
            if (dataStartRow == null) {
                rowIndex += 1
                continue
            }
            var dataEndRow = maxRowIndex
            for (probeRow in (dataStartRow + 1)..maxRowIndex) {
                if (isEmployeeCompletedStudiesBlockHeader(snapshot, probeRow)) {
                    dataEndRow = probeRow - 1
                    break
                }
            }
            val bucketColumns = buildEmployeeCompletedStudiesBucketColumns(snapshot, rowIndex, dataStartRow)
            if (bucketColumns.isNotEmpty()) {
                blocks += EmployeeCompletedStudiesBlock(
                    headerRowIndex = rowIndex,
                    dataStartRow = dataStartRow,
                    dataEndRow = dataEndRow,
                    employeeColumnIndex = 1,
                    metricColumnIndex = 2,
                    bucketColumns = bucketColumns,
                )
            }
            rowIndex = dataEndRow + 1
        }
        return blocks
    }

    private fun isEmployeeCompletedStudiesBlockHeader(snapshot: SheetSnapshot, rowIndex: Int): Boolean {
        return normalizeText(snapshot.visibleTextAt(rowIndex, 1)) == normalizeText("ФИО сотрудника")
    }

    private fun findEmployeeCompletedStudiesDataStartRow(snapshot: SheetSnapshot, headerRowIndex: Int): Int? {
        val maxRowIndex = snapshot.rows.maxOfOrNull { it.rowIndex } ?: return null
        val searchEnd = minOf(maxRowIndex, headerRowIndex + 6)
        for (rowIndex in (headerRowIndex + 1)..searchEnd) {
            if (isEmployeeCompletedStudiesMetricRow(snapshot, rowIndex)) {
                return rowIndex
            }
        }
        return null
    }

    private fun isEmployeeCompletedStudiesMetricRow(snapshot: SheetSnapshot, rowIndex: Int): Boolean {
        val metricText = snapshot.visibleTextAt(rowIndex, 2)?.trim().orEmpty()
        if (!isEmployeeCompletedStudiesMetricLabel(metricText)) {
            return false
        }
        return (3..snapshot.maxColumnIndex()).any { columnIndex ->
            parseNumeric(snapshot.visibleTextAt(rowIndex, columnIndex)) != null
        }
    }

    private fun buildEmployeeCompletedStudiesBucketColumns(
        snapshot: SheetSnapshot,
        headerRowIndex: Int,
        dataStartRow: Int,
    ): List<EmployeeCompletedStudiesBucketColumn> {
        val headerRows = (headerRowIndex until dataStartRow).toList()
        return (3..snapshot.maxColumnIndex())
            .mapNotNull { columnIndex ->
                val headerTexts = headerRows
                    .mapNotNull { rowIndex -> snapshot.visibleTextAt(rowIndex, columnIndex) }
                    .map(String::trim)
                    .filter { it.isNotBlank() }
                    .distinct()
                val label = headerTexts
                    .lastOrNull { text ->
                        text.isNotBlank() &&
                            !Regex("20\\d{2}").matches(text) &&
                            normalizeText(text) != normalizeText("ФИО сотрудника")
                    }
                    ?: return@mapNotNull null
                EmployeeCompletedStudiesBucketColumn(
                    columnIndex = columnIndex,
                    label = label,
                    isTotal = normalizeText(label) == normalizeText("Итого"),
                )
            }
    }

    private fun resolveEmployeeCompletedStudiesMetric(
        profile: DamumedReportNormalizationProfile,
        metricText: String?,
    ): DamumedReportMetricProfile? {
        val normalizedMetric = normalizeText(metricText)
        if (normalizedMetric.isBlank()) {
            return null
        }
        return profile.metrics.firstOrNull { metric ->
            metric.aliases.any { alias -> normalizedMetric.contains(normalizeText(alias)) }
        }
    }

    private fun buildEmployeeCompletedStudiesAxisValues(
        profile: DamumedReportNormalizationProfile,
        periodText: String?,
        employee: String,
        bucket: EmployeeCompletedStudiesBucketColumn,
        rowIndex: Int,
    ): Map<String, AxisValue> {
        val values = linkedMapOf<String, AxisValue>()
        profile.dimensions.firstOrNull { it.key == "period" }?.let { axis ->
            periodText?.takeIf { it.isNotBlank() }?.let { value ->
                values[axis.key] = AxisValue(
                    axis = axis,
                    rawValue = value,
                    normalizedValue = normalizeText(value),
                    displayValue = value,
                    sourceRowIndex = rowIndex,
                    sourceColumnIndex = bucket.columnIndex,
                    sourceScope = "upload",
                )
            }
        }
        profile.dimensions.firstOrNull { it.key == "employee" }?.let { axis ->
            values[axis.key] = AxisValue(
                axis = axis,
                rawValue = employee,
                normalizedValue = normalizeText(employee),
                displayValue = employee,
                sourceRowIndex = rowIndex,
                sourceColumnIndex = bucket.columnIndex,
                sourceScope = "row",
            )
        }
        profile.dimensions.firstOrNull { it.key == "period_bucket" }?.let { axis ->
            values[axis.key] = AxisValue(
                axis = axis,
                rawValue = bucket.label,
                normalizedValue = normalizeText(bucket.label),
                displayValue = bucket.label,
                sourceRowIndex = rowIndex,
                sourceColumnIndex = bucket.columnIndex,
                sourceScope = "header",
            )
        }
        return values
    }

    private fun isEmployeeCompletedStudiesMetricLabel(text: String?): Boolean {
        return normalizeText(text) in setOf(
            normalizeText("Количество услуг"),
            normalizeText("Количество пациентов"),
        )
    }

    private fun isEmployeeCompletedStudiesEmployeeValue(text: String?): Boolean {
        val normalized = normalizeText(text)
        if (normalized.isBlank()) {
            return false
        }
        if (normalized in setOf(
                normalizeText("ФИО сотрудника"),
                normalizeText("Количество услуг"),
                normalizeText("Количество пациентов"),
                normalizeText("Итого"),
                normalizeText("Всего за год"),
            )
        ) {
            return false
        }
        if (Regex("20\\d{2}").matches(text.orEmpty().trim())) {
            return false
        }
        return !looksLikePeriodText(text.orEmpty())
    }

    private fun detectWorkplaceBlocks(snapshot: SheetSnapshot): List<WorkplaceBlock> {
        val maxRowIndex = snapshot.rows.maxOfOrNull { it.rowIndex } ?: return emptyList()
        val blocks = mutableListOf<WorkplaceBlock>()
        var rowIndex = 0
        while (rowIndex <= maxRowIndex) {
            if (!isWorkplaceBlockHeader(snapshot, rowIndex)) {
                rowIndex += 1
                continue
            }
            val dataStartRow = findWorkplaceBlockDataStartRow(snapshot, rowIndex)
            if (dataStartRow == null) {
                rowIndex += 1
                continue
            }
            var dataEndRow = maxRowIndex
            for (probeRow in (dataStartRow + 1)..maxRowIndex) {
                if (isWorkplaceBlockHeader(snapshot, probeRow)) {
                    dataEndRow = probeRow - 1
                    break
                }
            }
            val columns = buildWorkplaceBlockColumns(snapshot, rowIndex, dataStartRow)
            if (columns.isNotEmpty()) {
                blocks += WorkplaceBlock(
                    headerRowIndex = rowIndex,
                    dataStartRow = dataStartRow,
                    dataEndRow = dataEndRow,
                    columns = columns,
                )
            }
            rowIndex = dataEndRow + 1
        }
        return blocks
    }

    private fun isWorkplaceBlockHeader(snapshot: SheetSnapshot, rowIndex: Int): Boolean {
        val first = normalizeText(snapshot.visibleTextAt(rowIndex, 0))
        val second = normalizeText(snapshot.visibleTextAt(rowIndex, 1))
        return first == "рабочее место" && second == "услуга"
    }

    private fun findWorkplaceBlockDataStartRow(snapshot: SheetSnapshot, headerRowIndex: Int): Int? {
        val maxRowIndex = snapshot.rows.maxOfOrNull { it.rowIndex } ?: return null
        val searchEnd = minOf(maxRowIndex, headerRowIndex + 6)
        for (rowIndex in (headerRowIndex + 1)..searchEnd) {
            if (isWorkplaceBlockHeader(snapshot, rowIndex)) {
                return null
            }
            if (isPotentialWorkplaceDataRow(snapshot, rowIndex)) {
                return rowIndex
            }
        }
        return null
    }

    private fun isPotentialWorkplaceDataRow(snapshot: SheetSnapshot, rowIndex: Int): Boolean {
        val hasNumericCells = snapshot.cellsByRow[rowIndex].orEmpty().any { projectedCell ->
            val sourceColumnIndex = projectedCell.sourceColumnIndex ?: return@any false
            if (sourceColumnIndex < 2) {
                return@any false
            }
            projectedCell.cell.numericValue != null ||
                projectedCell.cell.formulaText != null ||
                parseNumeric(
                    projectedCell.cell.formattedValueText
                        ?: projectedCell.cell.rawValueText
                        ?: snapshot.visibleTextAt(rowIndex, sourceColumnIndex),
                ) != null
        }
        if (!hasNumericCells) {
            return false
        }
        val serviceText = snapshot.visibleTextAt(rowIndex, 1)
        val workplaceText = snapshot.visibleTextAt(rowIndex, 0)
        return isWorkplaceReportServiceValue(serviceText) || isWorkplaceReportWorkplaceValue(workplaceText)
    }

    private fun buildWorkplaceBlockColumns(
        snapshot: SheetSnapshot,
        headerRowIndex: Int,
        dataStartRow: Int,
    ): List<WorkplaceBlockColumn> {
        val headerRows = (headerRowIndex until dataStartRow).toList()
        val columns = mutableListOf<WorkplaceBlockColumn>()
        for (columnIndex in 2..snapshot.maxColumnIndex()) {
            val headerTexts = headerRows
                .mapNotNull { rowIndex -> snapshot.visibleTextAt(rowIndex, columnIndex) }
                .map(String::trim)
                .filter { it.isNotBlank() }
                .distinct()
            if (headerTexts.isEmpty()) {
                continue
            }
            val isTotal = headerTexts.any(::isTotalHeaderText)
            val departmentGroup = headerTexts.firstOrNull(::isDepartmentGroupHeaderText)
            val department = if (isTotal) {
                null
            } else {
                headerTexts.asReversed().firstOrNull(::isDepartmentHeaderText)
            }
            if (!isTotal && department.isNullOrBlank()) {
                continue
            }
            columns += WorkplaceBlockColumn(
                columnIndex = columnIndex,
                departmentGroup = departmentGroup,
                department = department,
                isTotal = isTotal,
            )
        }
        return columns
    }

    private fun isDepartmentGroupHeaderText(text: String): Boolean {
        return normalizeText(text) in setOf("амбулатория", "стационар")
    }

    private fun isDepartmentHeaderText(text: String): Boolean {
        val normalized = normalizeText(text)
        return normalized.isNotBlank() &&
            normalized !in setOf("рабочее место", "услуга", "отделение", "период", "амбулатория", "стационар", "всего", "итого")
    }

    private fun isTotalHeaderText(text: String): Boolean {
        val normalized = normalizeText(text)
        return normalized == "всего" || normalized.startsWith("всего ") || normalized == "итого"
    }

    private fun isWorkplaceReportWorkplaceValue(text: String?): Boolean {
        val normalized = normalizeText(text)
        if (normalized.isBlank()) {
            return false
        }
        if (normalized in setOf("рабочее место", "услуга", "всего", "итого")) {
            return false
        }
        return !looksLikePeriodText(text.orEmpty())
    }

    private fun isWorkplaceReportServiceValue(text: String?): Boolean {
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

    private fun resolveWorkplaceReportPeriod(snapshot: SheetSnapshot, upload: DamumedReportUpload): String? {
        if (!upload.detectedPeriodText.isNullOrBlank()) {
            return upload.detectedPeriodText
        }
        val maxRowIndex = minOf(snapshot.rows.maxOfOrNull { it.rowIndex } ?: 0, 12)
        val maxColumnIndex = minOf(snapshot.maxColumnIndex(), 8)
        for (rowIndex in 0..maxRowIndex) {
            for (columnIndex in 0..maxColumnIndex) {
                val value = snapshot.visibleTextAt(rowIndex, columnIndex)?.trim().orEmpty()
                if (value.isNotBlank() && looksLikePeriodText(value) && normalizeText(value) != "период") {
                    return value
                }
            }
        }
        return null
    }

    private suspend fun buildSheetSnapshot(sheet: DamumedParsedSheetEntity): SheetSnapshot {
        val rows = parsedRowRepository.findAllBySheetIdOrderByRowIndexAsc(sheet.entityId).toList()
        val cells = parsedCellRepository.findAllBySheetIdOrderByRowIndexAscColumnIndexAsc(sheet.entityId).toList()
        val mergedRegions = parsedMergedRegionRepository.findAllBySheetIdOrderByRegionIndexAsc(sheet.entityId).toList()
        return SheetSnapshot(
            sheet = sheet,
            rows = rows,
            cellsByKey = cells.associateBy { it.rowIndex to it.columnIndex },
            cellsByRow = SheetSnapshot.projectCellsByRow(
                cellsByKey = cells.associateBy { it.rowIndex to it.columnIndex },
                cellsByRow = cells.groupBy { it.rowIndex },
                mergedRegions = mergedRegions,
            ),
            mergedRegions = mergedRegions,
        )
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

    private fun isPotentialFactCell(cell: DamumedParsedCellEntity): Boolean {
        if (cell.cellType == "MISSING" || cell.cellType == "BLANK") {
            return false
        }
        if (cell.numericValue != null) {
            return true
        }
        if (cell.formulaText != null) {
            return true
        }
        return parseNumeric(cell.formattedValueText) != null
    }

    private fun resolveMetric(
        profile: DamumedReportNormalizationProfile,
        headerTexts: List<String>,
        rowTexts: List<String>,
        cell: DamumedParsedCellEntity,
    ): DamumedReportMetricProfile? {
        val context = (headerTexts + rowTexts + listOfNotNull(cell.formattedValueText)).map(::normalizeText)
        val explicit = profile.metrics.firstOrNull { metric ->
            metric.aliases.any { alias ->
                val normalizedAlias = normalizeText(alias)
                context.any { candidate -> candidate.contains(normalizedAlias) }
            }
        }
        if (explicit != null) {
            return explicit
        }
        if (context.any { it.contains("всего") }) {
            return profile.metrics.firstOrNull { it.representsTotal } ?: profile.metrics.firstOrNull()
        }
        return profile.metrics.singleOrNull() ?: profile.metrics.firstOrNull()
    }

    private fun resolveAxisValues(
        profile: DamumedReportNormalizationProfile,
        snapshot: SheetSnapshot,
        rowIndex: Int,
        columnIndex: Int,
        headerTexts: List<String>,
        rowTexts: List<String>,
        upload: DamumedReportUpload,
    ): Map<String, AxisValue> {
        val values = linkedMapOf<String, AxisValue>()
        val availableRowCandidates = rowTexts
            .filter { isMeaningfulAxisCandidate(it, profile) }
            .toMutableList()

        profile.dimensions.forEach { axis ->
            val resolved = when (axis.type) {
                DamumedReportAxisType.PERIOD -> resolvePeriodAxis(axis, headerTexts, upload)
                DamumedReportAxisType.DEPARTMENT_GROUP -> resolveAliasBasedAxis(axis, headerTexts + rowTexts)
                DamumedReportAxisType.FUNDING_SOURCE -> resolveAliasBasedAxis(axis, headerTexts + rowTexts)
                DamumedReportAxisType.TOTAL -> resolveTotalAxis(axis, headerTexts + rowTexts)
                DamumedReportAxisType.RESULT_FLAG -> resolveResultFlagAxis(axis, headerTexts + rowTexts, upload)
                DamumedReportAxisType.WORKPLACE -> resolveHeaderDrivenAxis(axis, headerTexts, profile)
                DamumedReportAxisType.DEPARTMENT -> resolveDepartmentAxis(axis, headerTexts, availableRowCandidates, profile)
                DamumedReportAxisType.SERVICE,
                DamumedReportAxisType.MATERIAL,
                DamumedReportAxisType.EMPLOYEE,
                DamumedReportAxisType.IDENTIFIER,
                DamumedReportAxisType.PATIENT,
                DamumedReportAxisType.ORGANIZATION,
                DamumedReportAxisType.DIAGNOSIS,
                DamumedReportAxisType.STATUS,
                DamumedReportAxisType.DATE_TIME,
                DamumedReportAxisType.COST -> resolveRowDrivenAxis(axis, availableRowCandidates)
            }
            if (resolved != null) {
                values[axis.key] = AxisValue(
                    axis = axis,
                    rawValue = resolved,
                    normalizedValue = normalizeText(resolved),
                    displayValue = resolved,
                    sourceRowIndex = rowIndex,
                    sourceColumnIndex = columnIndex,
                    sourceScope = if (headerTexts.any { normalizeText(it) == normalizeText(resolved) }) "header" else "row",
                )
            }
        }

        if (!values.containsKey("period") && !upload.detectedPeriodText.isNullOrBlank()) {
            val axis = profile.dimensions.firstOrNull { it.type == DamumedReportAxisType.PERIOD }
            if (axis != null) {
                values[axis.key] = AxisValue(
                    axis = axis,
                    rawValue = upload.detectedPeriodText,
                    normalizedValue = normalizeText(upload.detectedPeriodText),
                    displayValue = upload.detectedPeriodText,
                    sourceRowIndex = rowIndex,
                    sourceColumnIndex = columnIndex,
                    sourceScope = "upload",
                )
            }
        }

        if (values.isEmpty() && upload.detectedPeriodText != null) {
            val axis = profile.dimensions.firstOrNull() ?: return emptyMap()
            values[axis.key] = AxisValue(
                axis = axis,
                rawValue = upload.detectedPeriodText,
                normalizedValue = normalizeText(upload.detectedPeriodText),
                displayValue = upload.detectedPeriodText,
                sourceRowIndex = rowIndex,
                sourceColumnIndex = columnIndex,
                sourceScope = "upload",
            )
        }

        return values
    }

    private suspend fun getOrCreateSection(
        upload: DamumedReportUpload,
        profile: DamumedReportNormalizationProfile,
        sheet: DamumedParsedSheetEntity,
        rowIndex: Int,
        columnIndex: Int,
        axisValues: Map<String, AxisValue>,
        sectionIndex: MutableMap<String, SectionAccumulator>,
        nextSequence: () -> Int,
    ): SectionAccumulator {
        val anchor = profile.dimensions
            .firstOrNull { it.repeatedAcrossBlocks && axisValues.containsKey(it.key) }
            ?.let { axis -> axis.key to axisValues.getValue(axis.key).displayValue }
            ?: ("sheet" to sheet.sheetName)
        val sectionKey = "${sheet.entityId}:${anchor.first}:${normalizeText(anchor.second)}"
        val existing = sectionIndex[sectionKey]
        if (existing != null) {
            val updated = existing.copy(
                entity = existing.entity.copy(
                    rowStartIndex = minOf(existing.entity.rowStartIndex ?: rowIndex, rowIndex),
                    rowEndIndex = maxOf(existing.entity.rowEndIndex ?: rowIndex, rowIndex),
                    columnStartIndex = minOf(existing.entity.columnStartIndex ?: columnIndex, columnIndex),
                    columnEndIndex = maxOf(existing.entity.columnEndIndex ?: columnIndex, columnIndex),
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
                sheetId = sheet.entityId,
                sectionKey = sectionKey,
                sectionName = anchor.second,
                semanticRole = profile.semanticRole.name,
                anchorAxisKey = anchor.first,
                anchorAxisValue = anchor.second,
                rowStartIndex = rowIndex,
                rowEndIndex = rowIndex,
                columnStartIndex = columnIndex,
                columnEndIndex = columnIndex,
            ),
        )
        sectionIndex[sectionKey] = created
        return created
    }

    private fun resolvePeriodAxis(
        axis: DamumedReportAxisProfile,
        headerTexts: List<String>,
        upload: DamumedReportUpload,
    ): String? {
        return upload.detectedPeriodText
            ?.takeIf { looksLikePeriodText(it) && !matchesAnyAlias(it, axis.aliases) }
            ?: extractValueAfterAlias(headerTexts, axis.aliases)
            ?: headerTexts.firstOrNull { looksLikePeriodText(it) }
            ?: headerTexts.firstOrNull { matchesAnyAlias(it, axis.aliases) }
    }

    private fun resolveAliasBasedAxis(axis: DamumedReportAxisProfile, context: List<String>): String? {
        return context.firstOrNull { text ->
            axis.aliases.any { alias -> normalizeText(text) == normalizeText(alias) || normalizeText(text).contains(normalizeText(alias)) }
        }
    }

    private fun resolveTotalAxis(axis: DamumedReportAxisProfile, context: List<String>): String? {
        return if (context.any { matchesAnyAlias(it, axis.aliases) }) {
            axis.aliases.firstOrNull()?.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() } ?: "Всего"
        } else {
            null
        }
    }

    private fun resolveResultFlagAxis(
        axis: DamumedReportAxisProfile,
        context: List<String>,
        upload: DamumedReportUpload,
    ): String? {
        return resolveAliasBasedAxis(axis, context)
            ?: if (upload.reportKind.name.contains("POSITIVE", ignoreCase = true)) "Положительный результат" else null
    }

    private fun resolveHeaderDrivenAxis(
        axis: DamumedReportAxisProfile,
        headerTexts: List<String>,
        profile: DamumedReportNormalizationProfile,
    ): String? {
        val explicit = extractValueAfterAlias(headerTexts, axis.aliases)
        if (explicit != null) {
            return explicit
        }
        return headerTexts.firstOrNull { isMeaningfulAxisCandidate(it, profile) && !matchesMetricAlias(it, profile) }
    }

    private fun resolveDepartmentAxis(
        axis: DamumedReportAxisProfile,
        headerTexts: List<String>,
        availableRowCandidates: MutableList<String>,
        profile: DamumedReportNormalizationProfile,
    ): String? {
        val explicit = extractValueAfterAlias(headerTexts, axis.aliases)
        if (explicit != null) {
            return explicit
        }
        val headerCandidate = headerTexts.lastOrNull { candidate ->
            isMeaningfulAxisCandidate(candidate, profile) &&
                !matchesMetricAlias(candidate, profile) &&
                !matchesAnyAxisAlias(candidate, profile, axis)
        }
        if (headerCandidate != null) {
            return headerCandidate
        }
        return takeNextRowCandidate(availableRowCandidates)
    }

    private fun resolveRowDrivenAxis(
        axis: DamumedReportAxisProfile,
        availableRowCandidates: MutableList<String>,
    ): String? {
        val explicit = extractValueAfterAlias(availableRowCandidates, axis.aliases)
        if (explicit != null) {
            availableRowCandidates.removeAll { candidate ->
                normalizeText(candidate) == normalizeText(explicit)
            }
            return explicit
        }
        return takeNextRowCandidate(availableRowCandidates)
    }

    private fun takeNextRowCandidate(availableRowCandidates: MutableList<String>): String? {
        if (availableRowCandidates.isEmpty()) {
            return null
        }
        return availableRowCandidates.removeAt(0)
    }

    private fun extractValueAfterAlias(texts: List<String>, aliases: Set<String>): String? {
        texts.forEachIndexed { index, text ->
            val normalized = normalizeText(text)
            aliases.forEach { alias ->
                val normalizedAlias = normalizeText(alias)
                if (normalized == normalizedAlias) {
                    val next = texts.getOrNull(index + 1)
                    if (!next.isNullOrBlank() && normalizeText(next) != normalizedAlias) {
                        return next.trim()
                    }
                }
                if (normalized.startsWith("$normalizedAlias:")) {
                    return text.substringAfter(':').trim().takeIf { it.isNotBlank() }
                }
            }
        }
        return null
    }

    private fun matchesAnyAlias(text: String, aliases: Set<String>): Boolean {
        val normalized = normalizeText(text)
        return aliases.any { alias -> normalized.contains(normalizeText(alias)) }
    }

    private fun matchesMetricAlias(text: String, profile: DamumedReportNormalizationProfile): Boolean {
        return profile.metrics.any { metric -> matchesAnyAlias(text, metric.aliases) }
    }

    private fun matchesAnyAxisAlias(
        text: String,
        profile: DamumedReportNormalizationProfile,
        excludeAxis: DamumedReportAxisProfile? = null,
    ): Boolean {
        return profile.dimensions
            .filterNot { excludeAxis != null && it.key == excludeAxis.key }
            .any { axis -> matchesAnyAlias(text, axis.aliases) }
    }

    private fun isMeaningfulAxisCandidate(text: String, profile: DamumedReportNormalizationProfile): Boolean {
        val trimmed = text.trim()
        if (trimmed.isBlank()) {
            return false
        }
        if (looksNumeric(trimmed)) {
            return false
        }
        if (profile.titleAliases.any { normalizeText(trimmed) == normalizeText(it) }) {
            return false
        }
        return true
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
            // Must look like a date: contains digits and date separators or month names
            if (afterS.any(Char::isDigit) && 
                (Regex("[./-]").containsMatchIn(afterS) || 
                 afterS.contains("янв") || afterS.contains("фев") || afterS.contains("мар") ||
                 afterS.contains("апр") || afterS.contains("мая") || afterS.contains("июн") ||
                 afterS.contains("июл") || afterS.contains("авг") || afterS.contains("сен") ||
                 afterS.contains("окт") || afterS.contains("ноя") || afterS.contains("дек"))) {
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
                 afterPo.contains("окт") || afterPo.contains("ноя") || afterPo.contains("дек"))) {
                return true
            }
        }
        // Standard date format patterns
        if (Regex("\\d{1,2}[./-]\\d{1,2}[./-]\\d{2,4}").containsMatchIn(normalized) ||
            Regex("20\\d{2}").containsMatchIn(normalized)) {
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

    private fun looksNumeric(value: String): Boolean {
        return parseNumeric(value) != null
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

    private data class SectionAccumulator(
        val entity: DamumedNormalizedSectionEntity,
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

    private data class ProjectedCell(
        val cell: DamumedParsedCellEntity,
        val sourceColumnIndex: Int?,
    )

    private data class NormalizationCounters(
        val sectionCount: Int,
        val factCount: Int,
        val dimensionCount: Int,
    )

    private data class WorkplaceBlock(
        val headerRowIndex: Int,
        val dataStartRow: Int,
        val dataEndRow: Int,
        val columns: List<WorkplaceBlockColumn>,
    )

    private data class WorkplaceBlockColumn(
        val columnIndex: Int,
        val departmentGroup: String?,
        val department: String?,
        val isTotal: Boolean,
    )

    private data class ReferralMaterialLayout(
        val materialColumnIndex: Int,
        val dataStartRow: Int,
        val dataEndRow: Int,
        val bucketColumns: List<ReferralMaterialBucketColumn>,
    )

    private data class ReferralMaterialBucketColumn(
        val columnIndex: Int,
        val label: String,
        val isTotal: Boolean,
    )

    private data class EmployeeCompletedStudiesBlock(
        val headerRowIndex: Int,
        val dataStartRow: Int,
        val dataEndRow: Int,
        val employeeColumnIndex: Int,
        val metricColumnIndex: Int,
        val bucketColumns: List<EmployeeCompletedStudiesBucketColumn>,
    )

    private data class EmployeeCompletedStudiesBucketColumn(
        val columnIndex: Int,
        val label: String,
        val isTotal: Boolean,
    )

    private data class ReferralRegistrationJournalLayout(
        val headerRowIndex: Int,
        val dataStartRow: Int,
        val dataEndRow: Int,
        val columnsByAxisKey: Map<String, List<Int>>,
    ) {
        fun firstColumnIndex(axisKey: String): Int? = columnsByAxisKey[axisKey]?.firstOrNull()

        fun firstCell(snapshot: SheetSnapshot, rowIndex: Int, axisKey: String): DamumedParsedCellEntity? {
            return columnsByAxisKey[axisKey]
                .orEmpty()
                .firstNotNullOfOrNull { columnIndex -> snapshot.rawCellAt(rowIndex, columnIndex) }
        }

        fun value(snapshot: SheetSnapshot, rowIndex: Int, axisKey: String): String? {
            return columnsByAxisKey[axisKey]
                .orEmpty()
                .firstNotNullOfOrNull { columnIndex ->
                    snapshot.visibleTextAt(rowIndex, columnIndex)
                        ?.trim()
                        ?.takeIf { it.isNotBlank() }
                }
        }
    }

    private data class CompletedLabStudiesJournalLayout(
        val headerRowIndex: Int,
        val dataStartRow: Int,
        val dataEndRow: Int,
        val axisColumnIndexByKey: Map<String, Int>,
        val metricColumnIndexByKey: Map<String, Int>,
    ) {
        fun axisColumnIndex(axisKey: String): Int? = axisColumnIndexByKey[axisKey]

        fun metricColumnIndex(metricKey: String): Int? = metricColumnIndexByKey[metricKey]

        fun axisCell(snapshot: SheetSnapshot, rowIndex: Int, axisKey: String): DamumedParsedCellEntity? {
            val columnIndex = axisColumnIndex(axisKey) ?: return null
            return snapshot.rawCellAt(rowIndex, columnIndex)
        }

        fun metricCell(snapshot: SheetSnapshot, rowIndex: Int, metricKey: String): DamumedParsedCellEntity? {
            val columnIndex = metricColumnIndex(metricKey) ?: return null
            return snapshot.rawCellAt(rowIndex, columnIndex)
        }

        fun axisValue(snapshot: SheetSnapshot, rowIndex: Int, axisKey: String): String? {
            val columnIndex = axisColumnIndex(axisKey) ?: return null
            return snapshot.visibleTextAt(rowIndex, columnIndex)
                ?.trim()
                ?.takeIf { it.isNotBlank() }
        }
    }

    private data class PositiveResultsJournalLayout(
        val headerRowIndex: Int,
        val dataStartRow: Int,
        val dataEndRow: Int,
        val axisColumnIndexByKey: Map<String, Int>,
        val metricColumnIndexByKey: Map<String, Int>,
    ) {
        fun axisColumnIndex(axisKey: String): Int? = axisColumnIndexByKey[axisKey]

        fun metricColumnIndex(metricKey: String): Int? = metricColumnIndexByKey[metricKey]

        fun axisCell(snapshot: SheetSnapshot, rowIndex: Int, axisKey: String): DamumedParsedCellEntity? {
            val columnIndex = axisColumnIndex(axisKey) ?: return null
            return snapshot.rawCellAt(rowIndex, columnIndex)
        }

        fun metricCell(snapshot: SheetSnapshot, rowIndex: Int, metricKey: String): DamumedParsedCellEntity? {
            val columnIndex = metricColumnIndex(metricKey) ?: return null
            return snapshot.rawCellAt(rowIndex, columnIndex)
        }

        fun axisValue(snapshot: SheetSnapshot, rowIndex: Int, axisKey: String): String? {
            val columnIndex = axisColumnIndex(axisKey) ?: return null
            return snapshot.visibleTextAt(rowIndex, columnIndex)
                ?.trim()
                ?.takeIf { it.isNotBlank() }
        }
    }

    private data class RejectLogLayout(
        val headerRowIndex: Int,
        val dataStartRow: Int,
        val dataEndRow: Int,
        val axisColumnIndexByKey: Map<String, Int>,
    ) {
        fun axisColumnIndex(axisKey: String): Int? = axisColumnIndexByKey[axisKey]

        fun axisCell(snapshot: SheetSnapshot, rowIndex: Int, axisKey: String): DamumedParsedCellEntity? {
            val columnIndex = axisColumnIndex(axisKey) ?: return null
            return snapshot.rawCellAt(rowIndex, columnIndex)
        }

        fun axisValue(snapshot: SheetSnapshot, rowIndex: Int, axisKey: String): String? {
            val columnIndex = axisColumnIndex(axisKey) ?: return null
            return snapshot.visibleTextAt(rowIndex, columnIndex)
                ?.trim()
                ?.takeIf { it.isNotBlank() }
        }
    }

    private data class GobmpCompletedServicesBlock(
        val headerRowIndex: Int,
        val dataStartRow: Int,
        val dataEndRow: Int,
        val axisColumnIndexByKey: Map<String, Int>,
        val metricColumnIndexByKey: Map<String, Int>,
    ) {
        fun axisColumnIndex(axisKey: String): Int? = axisColumnIndexByKey[axisKey]

        fun metricColumnIndex(metricKey: String): Int? = metricColumnIndexByKey[metricKey]

        fun axisCell(snapshot: SheetSnapshot, rowIndex: Int, axisKey: String): DamumedParsedCellEntity? {
            val columnIndex = axisColumnIndex(axisKey) ?: return null
            return snapshot.rawCellAt(rowIndex, columnIndex)
        }

        fun metricCell(snapshot: SheetSnapshot, rowIndex: Int, metricKey: String): DamumedParsedCellEntity? {
            val columnIndex = metricColumnIndex(metricKey) ?: return null
            return snapshot.rawCellAt(rowIndex, columnIndex)
        }

        fun axisValue(snapshot: SheetSnapshot, rowIndex: Int, axisKey: String): String? {
            val columnIndex = axisColumnIndex(axisKey) ?: return null
            return snapshot.visibleTextAt(rowIndex, columnIndex)
                ?.trim()
                ?.takeIf { it.isNotBlank() }
        }
    }

    private data class RegistryRowFactAccumulator(
        val sheet: DamumedParsedSheetEntity,
        val rowId: String?,
        val cellId: String?,
        val metric: DamumedReportMetricProfile,
        val numericValue: Double,
        val valueText: String?,
        val formulaText: String?,
        val periodText: String?,
        val sourceRowIndex: Int,
        val sourceColumnIndex: Int,
        val axisValues: Map<String, AxisValue>,
        var sectionId: String? = null,
    )

    private data class StructuredFactAccumulator(
        val sheet: DamumedParsedSheetEntity,
        var rowId: String?,
        var cellId: String?,
        val metric: DamumedReportMetricProfile,
        var numericValue: Double,
        var valueText: String?,
        var formulaText: String?,
        val periodText: String?,
        var sourceRowIndex: Int,
        var sourceColumnIndex: Int,
        val axisValues: Map<String, AxisValue>,
        var sectionId: String? = null,
    )

    private data class SheetSnapshot(
        val sheet: DamumedParsedSheetEntity,
        val rows: List<DamumedParsedRowEntity>,
        val cellsByKey: Map<Pair<Int, Int>, DamumedParsedCellEntity>,
        val cellsByRow: Map<Int, List<ProjectedCell>>,
        val mergedRegions: List<DamumedParsedMergedRegionEntity>,
    ) {
        fun collectHeaderTexts(rowIndex: Int, columnIndex: Int, maxDepth: Int = 8): List<String> {
            val startRow = (rowIndex - maxDepth).coerceAtLeast(0)
            return (startRow until rowIndex)
                .mapNotNull { headerRow -> visibleTextAt(headerRow, columnIndex) }
                .map(String::trim)
                .filter { it.isNotBlank() }
                .distinct()
        }

        fun collectRowTexts(rowIndex: Int, columnIndex: Int): List<String> {
            return (0 until columnIndex)
                .mapNotNull { leftColumn -> visibleTextAt(rowIndex, leftColumn) }
                .map(String::trim)
                .filter { it.isNotBlank() }
                .distinct()
        }

        fun rawCellAt(rowIndex: Int, columnIndex: Int): DamumedParsedCellEntity? {
            return cellsByKey[rowIndex to columnIndex]
        }

        fun maxColumnIndex(): Int {
            return cellsByKey.keys.maxOfOrNull { it.second } ?: 0
        }

        fun visibleTextAt(rowIndex: Int, columnIndex: Int): String? {
            val direct = cellsByKey[rowIndex to columnIndex]
            val directText = direct?.formattedValueText?.takeIf { it.isNotBlank() }
                ?: direct?.rawValueText?.takeIf { it.isNotBlank() }
            if (!directText.isNullOrBlank()) {
                return directText
            }
            val mergedRegion = mergedRegions.firstOrNull { region ->
                rowIndex in region.firstRow..region.lastRow && columnIndex in region.firstColumn..region.lastColumn
            } ?: return null
            val topLeft = cellsByKey[mergedRegion.firstRow to mergedRegion.firstColumn]
            return topLeft?.formattedValueText?.takeIf { it.isNotBlank() }
                ?: topLeft?.rawValueText?.takeIf { it.isNotBlank() }
        }

        companion object {
            fun projectCellsByRow(
                cellsByKey: Map<Pair<Int, Int>, DamumedParsedCellEntity>,
                cellsByRow: Map<Int, List<DamumedParsedCellEntity>>,
                mergedRegions: List<DamumedParsedMergedRegionEntity>,
            ): Map<Int, List<ProjectedCell>> {
                return cellsByRow.mapValues { (_, cells) ->
                    cells.map { cell ->
                        val resolvedCell = if (cell.cellType == "MISSING") {
                            resolveTopLeftCell(cellsByKey, mergedRegions, cell) ?: cell
                        } else {
                            cell
                        }
                        ProjectedCell(
                            cell = resolvedCell,
                            sourceColumnIndex = if (cell.cellType == "MISSING") resolvedCell.columnIndex else cell.columnIndex,
                        )
                    }
                        .sortedBy { it.sourceColumnIndex ?: Int.MAX_VALUE }
                        .distinctBy { projected -> projected.cell.entityId to projected.sourceColumnIndex }
                }
            }

            private fun resolveTopLeftCell(
                cellsByKey: Map<Pair<Int, Int>, DamumedParsedCellEntity>,
                mergedRegions: List<DamumedParsedMergedRegionEntity>,
                cell: DamumedParsedCellEntity,
            ): DamumedParsedCellEntity? {
                val region = mergedRegions.firstOrNull { merged ->
                    cell.rowIndex in merged.firstRow..merged.lastRow && cell.columnIndex in merged.firstColumn..merged.lastColumn
                } ?: return null
                return cellsByKey[region.firstRow to region.firstColumn]
            }
        }
    }

    private fun detectReferralCountByMaterialLayout(snapshot: SheetSnapshot): ReferralMaterialLayout {
        val materialHeaderRowIndex = snapshot.rows
            .firstOrNull { row ->
                (0..snapshot.maxColumnIndex()).any { columnIndex ->
                    normalizeText(snapshot.visibleTextAt(row.rowIndex, columnIndex)) == normalizeText("Наименование материала")
                }
            }
            ?.rowIndex
            ?: throw DamumedReportValidationException("Material report header row was not detected.")

        val materialColumnIndex = (0..snapshot.maxColumnIndex())
            .firstOrNull { columnIndex ->
                normalizeText(snapshot.visibleTextAt(materialHeaderRowIndex, columnIndex)) == normalizeText("Наименование материала")
            }
            ?: throw DamumedReportValidationException("Material report material column was not detected.")

        val dataStartRow = snapshot.rows
            .asSequence()
            .map { it.rowIndex }
            .firstOrNull { rowIndex ->
                rowIndex > materialHeaderRowIndex &&
                    snapshot.visibleTextAt(rowIndex, materialColumnIndex)?.isNotBlank() == true &&
                    ((materialColumnIndex + 1)..snapshot.maxColumnIndex()).any { columnIndex ->
                        parseNumeric(snapshot.visibleTextAt(rowIndex, columnIndex)) != null
                    }
            }
            ?: throw DamumedReportValidationException("Material report data rows were not detected.")

        val bucketColumns = ((materialColumnIndex + 1)..snapshot.maxColumnIndex())
            .mapNotNull { columnIndex ->
                val headerTexts = (materialHeaderRowIndex until dataStartRow)
                    .mapNotNull { headerRowIndex -> snapshot.visibleTextAt(headerRowIndex, columnIndex) }
                    .map(String::trim)
                    .filter { it.isNotBlank() }
                    .distinct()
                val label = headerTexts
                    .lastOrNull { !Regex("20\\d{2}").matches(it) }
                    ?.takeIf { it.isNotBlank() }
                    ?: return@mapNotNull null
                ReferralMaterialBucketColumn(
                    columnIndex = columnIndex,
                    label = label,
                    isTotal = normalizeText(label) == normalizeText("Итого"),
                )
            }

        if (bucketColumns.isEmpty()) {
            throw DamumedReportValidationException("Material report value columns were not detected.")
        }

        val dataEndRow = snapshot.rows
            .lastOrNull { row ->
                row.rowIndex >= dataStartRow && snapshot.visibleTextAt(row.rowIndex, materialColumnIndex)?.isNotBlank() == true
            }
            ?.rowIndex
            ?: dataStartRow

        return ReferralMaterialLayout(
            materialColumnIndex = materialColumnIndex,
            dataStartRow = dataStartRow,
            dataEndRow = dataEndRow,
            bucketColumns = bucketColumns,
        )
    }

    private fun buildReferralCountByMaterialAxisValues(
        profile: DamumedReportNormalizationProfile,
        periodText: String?,
        material: String,
        bucket: ReferralMaterialBucketColumn,
        rowIndex: Int,
    ): Map<String, AxisValue> {
        val values = linkedMapOf<String, AxisValue>()
        profile.dimensions.firstOrNull { it.key == "period" }?.let { axis ->
            periodText?.takeIf { it.isNotBlank() }?.let { value ->
                values[axis.key] = AxisValue(
                    axis = axis,
                    rawValue = value,
                    normalizedValue = normalizeText(value),
                    displayValue = value,
                    sourceRowIndex = rowIndex,
                    sourceColumnIndex = bucket.columnIndex,
                    sourceScope = "upload",
                )
            }
        }
        profile.dimensions.firstOrNull { it.key == "material" }?.let { axis ->
            values[axis.key] = AxisValue(
                axis = axis,
                rawValue = material,
                normalizedValue = normalizeText(material),
                displayValue = material,
                sourceRowIndex = rowIndex,
                sourceColumnIndex = bucket.columnIndex,
                sourceScope = "row",
            )
        }
        profile.dimensions.firstOrNull { it.key == "period_bucket" }?.let { axis ->
            values[axis.key] = AxisValue(
                axis = axis,
                rawValue = bucket.label,
                normalizedValue = normalizeText(bucket.label),
                displayValue = bucket.label,
                sourceRowIndex = rowIndex,
                sourceColumnIndex = bucket.columnIndex,
                sourceScope = "header",
            )
        }
        return values
    }
}
