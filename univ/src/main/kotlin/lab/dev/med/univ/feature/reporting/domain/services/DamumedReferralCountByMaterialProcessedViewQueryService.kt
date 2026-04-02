package lab.dev.med.univ.feature.reporting.domain.services

import kotlinx.coroutines.flow.toList
import lab.dev.med.univ.feature.reporting.data.entity.DamumedNormalizedFactDimensionEntity
import lab.dev.med.univ.feature.reporting.data.entity.DamumedNormalizedFactEntity
import lab.dev.med.univ.feature.reporting.data.repository.DamumedNormalizedFactDimensionRepository
import lab.dev.med.univ.feature.reporting.data.repository.DamumedNormalizedFactRepository
import lab.dev.med.univ.feature.reporting.data.repository.DamumedNormalizedSectionRepository
import lab.dev.med.univ.feature.reporting.data.repository.DamumedReportUploadRepository
import lab.dev.med.univ.feature.reporting.domain.errors.DamumedReportValidationException
import lab.dev.med.univ.feature.reporting.domain.models.DamumedLabReportKind
import lab.dev.med.univ.feature.reporting.domain.models.DamumedReferralCountByMaterialBucketView
import lab.dev.med.univ.feature.reporting.domain.models.DamumedReferralCountByMaterialCellView
import lab.dev.med.univ.feature.reporting.domain.models.DamumedReferralCountByMaterialProcessedView
import lab.dev.med.univ.feature.reporting.domain.models.DamumedReferralCountByMaterialRowView
import lab.dev.med.univ.feature.reporting.domain.models.DamumedReferralCountByMaterialSummaryView
import org.springframework.stereotype.Service

interface DamumedReferralCountByMaterialProcessedViewQueryService {
    suspend fun getView(uploadId: String): DamumedReferralCountByMaterialProcessedView
}

@Service
class DamumedReferralCountByMaterialProcessedViewQueryServiceImpl(
    private val uploadRepository: DamumedReportUploadRepository,
    private val sectionRepository: DamumedNormalizedSectionRepository,
    private val factRepository: DamumedNormalizedFactRepository,
    private val factDimensionRepository: DamumedNormalizedFactDimensionRepository,
) : DamumedReferralCountByMaterialProcessedViewQueryService {
    override suspend fun getView(uploadId: String): DamumedReferralCountByMaterialProcessedView {
        val upload = uploadRepository.findById(uploadId)
            ?: throw DamumedReportValidationException("Report upload not found.")
        if (upload.reportKind != DamumedLabReportKind.REFERRAL_COUNT_BY_MATERIAL) {
            throw DamumedReportValidationException("Processed view is only available for REFERRAL_COUNT_BY_MATERIAL.")
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
            .sortedWith(compareBy<BucketDescriptor> { it.sourceColumnIndex }.thenBy { it.label.lowercase() })

        val sectionsByMaterial = sections.associateBy { it.anchorAxisValue?.trim().orEmpty().lowercase() }
        val rows = factEnvelopes.groupBy { it.materialValue().trim().lowercase() }
            .entries
            .sortedWith(compareBy<Map.Entry<String, List<FactEnvelope>>> { entry ->
                entry.value.minOfOrNull { it.fact.sourceRowIndex } ?: Int.MAX_VALUE
            }.thenBy { it.value.firstOrNull()?.materialValue().orEmpty().lowercase() })
            .map { (_, materialFacts) ->
                val valuesByBucket = materialFacts.associate { envelope -> envelope.toBucketKey() to (envelope.fact.numericValue ?: 0.0) }
                val material = materialFacts.first().materialValue()
                val cells = bucketColumns.map { bucket ->
                    DamumedReferralCountByMaterialCellView(
                        bucketKey = bucket.key,
                        label = bucket.label,
                        numericValue = valuesByBucket[bucket.key] ?: 0.0,
                    )
                }
                DamumedReferralCountByMaterialRowView(
                    material = material,
                    sectionId = sectionsByMaterial[material.trim().lowercase()]?.entityId,
                    sourceRowIndex = materialFacts.minOf { it.fact.sourceRowIndex },
                    cells = cells,
                    rowTotal = cells.firstOrNull { it.label.equals("Итого", ignoreCase = true) }?.numericValue
                        ?: cells.sumOf { it.numericValue },
                )
            }

        val summaryCells = bucketColumns.map { bucket ->
            DamumedReferralCountByMaterialCellView(
                bucketKey = bucket.key,
                label = bucket.label,
                numericValue = rows.sumOf { row -> row.cells.firstOrNull { it.bucketKey == bucket.key }?.numericValue ?: 0.0 },
            )
        }

        return DamumedReferralCountByMaterialProcessedView(
            uploadId = upload.id,
            reportKind = upload.reportKind,
            normalizationStatus = upload.normalizationStatus,
            normalizedSectionCount = upload.normalizedSectionCount,
            normalizedFactCount = upload.normalizedFactCount,
            normalizedDimensionCount = upload.normalizedDimensionCount,
            periodText = upload.detectedPeriodText,
            bucketColumns = bucketColumns.map { bucket ->
                DamumedReferralCountByMaterialBucketView(
                    key = bucket.key,
                    label = bucket.label,
                    sourceColumnIndex = bucket.sourceColumnIndex,
                    isTotal = bucket.isTotal,
                )
            },
            materials = rows,
            summary = DamumedReferralCountByMaterialSummaryView(
                materialCount = rows.size,
                cells = summaryCells,
                grandTotal = summaryCells.firstOrNull { it.label.equals("Итого", ignoreCase = true) }?.numericValue
                    ?: summaryCells.sumOf { it.numericValue },
            ),
        )
    }

    private fun FactEnvelope.toBucketDescriptor(): BucketDescriptor {
        val bucket = dimensionsByAxis["period_bucket"]?.rawValue
            ?: throw DamumedReportValidationException("Period bucket axis missing in normalized fact.")
        return BucketDescriptor(
            key = toBucketKey(),
            label = bucket,
            sourceColumnIndex = fact.sourceColumnIndex,
            isTotal = bucket.equals("Итого", ignoreCase = true),
        )
    }

    private fun FactEnvelope.toBucketKey(): String {
        return dimensionsByAxis["period_bucket"]?.normalizedValue
            ?: throw DamumedReportValidationException("Period bucket axis missing in normalized fact.")
    }

    private fun FactEnvelope.materialValue(): String {
        return dimensionsByAxis["material"]?.rawValue
            ?: throw DamumedReportValidationException("Material axis missing in normalized fact.")
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
        val sourceColumnIndex: Int,
        val isTotal: Boolean,
    )
}
