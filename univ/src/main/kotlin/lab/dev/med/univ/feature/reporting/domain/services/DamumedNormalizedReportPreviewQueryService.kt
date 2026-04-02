package lab.dev.med.univ.feature.reporting.domain.services

import kotlinx.coroutines.flow.toList
import lab.dev.med.univ.feature.reporting.data.repository.DamumedNormalizedDimensionRepository
import lab.dev.med.univ.feature.reporting.data.repository.DamumedNormalizedFactDimensionRepository
import lab.dev.med.univ.feature.reporting.data.repository.DamumedNormalizedFactRepository
import lab.dev.med.univ.feature.reporting.data.repository.DamumedNormalizedSectionRepository
import lab.dev.med.univ.feature.reporting.data.repository.DamumedReportUploadRepository
import lab.dev.med.univ.feature.reporting.domain.errors.DamumedReportValidationException
import lab.dev.med.univ.feature.reporting.domain.models.DamumedNormalizedDimensionPreview
import lab.dev.med.univ.feature.reporting.domain.models.DamumedNormalizedFactDimensionPreview
import lab.dev.med.univ.feature.reporting.domain.models.DamumedNormalizedFactPreview
import lab.dev.med.univ.feature.reporting.domain.models.DamumedNormalizedReportPreview
import lab.dev.med.univ.feature.reporting.domain.models.DamumedNormalizedSectionPreview
import org.springframework.stereotype.Service

interface DamumedNormalizedReportPreviewQueryService {
    suspend fun getPreview(uploadId: String, maxFacts: Int = 250): DamumedNormalizedReportPreview
}

@Service
class DamumedNormalizedReportPreviewQueryServiceImpl(
    private val uploadRepository: DamumedReportUploadRepository,
    private val sectionRepository: DamumedNormalizedSectionRepository,
    private val dimensionRepository: DamumedNormalizedDimensionRepository,
    private val factRepository: DamumedNormalizedFactRepository,
    private val factDimensionRepository: DamumedNormalizedFactDimensionRepository,
) : DamumedNormalizedReportPreviewQueryService {
    override suspend fun getPreview(uploadId: String, maxFacts: Int): DamumedNormalizedReportPreview {
        val upload = uploadRepository.findById(uploadId)
            ?: throw DamumedReportValidationException("Report upload not found.")
        val sections = sectionRepository.findAllByUploadIdOrderBySheetIdAscRowStartIndexAsc(uploadId).toList()
        val dimensions = dimensionRepository.findAllByUploadIdOrderByAxisKeyAscDisplayValueAsc(uploadId).toList()
        val facts = factRepository.findAllByUploadIdOrderBySheetIdAscSourceRowIndexAscSourceColumnIndexAsc(uploadId)
            .toList()
            .take(maxFacts.coerceAtLeast(1))

        return DamumedNormalizedReportPreview(
            uploadId = upload.id,
            reportKind = upload.reportKind,
            normalizationStatus = upload.normalizationStatus,
            normalizedSectionCount = upload.normalizedSectionCount,
            normalizedFactCount = upload.normalizedFactCount,
            normalizedDimensionCount = upload.normalizedDimensionCount,
            sections = sections.map { section ->
                DamumedNormalizedSectionPreview(
                    id = section.entityId,
                    sheetId = section.sheetId,
                    sectionKey = section.sectionKey,
                    sectionName = section.sectionName,
                    semanticRole = section.semanticRole,
                    anchorAxisKey = section.anchorAxisKey,
                    anchorAxisValue = section.anchorAxisValue,
                    rowStartIndex = section.rowStartIndex,
                    rowEndIndex = section.rowEndIndex,
                )
            },
            dimensions = dimensions.map { dimension ->
                DamumedNormalizedDimensionPreview(
                    id = dimension.entityId,
                    axisKey = dimension.axisKey,
                    axisType = dimension.axisType,
                    rawValue = dimension.rawValue,
                    normalizedValue = dimension.normalizedValue,
                    displayValue = dimension.displayValue,
                )
            },
            facts = facts.map { fact ->
                val factDimensions = factDimensionRepository.findAllByFactIdOrderByAxisKeyAsc(fact.entityId)
                    .toList()
                DamumedNormalizedFactPreview(
                    id = fact.entityId,
                    sheetId = fact.sheetId,
                    sectionId = fact.sectionId,
                    metricKey = fact.metricKey,
                    metricLabel = fact.metricLabel,
                    numericValue = fact.numericValue,
                    valueText = fact.valueText,
                    formulaText = fact.formulaText,
                    periodText = fact.periodText,
                    sourceRowIndex = fact.sourceRowIndex,
                    sourceColumnIndex = fact.sourceColumnIndex,
                    dimensions = factDimensions.map { dimension ->
                        DamumedNormalizedFactDimensionPreview(
                            axisKey = dimension.axisKey,
                            dimensionId = dimension.dimensionId,
                            rawValue = dimension.rawValue,
                            normalizedValue = dimension.normalizedValue,
                            sourceScope = dimension.sourceScope,
                        )
                    },
                )
            },
        )
    }
}
