package lab.dev.med.univ.feature.reporting.domain.services

import kotlinx.coroutines.flow.toList
import lab.dev.med.univ.feature.reporting.data.entity.DamumedNormalizedFactDimensionEntity
import lab.dev.med.univ.feature.reporting.data.entity.DamumedNormalizedFactEntity
import lab.dev.med.univ.feature.reporting.data.entity.DamumedNormalizedSectionEntity
import lab.dev.med.univ.feature.reporting.data.repository.DamumedNormalizedFactDimensionRepository
import lab.dev.med.univ.feature.reporting.data.repository.DamumedNormalizedFactRepository
import lab.dev.med.univ.feature.reporting.data.repository.DamumedNormalizedSectionRepository
import lab.dev.med.univ.feature.reporting.data.repository.DamumedReportUploadRepository
import lab.dev.med.univ.feature.reporting.domain.errors.DamumedReportValidationException
import lab.dev.med.univ.feature.reporting.domain.models.DamumedLabReportKind
import lab.dev.med.univ.feature.reporting.domain.models.DamumedWorkplaceCompletedStudiesCellView
import lab.dev.med.univ.feature.reporting.domain.models.DamumedWorkplaceCompletedStudiesColumnView
import lab.dev.med.univ.feature.reporting.domain.models.DamumedWorkplaceCompletedStudiesProcessedView
import lab.dev.med.univ.feature.reporting.domain.models.DamumedWorkplaceCompletedStudiesServiceView
import lab.dev.med.univ.feature.reporting.domain.models.DamumedWorkplaceCompletedStudiesSummaryView
import lab.dev.med.univ.feature.reporting.domain.models.DamumedWorkplaceCompletedStudiesWorkplaceView
import org.springframework.stereotype.Service

interface DamumedWorkplaceCompletedStudiesProcessedViewQueryService {
    suspend fun getView(uploadId: String): DamumedWorkplaceCompletedStudiesProcessedView
}

@Service
class DamumedWorkplaceCompletedStudiesProcessedViewQueryServiceImpl(
    private val uploadRepository: DamumedReportUploadRepository,
    private val sectionRepository: DamumedNormalizedSectionRepository,
    private val factRepository: DamumedNormalizedFactRepository,
    private val factDimensionRepository: DamumedNormalizedFactDimensionRepository,
) : DamumedWorkplaceCompletedStudiesProcessedViewQueryService {
    override suspend fun getView(uploadId: String): DamumedWorkplaceCompletedStudiesProcessedView {
        val upload = uploadRepository.findById(uploadId)
            ?: throw DamumedReportValidationException("Report upload not found.")
        if (upload.reportKind != DamumedLabReportKind.WORKPLACE_COMPLETED_STUDIES) {
            throw DamumedReportValidationException("Processed view is only available for WORKPLACE_COMPLETED_STUDIES.")
        }

        val sections = sectionRepository.findAllByUploadIdOrderBySheetIdAscRowStartIndexAsc(uploadId).toList()
        val facts = factRepository.findAllByUploadIdOrderBySheetIdAscSourceRowIndexAscSourceColumnIndexAsc(uploadId).toList()
        val factEnvelopes = facts.map { fact ->
            FactEnvelope(
                fact = fact,
                dimensions = factDimensionRepository.findAllByFactIdOrderByAxisKeyAsc(fact.entityId).toList(),
            )
        }

        val orderedColumns = factEnvelopes
            .map { envelope -> envelope.toColumnDescriptor() }
            .distinctBy { it.key }
            .sortedWith(compareBy<ColumnDescriptor> { it.sourceColumnIndex }.thenBy { it.displayLabel })

        val columnsByKey = orderedColumns.associateBy { it.key }
        val sectionByWorkplace = sections.associateBy { it.anchorAxisValue?.trim().orEmpty().lowercase() }
        val workplaceGroups = factEnvelopes.groupBy { it.workplaceValue().trim().lowercase() }

        val workplaces = workplaceGroups.entries
            .sortedWith(compareBy<Map.Entry<String, List<FactEnvelope>>> { entry ->
                sectionByWorkplace[entry.key]?.rowStartIndex ?: entry.value.minOfOrNull { it.fact.sourceRowIndex } ?: Int.MAX_VALUE
            }.thenBy { it.value.firstOrNull()?.workplaceValue().orEmpty().lowercase() })
            .map { (_, workplaceFacts) ->
                buildWorkplaceView(
                    workplaceFacts = workplaceFacts,
                    orderedColumns = orderedColumns,
                    columnsByKey = columnsByKey,
                    section = sectionByWorkplace[workplaceFacts.first().workplaceValue().trim().lowercase()],
                )
            }

        val periodText = facts.firstOrNull { !it.periodText.isNullOrBlank() }?.periodText ?: upload.detectedPeriodText
        val summary = buildSummaryFromServices(
            services = workplaces.flatMap { it.services },
            orderedColumns = orderedColumns,
        )

        return DamumedWorkplaceCompletedStudiesProcessedView(
            uploadId = upload.id,
            reportKind = upload.reportKind,
            normalizationStatus = upload.normalizationStatus,
            normalizedSectionCount = upload.normalizedSectionCount,
            normalizedFactCount = upload.normalizedFactCount,
            normalizedDimensionCount = upload.normalizedDimensionCount,
            periodText = periodText,
            departmentColumns = orderedColumns.map { it.toView() },
            workplaces = workplaces,
            summary = summary,
        )
    }

    private fun buildWorkplaceView(
        workplaceFacts: List<FactEnvelope>,
        orderedColumns: List<ColumnDescriptor>,
        columnsByKey: Map<String, ColumnDescriptor>,
        section: DamumedNormalizedSectionEntity?,
    ): DamumedWorkplaceCompletedStudiesWorkplaceView {
        val services = workplaceFacts.groupBy { it.serviceValue().trim().lowercase() }
            .entries
            .sortedWith(compareBy<Map.Entry<String, List<FactEnvelope>>> { entry ->
                entry.value.minOfOrNull { it.fact.sourceRowIndex } ?: Int.MAX_VALUE
            }.thenBy { it.value.firstOrNull()?.serviceValue().orEmpty().lowercase() })
            .map { (_, serviceFacts) ->
                buildServiceView(
                    serviceFacts = serviceFacts,
                    orderedColumns = orderedColumns,
                    columnsByKey = columnsByKey,
                )
            }

        return DamumedWorkplaceCompletedStudiesWorkplaceView(
            workplace = workplaceFacts.first().workplaceValue(),
            sectionId = section?.entityId,
            rowStartIndex = section?.rowStartIndex,
            rowEndIndex = section?.rowEndIndex,
            services = services,
            summary = buildSummaryFromServices(services, orderedColumns),
        )
    }

    private fun buildServiceView(
        serviceFacts: List<FactEnvelope>,
        orderedColumns: List<ColumnDescriptor>,
        columnsByKey: Map<String, ColumnDescriptor>,
    ): DamumedWorkplaceCompletedStudiesServiceView {
        // Используем toColumnKey() чтобы ключи совпадали с column.key в departmentColumns
        val factsByColumn = serviceFacts
            .groupBy { it.toColumnKey() }
            .mapValues { (_, items) -> items.sumOf { it.fact.numericValue ?: 0.0 } }

        val cells = orderedColumns.map { column ->
            DamumedWorkplaceCompletedStudiesCellView(
                columnKey = column.key,
                metricKey = column.metricKey,
                numericValue = factsByColumn[column.key] ?: 0.0,
            )
        }

        return DamumedWorkplaceCompletedStudiesServiceView(
            service = serviceFacts.first().serviceValue(),
            sourceRowIndex = serviceFacts.minOf { it.fact.sourceRowIndex },
            cells = cells,
            completedValueTotal = cells.filter { it.metricKey == "completed_count" }.sumOf { it.numericValue },
            reportedTotalValueTotal = cells.filter { it.metricKey == "total_count" }.sumOf { it.numericValue },
        )
    }

    private fun buildSummaryFromServices(
        services: List<DamumedWorkplaceCompletedStudiesServiceView>,
        orderedColumns: List<ColumnDescriptor>,
    ): DamumedWorkplaceCompletedStudiesSummaryView {
        val cells = orderedColumns.map { column ->
            DamumedWorkplaceCompletedStudiesCellView(
                columnKey = column.key,
                metricKey = column.metricKey,
                numericValue = services.sumOf { service ->
                    service.cells.firstOrNull { it.columnKey == column.key }?.numericValue ?: 0.0
                },
            )
        }

        return DamumedWorkplaceCompletedStudiesSummaryView(
            serviceCount = services.size,
            completedValueTotal = cells.filter { it.metricKey == "completed_count" }.sumOf { it.numericValue },
            reportedTotalValueTotal = cells.filter { it.metricKey == "total_count" }.sumOf { it.numericValue },
            cells = cells,
        )
    }

    private fun FactEnvelope.toColumnDescriptor(): ColumnDescriptor {
        val departmentGroup = dimensionsByAxis["department_group"]?.rawValue
        val department = dimensionsByAxis["department"]?.rawValue
        val isTotal = dimensionsByAxis.containsKey("total") || fact.metricKey == "total_count"
        val displayLabel = when {
            isTotal && !departmentGroup.isNullOrBlank() -> "$departmentGroup / Всего"
            isTotal -> "Всего"
            !departmentGroup.isNullOrBlank() && !department.isNullOrBlank() -> "$departmentGroup / $department"
            !department.isNullOrBlank() -> department
            !departmentGroup.isNullOrBlank() -> departmentGroup
            else -> fact.metricLabel
        }
        return ColumnDescriptor(
            key = toColumnKey(),
            departmentGroup = departmentGroup,
            department = department,
            displayLabel = displayLabel,
            metricKey = fact.metricKey,
            isTotal = isTotal,
            sourceColumnIndex = fact.sourceColumnIndex,
        )
    }

    private fun FactEnvelope.toColumnKey(): String {
        // Используем departmentGroup для создания ключей в departmentColumns
        val departmentGroup = dimensionsByAxis["department_group"]?.normalizedValue.orEmpty()
        val department = dimensionsByAxis["department"]?.normalizedValue.orEmpty()
        val total = dimensionsByAxis["total"]?.normalizedValue.orEmpty()
        return listOf(fact.metricKey, departmentGroup, department, total)
            .joinToString("::")
    }

    // Ключ для объединения данных из Part1 и Part2 - игнорируем departmentGroup
    private fun FactEnvelope.toColumnKeyForMerge(): String {
        val department = dimensionsByAxis["department"]?.normalizedValue.orEmpty()
        val total = dimensionsByAxis["total"]?.normalizedValue.orEmpty()
        return listOf(fact.metricKey, department, total)
            .joinToString("::")
    }

    private fun FactEnvelope.workplaceValue(): String {
        return dimensionsByAxis["workplace"]?.rawValue ?: throw DamumedReportValidationException("Workplace axis missing in normalized fact.")
    }

    private fun FactEnvelope.serviceValue(): String {
        return dimensionsByAxis["service"]?.rawValue ?: throw DamumedReportValidationException("Service axis missing in normalized fact.")
    }

    private val FactEnvelope.dimensionsByAxis: Map<String, DamumedNormalizedFactDimensionEntity>
        get() = dimensions.associateBy { it.axisKey }

    private fun ColumnDescriptor.toView(): DamumedWorkplaceCompletedStudiesColumnView {
        return DamumedWorkplaceCompletedStudiesColumnView(
            key = key,
            departmentGroup = departmentGroup,
            department = department,
            displayLabel = displayLabel,
            metricKey = metricKey,
            isTotal = isTotal,
            sourceColumnIndex = sourceColumnIndex,
        )
    }

    private data class FactEnvelope(
        val fact: DamumedNormalizedFactEntity,
        val dimensions: List<DamumedNormalizedFactDimensionEntity>,
    )

    private data class ColumnDescriptor(
        val key: String,
        val departmentGroup: String?,
        val department: String?,
        val displayLabel: String,
        val metricKey: String,
        val isTotal: Boolean,
        val sourceColumnIndex: Int,
    )
}
