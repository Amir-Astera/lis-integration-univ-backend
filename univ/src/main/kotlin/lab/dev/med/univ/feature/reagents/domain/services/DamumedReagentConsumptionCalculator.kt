package lab.dev.med.univ.feature.reagents.domain.services

import com.fasterxml.jackson.databind.ObjectMapper
import kotlinx.coroutines.flow.toList
import lab.dev.med.univ.feature.reporting.data.entity.toModel
import lab.dev.med.univ.feature.reporting.data.repository.DamumedNormalizedFactRepository
import lab.dev.med.univ.feature.reporting.data.repository.DamumedReportUploadRepository
import lab.dev.med.univ.feature.reporting.domain.models.DamumedLabReportKind
import lab.dev.med.univ.feature.reagents.data.entity.DamumedReportReagentConsumptionEntity
import lab.dev.med.univ.feature.reagents.data.entity.normalizeServiceName
import lab.dev.med.univ.feature.reagents.data.entity.toEntity
import lab.dev.med.univ.feature.reagents.data.entity.toModel
import lab.dev.med.univ.feature.reagents.data.repository.DamumedReportReagentConsumptionRepository
import lab.dev.med.univ.feature.reagents.data.repository.ReagentInventoryRepository
import lab.dev.med.univ.feature.reagents.data.repository.ServiceReagentConsumptionNormRepository
import lab.dev.med.univ.feature.reagents.domain.errors.ReagentModuleValidationException
import lab.dev.med.univ.feature.reagents.domain.models.CalculateDamumedConsumptionRequest
import lab.dev.med.univ.feature.reagents.domain.models.CategorySummary
import lab.dev.med.univ.feature.reagents.domain.models.ConsumptionEntry
import lab.dev.med.univ.feature.reagents.domain.models.DamumedConsumptionCalculationResult
import lab.dev.med.univ.feature.reagents.domain.models.DamumedReportReagentConsumption
import lab.dev.med.univ.feature.reagents.domain.models.DetectionConfidence
import lab.dev.med.univ.feature.reagents.domain.models.ReagentSummary
import lab.dev.med.univ.feature.reagents.domain.models.ReagentUnitType
import lab.dev.med.univ.feature.reporting.data.entity.DamumedNormalizedFactEntity
import org.springframework.stereotype.Service
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.UUID

/**
 * Service for calculating reagent consumption from Damumed reports.
 * Links normalized report facts to reagent norms and computes total consumption.
 */
interface DamumedReagentConsumptionCalculator {
    /**
     * Calculate reagent consumption for a Damumed report upload.
     */
    suspend fun calculate(request: CalculateDamumedConsumptionRequest): DamumedConsumptionCalculationResult

    /**
     * Get calculated consumption entries for a specific upload.
     */
    suspend fun getCalculatedConsumption(uploadId: String): List<DamumedReportReagentConsumption>

    /**
     * Recalculate and overwrite existing consumption data for an upload.
     */
    suspend fun recalculate(uploadId: String): DamumedConsumptionCalculationResult
}

@Service
internal class DamumedReagentConsumptionCalculatorImpl(
    private val objectMapper: ObjectMapper,
    private val uploadRepository: DamumedReportUploadRepository,
    private val factRepository: DamumedNormalizedFactRepository,
    private val normRepository: ServiceReagentConsumptionNormRepository,
    private val inventoryRepository: ReagentInventoryRepository,
    private val mappingQueryService: ServiceToAnalyzerMappingQueryService,
    private val consumptionRepository: DamumedReportReagentConsumptionRepository,
) : DamumedReagentConsumptionCalculator {

    override suspend fun calculate(request: CalculateDamumedConsumptionRequest): DamumedConsumptionCalculationResult {
        val upload = uploadRepository.findById(request.uploadId)?.toModel()
            ?: throw ReagentModuleValidationException("Report upload '${request.uploadId}' not found")

        // Only process reports that contain service/completed count data
        if (!isSupportedReportKind(upload.reportKind)) {
            throw ReagentModuleValidationException(
                "Report kind '${upload.reportKind}' is not supported for reagent consumption calculation. " +
                    "Supported kinds: WORKPLACE_COMPLETED_STUDIES, COMPLETED_LAB_STUDIES_JOURNAL, EMPLOYEE_COMPLETED_STUDIES_SUMMARY"
            )
        }

        // Clear existing calculations for this upload
        consumptionRepository.deleteAllByUploadId(request.uploadId)

        // Get all facts for this upload that represent completed services
        val facts = factRepository.findAllByUploadIdOrderBySheetIdAscSourceRowIndexAscSourceColumnIndexAsc(request.uploadId)
            .toList()
            .filter { isServiceFact(it) }

        val results = mutableListOf<DamumedReportReagentConsumption>()
        val unmappedServices = mutableSetOf<String>()
        var totalEntries = 0
        var totalCost = BigDecimal.ZERO

        for (fact in facts) {
            val serviceName = extractServiceName(fact) ?: continue
            val completedCount = extractCompletedCount(fact) ?: 1
            val category = extractCategory(fact, serviceName)

            // Apply category filter if specified
            if (request.serviceCategoryFilter != null && category !in request.serviceCategoryFilter) {
                continue
            }

            // Get norms for this service
            val norms = getNormsForService(serviceName, category)

            if (norms.isEmpty()) {
                unmappedServices.add(serviceName)
                continue
            }

            // Determine analyzer (from override, mapping, or norm)
            val analyzerId = request.overrideAnalyzerMappings?.get(serviceName)
                ?: mappingQueryService.findMatchingAnalyzer(serviceName, category)?.analyzerId
                ?: norms.firstOrNull()?.analyzerId

            val detectionConfidence = when {
                request.overrideAnalyzerMappings?.containsKey(serviceName) == true -> DetectionConfidence.MANUAL
                mappingQueryService.findMatchingAnalyzer(serviceName, category) != null -> DetectionConfidence.HIGH
                norms.firstOrNull()?.analyzerId != null -> DetectionConfidence.MEDIUM
                else -> DetectionConfidence.LOW
            }

            // Calculate consumption for each norm
            val consumptionEntries = norms.map { norm ->
                val totalQuantity = norm.calculateTotalQuantity(completedCount)
                val unitCost = getUnitCost(norm.reagentName, norm.unitType)
                val totalEntryCost = unitCost?.multiply(totalQuantity)

                ConsumptionEntry(
                    reagentName = norm.reagentName,
                    quantity = totalQuantity,
                    unitType = norm.unitType,
                    unitCostTenge = unitCost,
                    totalCostTenge = totalEntryCost,
                    sourceNormId = norm.id,
                )
            }

            val serviceTotalCost = consumptionEntries.sumOf { it.totalCostTenge ?: BigDecimal.ZERO }
            totalCost = totalCost.add(serviceTotalCost)
            totalEntries += consumptionEntries.size

            val consumption = DamumedReportReagentConsumption(
                id = UUID.randomUUID().toString(),
                uploadId = request.uploadId,
                factId = fact.entityId,
                serviceName = serviceName,
                serviceCategory = category,
                completedCount = completedCount,
                consumptionEntries = consumptionEntries,
                totalEstimatedCostTenge = serviceTotalCost,
                detectedAnalyzerId = analyzerId,
                detectionConfidence = detectionConfidence,
                calculatedAt = LocalDateTime.now(),
                calculatedBy = "system",
            )

            results.add(consumption)
            consumptionRepository.save(consumption.toEntity())
        }

        return DamumedConsumptionCalculationResult(
            uploadId = request.uploadId,
            totalServicesProcessed = results.size,
            totalConsumptionEntries = totalEntries,
            totalEstimatedCostTenge = totalCost,
            byCategory = buildCategorySummary(results),
            unmappedServices = unmappedServices.toList(),
        )
    }

    override suspend fun getCalculatedConsumption(uploadId: String): List<DamumedReportReagentConsumption> {
        return consumptionRepository.findAllByUploadIdOrderByCalculatedAtDesc(uploadId)
            .toList()
            .map { it.toModel() }
    }

    override suspend fun recalculate(uploadId: String): DamumedConsumptionCalculationResult {
        // Delete existing
        consumptionRepository.deleteAllByUploadId(uploadId)

        // Calculate fresh
        return calculate(CalculateDamumedConsumptionRequest(uploadId))
    }

    // =============================================================================
    // Helper methods
    // =============================================================================

    private fun isSupportedReportKind(kind: DamumedLabReportKind): Boolean {
        return kind in setOf(
            DamumedLabReportKind.WORKPLACE_COMPLETED_STUDIES,
            DamumedLabReportKind.COMPLETED_LAB_STUDIES_JOURNAL,
            DamumedLabReportKind.EMPLOYEE_COMPLETED_STUDIES_SUMMARY,
        )
    }

    private fun isServiceFact(fact: DamumedNormalizedFactEntity): Boolean {
        // Check if this fact represents a service metric
        return fact.metricKey in setOf("completed_count", "total_count") &&
            fact.numericValue != null &&
            fact.numericValue > 0
    }

    private fun extractServiceName(fact: DamumedNormalizedFactEntity): String? {
        // Try to get service name from valueText or dimension references
        return fact.valueText?.takeIf { it.isNotBlank() }
            ?: fact.metricLabel?.takeIf { it.isNotBlank() }
    }

    private fun extractCompletedCount(fact: DamumedNormalizedFactEntity): Int? {
        return fact.numericValue?.toInt()?.takeIf { it > 0 }
    }

    private fun extractCategory(
        fact: DamumedNormalizedFactEntity,
        serviceName: String,
    ): String? {
        // Try to extract from fact context or detect from service name
        return detectServiceCategory(serviceName)
    }

    private suspend fun getNormsForService(serviceName: String, category: String?): List<lab.dev.med.univ.feature.reagents.domain.models.ServiceReagentConsumptionNorm> {
        val normalizedName = normalizeServiceName(serviceName)

        // First try exact normalized match
        val exactMatches = normRepository
            .findAllByServiceNameNormalizedContainingIgnoreCaseAndIsActiveTrue(normalizedName)
            .toList()
            .map { it.toModel() }
            .filter { it.serviceNameNormalized == normalizedName }

        if (exactMatches.isNotEmpty()) {
            return exactMatches
        }

        // Fall back to partial matches
        return normRepository
            .findAllByServiceNameNormalizedContainingIgnoreCaseAndIsActiveTrue(normalizedName)
            .toList()
            .map { it.toModel() }
    }

    private suspend fun getUnitCost(reagentName: String, unitType: ReagentUnitType): BigDecimal? {
        // Get latest inventory price for this reagent
        val inventory = inventoryRepository.findAllByOrderByReceivedAtDescCreatedAtDesc()
            .toList()
            .filter { it.reagentName.equals(reagentName, ignoreCase = true) && it.unitType == unitType }
            .maxByOrNull { it.receivedAt }

        return inventory?.unitPriceTenge?.let { BigDecimal(it) }
    }

    private fun buildCategorySummary(
        results: List<DamumedReportReagentConsumption>,
    ): Map<String, CategorySummary> {
        return results
            .groupBy { it.serviceCategory ?: "UNKNOWN" }
            .mapValues { (_, consumptions) ->
                val totalCost = consumptions.sumOf { it.totalEstimatedCostTenge }

                // Aggregate reagent totals within category
                val reagentTotals = consumptions
                    .flatMap { it.consumptionEntries }
                    .groupBy { it.reagentName }
                    .map { (name, entries) ->
                        ReagentSummary(
                            reagentName = name,
                            totalQuantity = entries.sumOf { it.quantity },
                            unitType = entries.first().unitType,
                            totalCostTenge = entries.sumOf { it.totalCostTenge ?: BigDecimal.ZERO },
                        )
                    }
                    .sortedByDescending { it.totalCostTenge ?: BigDecimal.ZERO }
                    .take(5) // Top 5 by cost

                CategorySummary(
                    serviceCount = consumptions.size,
                    totalCostTenge = totalCost,
                    topReagents = reagentTotals,
                )
            }
    }

    private fun detectServiceCategory(serviceName: String): String? {
        val normalized = serviceName.lowercase()
        return when {
            normalized.contains("гематолог") || normalized.contains("крови") ||
                normalized.contains("вск") || normalized.contains("рвс") ||
                normalized.contains("лейкоцит") || normalized.contains("тромбоцит") ||
                normalized.contains("гемоглобин") || normalized.contains("гематокрит") -> "Гематология"

            normalized.contains("биохим") || normalized.contains("химия") ||
                normalized.contains("глюкоз") || normalized.contains("холестерин") ||
                normalized.contains("белок") || normalized.contains("креатинин") ||
                normalized.contains("мочевин") || normalized.contains("билирубин") ||
                normalized.contains("алт") || normalized.contains("аст") ||
                normalized.contains("амилаза") || normalized.contains("липаза") -> "Биохимия"

            normalized.contains("коагул") || normalized.contains("протромбин") ||
                normalized.contains("аптв") || normalized.contains("фибриноген") ||
                normalized.contains("д-димер") || normalized.contains("мно") ||
                normalized.contains("тромбин") -> "Коагулология"

            normalized.contains("иммун") || normalized.contains("гормон") ||
                normalized.contains("антител") || normalized.contains("вич") ||
                normalized.contains("гепатит") || normalized.contains("сифилис") ||
                normalized.contains("воз") || normalized.contains("covid") ||
                normalized.contains("ige") || normalized.contains("iga") -> "Иммунология"

            normalized.contains("микробиол") || normalized.contains("посев") ||
                normalized.contains("бакпосев") || normalized.contains("чувствительность") ||
                normalized.contains("асп") || normalized.contains("гриб") ||
                normalized.contains("бактери") -> "Микробиология"

            normalized.contains("молекуляр") || normalized.contains("пцр") ||
                normalized.contains("генет") || normalized.contains("днк") ||
                normalized.contains("рнк") -> "Молекулярная_биология"

            normalized.contains("общеклиническ") || normalized.contains("загальноклінічн") ||
                normalized.contains("общий анализ мочи") || normalized.contains("кал на яйца") ||
                normalized.contains("соскоб") -> "Общеклинические"

            else -> "Другое"
        }
    }
}
