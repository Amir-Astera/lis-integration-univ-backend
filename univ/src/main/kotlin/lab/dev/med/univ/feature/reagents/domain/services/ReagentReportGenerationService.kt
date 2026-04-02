package lab.dev.med.univ.feature.reagents.domain.services

import com.fasterxml.jackson.databind.ObjectMapper
import kotlinx.coroutines.flow.toList
import lab.dev.med.univ.feature.reagents.data.entity.toEntity
import lab.dev.med.univ.feature.reagents.data.entity.toModel
import lab.dev.med.univ.feature.reagents.data.repository.AnalyzerReagentRateRepository
import lab.dev.med.univ.feature.reagents.data.repository.AnalyzerRepository
import lab.dev.med.univ.feature.reagents.data.repository.ParsedAnalyzerSampleRepository
import lab.dev.med.univ.feature.reagents.data.repository.ReagentInventoryRepository
import lab.dev.med.univ.feature.reagents.data.repository.ReagentConsumptionReportRepository
import lab.dev.med.univ.feature.reagents.domain.errors.AnalyzerNotFoundException
import lab.dev.med.univ.feature.reagents.domain.errors.ReagentModuleValidationException
import lab.dev.med.univ.feature.reagents.domain.models.AnalyzerReagentRate
import lab.dev.med.univ.feature.reagents.domain.models.ParsedAnalyzerSample
import lab.dev.med.univ.feature.reagents.domain.models.ReagentInventory
import lab.dev.med.univ.feature.reagents.domain.models.ReagentConsumptionReport
import lab.dev.med.univ.feature.reagents.domain.models.ReagentInventoryStatus
import lab.dev.med.univ.feature.reagents.domain.models.ReagentOperationType
import lab.dev.med.univ.feature.reagents.domain.models.ReagentUnitType
import lab.dev.med.univ.feature.reagents.domain.models.SampleClassification
import org.springframework.stereotype.Service
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.UUID

interface GenerateReagentConsumptionReportService {
    suspend fun generate(
        analyzerId: String,
        periodStart: LocalDate,
        periodEnd: LocalDate,
        generatedBy: String? = null,
    ): ReagentConsumptionReport
}

@Service
internal class GenerateReagentConsumptionReportServiceImpl(
    private val objectMapper: ObjectMapper,
    private val analyzerRepository: AnalyzerRepository,
    private val analyzerReagentRateRepository: AnalyzerReagentRateRepository,
    private val parsedAnalyzerSampleRepository: ParsedAnalyzerSampleRepository,
    private val reagentInventoryRepository: ReagentInventoryRepository,
    private val reagentConsumptionReportRepository: ReagentConsumptionReportRepository,
) : GenerateReagentConsumptionReportService {

    override suspend fun generate(
        analyzerId: String,
        periodStart: LocalDate,
        periodEnd: LocalDate,
        generatedBy: String?,
    ): ReagentConsumptionReport {
        if (periodEnd.isBefore(periodStart)) {
            throw ReagentModuleValidationException("Report periodEnd cannot be earlier than periodStart.")
        }
        if (analyzerRepository.findById(analyzerId) == null) {
            throw AnalyzerNotFoundException(analyzerId)
        }

        val periodStartDateTime = periodStart.atStartOfDay()
        val periodEndExclusive = periodEnd.plusDays(1).atStartOfDay()

        val rates = analyzerReagentRateRepository.findAllByAnalyzerIdOrderByReagentNameAscOperationTypeAsc(analyzerId)
            .toList()
            .map { it.toModel() }

        val inventoryItems = reagentInventoryRepository.findAllByOrderByReceivedAtDescCreatedAtDesc()
            .toList()
            .map { it.toModel() }
            .filter { it.analyzerId == null || it.analyzerId == analyzerId }

        val samples = parsedAnalyzerSampleRepository.findAllByAnalyzerIdOrderBySampleTimestampAsc(analyzerId)
            .toList()
            .map { it.toModel() }
            .filter { !it.sampleTimestamp.isBefore(periodStartDateTime) && it.sampleTimestamp.isBefore(periodEndExclusive) }

        val legitimateSamples = samples.filter { it.classification == SampleClassification.LEGITIMATE }
        val suspiciousSamples = samples.filter { it.classification == SampleClassification.SUSPICIOUS }
        val rerunSamples = samples.filter { it.classification == SampleClassification.PROBABLE_RERUN }
        val washSamples = samples.filter { it.classification == SampleClassification.WASH_TEST }
        val serviceSamples = samples.filter {
            it.classification == SampleClassification.WASH_TEST ||
                it.classification == SampleClassification.QC ||
                it.classification == SampleClassification.CALIBRATION ||
                it.classification == SampleClassification.ERROR
        }
        val unauthorizedSamples = suspiciousSamples + rerunSamples

        val priceMap = buildPriceMap(inventoryItems)
        val legitimateConsumption = buildConsumptionRows(legitimateSamples, rates, priceMap, "LEGITIMATE")
        val unauthorizedConsumption = buildConsumptionRows(unauthorizedSamples, rates, priceMap, "UNAUTHORIZED")
        val serviceConsumption = buildConsumptionRows(serviceSamples, rates, priceMap, "SERVICE")
        val serviceOperations = buildServiceOperationRows(serviceSamples)
        val inventoryReceived = buildInventorySnapshotRows(
            inventoryItems.filter { !it.receivedAt.isBefore(periodStart) && !it.receivedAt.isAfter(periodEnd) },
            "RECEIVED",
        )
        val inventoryEndActual = buildInventorySnapshotRows(
            inventoryItems.filter { it.status == ReagentInventoryStatus.IN_STOCK || it.status == ReagentInventoryStatus.OPENED },
            "ACTUAL_END",
        )

        val existingReport = reagentConsumptionReportRepository.findAllByAnalyzerIdOrderByGeneratedAtDesc(analyzerId)
            .toList()
            .map { it.toModel() }
            .firstOrNull { it.periodStart == periodStart && it.periodEnd == periodEnd }

        val report = ReagentConsumptionReport(
            id = existingReport?.id ?: UUID.randomUUID().toString(),
            analyzerId = analyzerId,
            periodStart = periodStart,
            periodEnd = periodEnd,
            legitimateTestCount = legitimateSamples.size,
            legitimateReagentConsumptionJson = toJson(legitimateConsumption),
            legitimateCostTenge = legitimateConsumption.sumOf { it.estimatedCostTenge },
            serviceOperationsJson = toJson(serviceOperations),
            serviceReagentConsumptionJson = toJson(serviceConsumption),
            serviceCostTenge = serviceConsumption.sumOf { it.estimatedCostTenge },
            suspiciousTestCount = suspiciousSamples.size,
            rerunTestCount = rerunSamples.size,
            washTestCount = washSamples.size,
            unauthorizedReagentConsumptionJson = toJson(unauthorizedConsumption),
            unauthorizedCostTenge = unauthorizedConsumption.sumOf { it.estimatedCostTenge },
            inventoryStartJson = toJson(emptyList<Any>()),
            inventoryReceivedJson = toJson(inventoryReceived),
            inventoryEndExpectedJson = toJson(emptyList<Any>()),
            inventoryEndActualJson = toJson(inventoryEndActual),
            discrepancyJson = toJson(emptyList<Any>()),
            discrepancyTotalTenge = 0.0,
            generatedBy = generatedBy,
            generatedAt = LocalDateTime.now(),
            version = existingReport?.version,
        )

        return reagentConsumptionReportRepository.save(report.toEntity()).toModel()
    }

    private fun buildConsumptionRows(
        samples: List<ParsedAnalyzerSample>,
        rates: List<AnalyzerReagentRate>,
        priceMap: Map<PriceKey, Double>,
        stream: String,
    ): List<ReagentConsumptionRow> {
        if (samples.isEmpty()) {
            return emptyList()
        }

        val grouped = linkedMapOf<ConsumptionKey, ReagentConsumptionRow>()
        samples.forEach { sample ->
            resolvePatientTestRates(sample, rates).forEach { rate ->
                val volume = rate.volumePerOperationMl ?: 0.0
                val units = rate.unitsPerOperation ?: 0
                val key = ConsumptionKey(
                    stream = stream,
                    reagentName = rate.reagentName,
                    unitType = rate.unitType,
                    testMode = rate.testMode,
                    sourceOperationType = rate.operationType.name,
                )
                val current = grouped.getOrPut(key) {
                    ReagentConsumptionRow(
                        stream = stream,
                        reagentName = rate.reagentName,
                        unitType = rate.unitType,
                        testMode = rate.testMode,
                        sourceOperationType = rate.operationType.name,
                        operationCount = 0,
                        totalVolumeMl = 0.0,
                        totalUnits = 0,
                        estimatedCostTenge = 0.0,
                    )
                }
                val estimatedCost = when (rate.unitType) {
                    ReagentUnitType.ML, ReagentUnitType.LITER, ReagentUnitType.UL -> volume * priceMap[PriceKey(rate.reagentName, rate.unitType)].orZero()
                    ReagentUnitType.MG, ReagentUnitType.G, ReagentUnitType.KG -> volume * priceMap[PriceKey(rate.reagentName, rate.unitType)].orZero()
                    ReagentUnitType.IU, ReagentUnitType.MILLI_IU -> units * priceMap[PriceKey(rate.reagentName, rate.unitType)].orZero()
                    ReagentUnitType.PIECE, ReagentUnitType.TEST, ReagentUnitType.KIT, ReagentUnitType.TEST_POSITION,
                    ReagentUnitType.STRIP, ReagentUnitType.SLIDE, ReagentUnitType.CHIP, ReagentUnitType.DISK, ReagentUnitType.PLATE, ReagentUnitType.WELL -> units * priceMap[PriceKey(rate.reagentName, rate.unitType)].orZero()
                    ReagentUnitType.BOX, ReagentUnitType.PACK, ReagentUnitType.CASE,
                    ReagentUnitType.BOTTLE, ReagentUnitType.VIAL, ReagentUnitType.AMPOULE, ReagentUnitType.TUBE, ReagentUnitType.CANISTER, ReagentUnitType.CARTRIDGE, ReagentUnitType.CASSETTE -> units * priceMap[PriceKey(rate.reagentName, rate.unitType)].orZero()
                }
                grouped[key] = current.copy(
                    operationCount = current.operationCount + 1,
                    totalVolumeMl = current.totalVolumeMl + volume,
                    totalUnits = current.totalUnits + units,
                    estimatedCostTenge = current.estimatedCostTenge + estimatedCost,
                )
            }
        }
        return grouped.values.toList()
    }

    private fun buildPriceMap(items: List<ReagentInventory>): Map<PriceKey, Double> {
        return items
            .filter { it.unitPriceTenge != null }
            .groupBy { PriceKey(it.reagentName, it.unitType) }
            .mapValues { (_, values) ->
                values.maxByOrNull { it.receivedAt.atStartOfDay() }?.unitPriceTenge.orZero()
            }
    }

    private fun buildInventorySnapshotRows(
        items: List<ReagentInventory>,
        snapshotType: String,
    ): List<InventorySnapshotRow> {
        return items.groupBy { InventorySnapshotKey(it.reagentName, it.unitType) }
            .entries
            .sortedBy { it.key.reagentName }
            .map { (key, values) ->
                InventorySnapshotRow(
                    snapshotType = snapshotType,
                    reagentName = key.reagentName,
                    unitType = key.unitType,
                    itemCount = values.size,
                    totalVolumeMl = values.sumOf { it.totalVolumeMl ?: 0.0 },
                    totalUnits = values.sumOf { it.totalUnits ?: 0 },
                    totalEstimatedValueTenge = values.sumOf {
                        when (key.unitType) {
                            ReagentUnitType.ML, ReagentUnitType.LITER, ReagentUnitType.UL -> (it.totalVolumeMl ?: 0.0) * it.unitPriceTenge.orZero()
                            ReagentUnitType.MG, ReagentUnitType.G, ReagentUnitType.KG -> (it.totalVolumeMl ?: 0.0) * it.unitPriceTenge.orZero()
                            ReagentUnitType.IU, ReagentUnitType.MILLI_IU -> (it.totalUnits ?: 0) * it.unitPriceTenge.orZero()
                            ReagentUnitType.PIECE, ReagentUnitType.TEST, ReagentUnitType.KIT, ReagentUnitType.TEST_POSITION,
                            ReagentUnitType.STRIP, ReagentUnitType.SLIDE, ReagentUnitType.CHIP, ReagentUnitType.DISK, ReagentUnitType.PLATE, ReagentUnitType.WELL -> (it.totalUnits ?: 0) * it.unitPriceTenge.orZero()
                            ReagentUnitType.BOX, ReagentUnitType.PACK, ReagentUnitType.CASE,
                            ReagentUnitType.BOTTLE, ReagentUnitType.VIAL, ReagentUnitType.AMPOULE, ReagentUnitType.TUBE, ReagentUnitType.CANISTER, ReagentUnitType.CARTRIDGE, ReagentUnitType.CASSETTE -> (it.totalUnits ?: 0) * it.unitPriceTenge.orZero()
                        }
                    },
                )
            }
    }

    private fun buildServiceOperationRows(samples: List<ParsedAnalyzerSample>): List<ServiceOperationRow> {
        return samples.groupingBy { it.classification.name }
            .eachCount()
            .entries
            .sortedBy { it.key }
            .map { ServiceOperationRow(it.key, it.value) }
    }

    private fun resolvePatientTestRates(
        sample: ParsedAnalyzerSample,
        rates: List<AnalyzerReagentRate>,
    ): List<AnalyzerReagentRate> {
        val patientTestRates = rates.filter { it.operationType == ReagentOperationType.PATIENT_TEST }
        val normalizedSampleMode = sample.testMode?.trim()?.takeIf { it.isNotEmpty() }
        val exact = if (normalizedSampleMode == null) {
            emptyList()
        } else {
            patientTestRates.filter { it.testMode?.trim()?.equals(normalizedSampleMode, ignoreCase = true) == true }
        }
        return if (exact.isNotEmpty()) {
            exact
        } else {
            patientTestRates.filter { it.testMode.isNullOrBlank() }
        }
    }

    private fun toJson(value: Any): String = objectMapper.writeValueAsString(value)

    private fun Double?.orZero(): Double = this ?: 0.0

    private data class ConsumptionKey(
        val stream: String,
        val reagentName: String,
        val unitType: ReagentUnitType,
        val testMode: String?,
        val sourceOperationType: String,
    )

    private data class PriceKey(
        val reagentName: String,
        val unitType: ReagentUnitType,
    )

    private data class InventorySnapshotKey(
        val reagentName: String,
        val unitType: ReagentUnitType,
    )

    private data class ReagentConsumptionRow(
        val stream: String,
        val reagentName: String,
        val unitType: ReagentUnitType,
        val testMode: String?,
        val sourceOperationType: String,
        val operationCount: Int,
        val totalVolumeMl: Double,
        val totalUnits: Int,
        val estimatedCostTenge: Double,
    )

    private data class ServiceOperationRow(
        val operationName: String,
        val count: Int,
    )

    private data class InventorySnapshotRow(
        val snapshotType: String,
        val reagentName: String,
        val unitType: ReagentUnitType,
        val itemCount: Int,
        val totalVolumeMl: Double,
        val totalUnits: Int,
        val totalEstimatedValueTenge: Double,
    )
}
