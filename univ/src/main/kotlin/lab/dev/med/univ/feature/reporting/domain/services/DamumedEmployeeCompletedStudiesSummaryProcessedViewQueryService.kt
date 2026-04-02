package lab.dev.med.univ.feature.reporting.domain.services

import kotlinx.coroutines.flow.toList
import lab.dev.med.univ.feature.reporting.data.entity.DamumedNormalizedFactDimensionEntity
import lab.dev.med.univ.feature.reporting.data.entity.DamumedNormalizedFactEntity
import lab.dev.med.univ.feature.reporting.data.repository.DamumedNormalizedFactDimensionRepository
import lab.dev.med.univ.feature.reporting.data.repository.DamumedNormalizedFactRepository
import lab.dev.med.univ.feature.reporting.data.repository.DamumedNormalizedSectionRepository
import lab.dev.med.univ.feature.reporting.data.repository.DamumedReportUploadRepository
import lab.dev.med.univ.feature.reporting.domain.errors.DamumedReportValidationException
import lab.dev.med.univ.feature.reporting.domain.models.DamumedEmployeeCompletedStudiesBucketView
import lab.dev.med.univ.feature.reporting.domain.models.DamumedEmployeeCompletedStudiesCellView
import lab.dev.med.univ.feature.reporting.domain.models.DamumedEmployeeCompletedStudiesEmployeeView
import lab.dev.med.univ.feature.reporting.domain.models.DamumedEmployeeCompletedStudiesSummaryProcessedView
import lab.dev.med.univ.feature.reporting.domain.models.DamumedEmployeeCompletedStudiesSummaryTotalsView
import lab.dev.med.univ.feature.reporting.domain.models.DamumedLabReportKind
import org.springframework.stereotype.Service

interface DamumedEmployeeCompletedStudiesSummaryProcessedViewQueryService {
    suspend fun getView(uploadId: String): DamumedEmployeeCompletedStudiesSummaryProcessedView
}

@Service
class DamumedEmployeeCompletedStudiesSummaryProcessedViewQueryServiceImpl(
    private val uploadRepository: DamumedReportUploadRepository,
    private val sectionRepository: DamumedNormalizedSectionRepository,
    private val factRepository: DamumedNormalizedFactRepository,
    private val factDimensionRepository: DamumedNormalizedFactDimensionRepository,
) : DamumedEmployeeCompletedStudiesSummaryProcessedViewQueryService {
    override suspend fun getView(uploadId: String): DamumedEmployeeCompletedStudiesSummaryProcessedView {
        val upload = uploadRepository.findById(uploadId)
            ?: throw DamumedReportValidationException("Report upload not found.")
        if (upload.reportKind != DamumedLabReportKind.EMPLOYEE_COMPLETED_STUDIES_SUMMARY) {
            throw DamumedReportValidationException("Processed view is only available for EMPLOYEE_COMPLETED_STUDIES_SUMMARY.")
        }

        val sections = sectionRepository.findAllByUploadIdOrderBySheetIdAscRowStartIndexAsc(uploadId).toList()
        val facts = factRepository.findAllByUploadIdOrderBySheetIdAscSourceRowIndexAscSourceColumnIndexAsc(uploadId).toList()
        val factEnvelopes = facts.map { fact ->
            FactEnvelope(
                fact = fact,
                dimensions = factDimensionRepository.findAllByFactIdOrderByAxisKeyAsc(fact.entityId).toList(),
            )
        }

        val bucketColumns = factEnvelopes
            .map { it.toBucketDescriptor() }
            .distinctBy { it.key }
            .sortedWith(compareBy<BucketDescriptor> { it.sourceColumnIndex }.thenBy { it.metricLabel.lowercase() })

        val sectionsByEmployee = sections.associateBy { it.anchorAxisValue?.trim().orEmpty().lowercase() }
        val employees = factEnvelopes.groupBy { it.employeeValue().trim().lowercase() }
            .entries
            .sortedWith(compareBy<Map.Entry<String, List<FactEnvelope>>> { entry ->
                entry.value.minOfOrNull { it.fact.sourceRowIndex } ?: Int.MAX_VALUE
            }.thenBy { it.value.firstOrNull()?.employeeValue().orEmpty().lowercase() })
            .map { (_, employeeFacts) ->
                val employee = employeeFacts.first().employeeValue()
                val valuesByBucket = employeeFacts
                    .groupBy { it.toBucketKey() }
                    .mapValues { (_, items) -> items.sumOf { item -> item.fact.numericValue ?: 0.0 } }
                val cells = bucketColumns.map { bucket ->
                    DamumedEmployeeCompletedStudiesCellView(
                        bucketKey = bucket.key,
                        metricKey = bucket.metricKey,
                        numericValue = valuesByBucket[bucket.key] ?: 0.0,
                    )
                }
                DamumedEmployeeCompletedStudiesEmployeeView(
                    employee = employee,
                    sectionId = sectionsByEmployee[employee.trim().lowercase()]?.entityId,
                    sourceRowIndex = employeeFacts.minOf { it.fact.sourceRowIndex },
                    cells = cells,
                    completedServiceCountTotal = cells.filter { it.metricKey == "completed_service_count" }.sumOf { it.numericValue },
                    completedPatientCountTotal = cells.filter { it.metricKey == "completed_patient_count" }.sumOf { it.numericValue },
                )
            }

        val summaryCells = bucketColumns.map { bucket ->
            DamumedEmployeeCompletedStudiesCellView(
                bucketKey = bucket.key,
                metricKey = bucket.metricKey,
                numericValue = employees.sumOf { employee -> employee.cells.firstOrNull { it.bucketKey == bucket.key }?.numericValue ?: 0.0 },
            )
        }

        return DamumedEmployeeCompletedStudiesSummaryProcessedView(
            uploadId = upload.id,
            reportKind = upload.reportKind,
            normalizationStatus = upload.normalizationStatus,
            normalizedSectionCount = upload.normalizedSectionCount,
            normalizedFactCount = upload.normalizedFactCount,
            normalizedDimensionCount = upload.normalizedDimensionCount,
            periodText = facts.firstOrNull { !it.periodText.isNullOrBlank() }?.periodText ?: upload.detectedPeriodText,
            bucketColumns = bucketColumns.map {
                DamumedEmployeeCompletedStudiesBucketView(
                    key = it.key,
                    label = it.label,
                    metricKey = it.metricKey,
                    metricLabel = it.metricLabel,
                    sourceColumnIndex = it.sourceColumnIndex,
                    isTotal = it.isTotal,
                )
            },
            employees = employees,
            summary = DamumedEmployeeCompletedStudiesSummaryTotalsView(
                employeeCount = employees.size,
                completedServiceCountTotal = summaryCells.filter { it.metricKey == "completed_service_count" }.sumOf { it.numericValue },
                completedPatientCountTotal = summaryCells.filter { it.metricKey == "completed_patient_count" }.sumOf { it.numericValue },
                cells = summaryCells,
            ),
        )
    }

    private fun FactEnvelope.toBucketDescriptor(): BucketDescriptor {
        val bucketLabel = dimensionsByAxis["period_bucket"]?.rawValue
            ?: throw DamumedReportValidationException("Period bucket axis missing in normalized fact.")
        return BucketDescriptor(
            key = toBucketKey(),
            label = bucketLabel,
            metricKey = fact.metricKey,
            metricLabel = fact.metricLabel,
            sourceColumnIndex = fact.sourceColumnIndex,
            isTotal = bucketLabel.equals("Итого", ignoreCase = true) || bucketLabel.contains("всего", ignoreCase = true),
        )
    }

    private fun FactEnvelope.toBucketKey(): String {
        val bucket = dimensionsByAxis["period_bucket"]?.normalizedValue
            ?: throw DamumedReportValidationException("Period bucket axis missing in normalized fact.")
        return "${fact.metricKey}::$bucket"
    }

    private fun FactEnvelope.employeeValue(): String {
        return dimensionsByAxis["employee"]?.rawValue
            ?: throw DamumedReportValidationException("Employee axis missing in normalized fact.")
    }

    private val FactEnvelope.dimensionsByAxis: Map<String, DamumedNormalizedFactDimensionEntity>
        get() = dimensions.associateBy { it.axisKey }

    private data class FactEnvelope(
        val fact: DamumedNormalizedFactEntity,
        val dimensions: List<DamumedNormalizedFactDimensionEntity>,
    )

    private data class BucketDescriptor(
        val key: String,
        val label: String,
        val metricKey: String,
        val metricLabel: String,
        val sourceColumnIndex: Int,
        val isTotal: Boolean,
    )
}
