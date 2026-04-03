package lab.dev.med.univ.feature.reagents.domain.services

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import kotlinx.coroutines.flow.toList
import lab.dev.med.univ.feature.reagents.data.entity.toEntity
import lab.dev.med.univ.feature.reagents.data.entity.toModel
import lab.dev.med.univ.feature.reagents.data.repository.AnalyzerRepository
import lab.dev.med.univ.feature.reagents.data.repository.LogAnomalyDailySummaryRepository
import lab.dev.med.univ.feature.reagents.data.repository.LogAnomalyRecordRepository
import lab.dev.med.univ.feature.reagents.data.repository.ParsedAnalyzerSampleRepository
import lab.dev.med.univ.feature.reagents.data.repository.ServiceReagentConsumptionNormRepository
import lab.dev.med.univ.feature.reagents.domain.models.AnalyticsPeriod
import lab.dev.med.univ.feature.reagents.domain.models.AnalyzerAnomalySummary
import lab.dev.med.univ.feature.reagents.domain.models.AnomalyReagentEntry
import lab.dev.med.univ.feature.reagents.domain.models.AnomalyServiceEntry
import lab.dev.med.univ.feature.reagents.domain.models.AnomalyType
import lab.dev.med.univ.feature.reagents.domain.models.CrossRefStatus
import lab.dev.med.univ.feature.reagents.domain.models.DamumedServiceStat
import lab.dev.med.univ.feature.reagents.domain.models.LegitimateServiceEntry
import lab.dev.med.univ.feature.reagents.domain.models.LogAnalyticsResult
import lab.dev.med.univ.feature.reagents.domain.models.LogAnomalyDailySummary
import lab.dev.med.univ.feature.reagents.domain.models.LogAnomalyRecord
import lab.dev.med.univ.feature.reagents.domain.models.SampleClassification
import lab.dev.med.univ.feature.reporting.data.entity.DamumedNormalizedFactEntity
import lab.dev.med.univ.feature.reporting.data.entity.DamumedNormalizedFactDimensionEntity
import lab.dev.med.univ.feature.reporting.data.repository.DamumedNormalizedFactDimensionRepository
import lab.dev.med.univ.feature.reporting.data.repository.DamumedNormalizedFactRepository
import lab.dev.med.univ.feature.reporting.data.repository.DamumedReportUploadRepository
import lab.dev.med.univ.feature.reporting.domain.models.DamumedLabReportKind
import lab.dev.med.univ.feature.reporting.domain.models.DamumedReportNormalizationStatus
import org.springframework.stereotype.Service
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.UUID

interface LogAnomalyAnalysisService {
    suspend fun buildAnomaliesFromUpload(logUploadId: String)
    suspend fun getAnalytics(
        period: AnalyticsPeriod,
        analyzerId: String?,
        referenceDate: LocalDate,
        dateFrom: LocalDate? = null,
        dateTo: LocalDate? = null,
    ): LogAnalyticsResult
}

@Service
class LogAnomalyAnalysisServiceImpl(
    private val parsedSampleRepository: ParsedAnalyzerSampleRepository,
    private val anomalyRecordRepository: LogAnomalyRecordRepository,
    private val dailySummaryRepository: LogAnomalyDailySummaryRepository,
    private val analyzerRepository: AnalyzerRepository,
    private val normRepository: ServiceReagentConsumptionNormRepository,
    private val damumedUploadRepository: DamumedReportUploadRepository,
    private val damumedFactRepository: DamumedNormalizedFactRepository,
    private val damumedFactDimensionRepository: DamumedNormalizedFactDimensionRepository,
    private val objectMapper: ObjectMapper,
) : LogAnomalyAnalysisService {

    override suspend fun buildAnomaliesFromUpload(logUploadId: String) {
        anomalyRecordRepository.deleteAllByLogUploadId(logUploadId)

        val samples = parsedSampleRepository
            .findAllByLogUploadIdOrderBySampleTimestampAsc(logUploadId)
            .toList()

        val anomalyClassifications = setOf(
            SampleClassification.SUSPICIOUS,
            SampleClassification.ERROR,
            SampleClassification.XML_RESULT,
            SampleClassification.PROBABLE_RERUN,
        )

        val anomalySamples = samples.filter { it.classification in anomalyClassifications }
        if (anomalySamples.isEmpty()) return

        val allNorms = normRepository.findAllByIsActiveTrueOrderByServiceNameAsc().toList()

        val anomalyRecords = anomalySamples.map { sample ->
            val anomalyType = when (sample.classification) {
                SampleClassification.SUSPICIOUS -> AnomalyType.SUSPICIOUS
                SampleClassification.ERROR -> AnomalyType.ERROR
                SampleClassification.XML_RESULT -> AnomalyType.XML_RESULT
                SampleClassification.PROBABLE_RERUN -> AnomalyType.NO_LIS_ORDER
                else -> AnomalyType.ERROR
            }

            val sampleDate = sample.sampleTimestamp.toLocalDate()

            val matchingNorms = if (!sample.serviceName.isNullOrBlank()) {
                val normalizedServiceName = sample.serviceName.lowercase().trim()
                allNorms.filter { norm ->
                    norm.serviceNameNormalized.lowercase().let { normName ->
                        normalizedServiceName.contains(normName) || normName.contains(normalizedServiceName)
                    }
                }
            } else emptyList()

            val reagentEntries = matchingNorms.map { norm ->
                mapOf(
                    "reagentName" to norm.reagentName,
                    "unitType" to norm.unitType,
                    "quantity" to norm.quantityPerService,
                    "analyzerId" to (norm.analyzerId ?: ""),
                )
            }
            val reagentsJson = if (reagentEntries.isNotEmpty()) {
                objectMapper.writeValueAsString(reagentEntries)
            } else null

            LogAnomalyRecord(
                id = UUID.randomUUID().toString(),
                parsedSampleId = sample.id,
                logUploadId = logUploadId,
                analyzerId = sample.analyzerId,
                anomalyDate = sampleDate,
                anomalyTimestamp = sample.sampleTimestamp,
                barcode = sample.barcode.takeIf { it.isNotBlank() },
                deviceSystemName = sample.deviceSystemName,
                lisAnalyzerId = sample.lisAnalyzerId,
                anomalyType = anomalyType,
                classificationReason = sample.classificationReason,
                serviceId = sample.serviceId,
                serviceName = sample.serviceName,
                serviceCategory = null,
                testMode = sample.testMode,
                wbcValue = sample.wbcValue,
                rbcValue = sample.rbcValue,
                hgbValue = sample.hgbValue,
                pltValue = sample.pltValue,
                estimatedReagentsJson = reagentsJson,
                matchedDamumedFactId = null,
                crossRefStatus = CrossRefStatus.NOT_CHECKED,
            )
        }

        anomalyRecordRepository.saveAll(anomalyRecords.map { it.toEntity() }).toList()

        rebuildDailySummariesForUpload(logUploadId, samples)
    }

    private suspend fun rebuildDailySummariesForUpload(
        logUploadId: String,
        allSamples: List<lab.dev.med.univ.feature.reagents.data.entity.ParsedAnalyzerSampleEntity>,
    ) {
        val byDate = allSamples.groupBy { it.sampleTimestamp.toLocalDate() }

        for ((date, daySamples) in byDate) {
            val analyzerGroups = daySamples.groupBy { it.analyzerId }

            for ((analyzerId, analyzerSamples) in analyzerGroups) {
                val existing = dailySummaryRepository.findBySummaryDateAndAnalyzerId(date, analyzerId)

                val legitimateCount = analyzerSamples.count { it.classification == SampleClassification.LEGITIMATE }
                val suspiciousCount = analyzerSamples.count { it.classification == SampleClassification.SUSPICIOUS }
                val errorCount = analyzerSamples.count { it.classification == SampleClassification.ERROR || it.classification == SampleClassification.XML_RESULT }
                val washCount = analyzerSamples.count { it.classification == SampleClassification.WASH_TEST }
                val noLisCount = analyzerSamples.count { it.classification == SampleClassification.PROBABLE_RERUN }
                val anomalyCount = suspiciousCount + errorCount + noLisCount

                val summary = LogAnomalyDailySummary(
                    id = existing?.id ?: UUID.randomUUID().toString(),
                    summaryDate = date,
                    analyzerId = analyzerId,
                    totalSamples = analyzerSamples.size,
                    legitimateCount = legitimateCount,
                    anomalyCount = anomalyCount,
                    suspiciousCount = suspiciousCount,
                    noLisOrderCount = noLisCount,
                    errorCount = errorCount,
                    washTestCount = washCount,
                    damumedCompletedCount = existing?.damumedCompletedCount,
                    anomalyReagentsJson = null,
                    lastUpdatedAt = LocalDateTime.now(),
                )

                dailySummaryRepository.save(summary.toEntity(version = existing?.version))
            }
        }
    }

    override suspend fun getAnalytics(
        period: AnalyticsPeriod,
        analyzerId: String?,
        referenceDate: LocalDate,
        dateFrom: LocalDate?,
        dateTo: LocalDate?,
    ): LogAnalyticsResult {
        val periodFrom = dateFrom ?: referenceDate.minusDays(period.toDays() - 1)
        val periodTo = dateTo ?: referenceDate
        val periodFromDt = periodFrom.atStartOfDay()
        val periodToDt = periodTo.atTime(23, 59, 59)

        val dailySummaries = if (analyzerId != null) {
            dailySummaryRepository
                .findAllByAnalyzerIdAndSummaryDateBetweenOrderBySummaryDateAsc(analyzerId, periodFrom, periodTo)
                .toList()
        } else {
            dailySummaryRepository
                .findAllBySummaryDateBetweenOrderBySummaryDateAsc(periodFrom, periodTo)
                .toList()
        }.map { it.toModel() }

        val records = if (analyzerId != null) {
            anomalyRecordRepository
                .findAllByAnalyzerIdAndAnomalyDateBetweenOrderByAnomalyDateDescAnomalyTimestampDesc(
                    analyzerId, periodFrom, periodTo
                )
                .toList()
        } else {
            anomalyRecordRepository
                .findAllByAnomalyDateBetweenOrderByAnomalyDateDescAnomalyTimestampDesc(periodFrom, periodTo)
                .toList()
        }.map { it.toModel() }

        // Load all parsed samples for the period to compute legitimate services, patients, reagent consumption
        val allSamples = if (analyzerId != null) {
            parsedSampleRepository
                .findAllByAnalyzerIdAndSampleTimestampBetweenOrderBySampleTimestampAsc(
                    analyzerId, periodFromDt, periodToDt
                )
                .toList()
        } else {
            parsedSampleRepository
                .findAllBySampleTimestampBetweenOrderBySampleTimestampAsc(periodFromDt, periodToDt)
                .toList()
        }

        val allNorms = normRepository.findAllByIsActiveTrueOrderByServiceNameAsc().toList()

        val analyzers = analyzerRepository.findAllByOrderByNameAsc().toList()
        val analyzerNameById = analyzers.associate { it.id to it.name }

        val summariesByAnalyzer = dailySummaries
            .filter { it.analyzerId != null }
            .groupBy { it.analyzerId!! }

        val samplesByAnalyzer = allSamples.groupBy { it.analyzerId }

        val anomalyClassificationsSet = setOf(
            SampleClassification.SUSPICIOUS,
            SampleClassification.ERROR,
            SampleClassification.XML_RESULT,
            SampleClassification.PROBABLE_RERUN,
        )

        val byAnalyzer = analyzers.mapNotNull { analyzer ->
            val analyzerSummaries = summariesByAnalyzer[analyzer.id]
            val analyzerSamples = samplesByAnalyzer[analyzer.id] ?: emptyList()
            if (analyzerSummaries == null && analyzerSamples.isEmpty()) return@mapNotNull null
            // Prefer direct sample counts (always accurate) over daily summary aggregates
            val anomalyCountDirect = analyzerSamples.count { it.classification in anomalyClassificationsSet }
            val legitimateCountDirect = analyzerSamples.count { it.classification == SampleClassification.LEGITIMATE }
            val washCountDirect = analyzerSamples.count { it.classification == SampleClassification.WASH_TEST }
            AnalyzerAnomalySummary(
                analyzerId = analyzer.id,
                analyzerName = analyzer.name,
                anomalyCount = if (analyzerSamples.isNotEmpty()) anomalyCountDirect
                    else analyzerSummaries?.sumOf { it.anomalyCount } ?: 0,
                legitimateCount = if (analyzerSamples.isNotEmpty()) legitimateCountDirect
                    else analyzerSummaries?.sumOf { it.legitimateCount } ?: 0,
                totalSamples = analyzerSamples.size,
                washTestCount = if (analyzerSamples.isNotEmpty()) washCountDirect
                    else analyzerSummaries?.sumOf { it.washTestCount } ?: 0,
                damumedCompletedCount = analyzerSummaries?.mapNotNull { it.damumedCompletedCount }?.sumOf { it }
                    ?.takeIf { it > 0 },
            )
        }

        // Anomaly services with reagent totals
        val anomalyServiceGroups = records
            .filter { !it.serviceName.isNullOrBlank() }
            .groupBy { it.serviceName!! }

        val anomalyServices = anomalyServiceGroups.map { (serviceName, serviceRecords) ->
            val analyzerIdForService = serviceRecords.firstOrNull()?.analyzerId
            val reagentTotals = mutableMapOf<String, Double>()
            val reagentUnits = mutableMapOf<String, String>()

            serviceRecords.forEach { record ->
                if (!record.estimatedReagentsJson.isNullOrBlank()) {
                    runCatching {
                        val entries = objectMapper.readValue<List<Map<String, Any>>>(record.estimatedReagentsJson)
                        entries.forEach { entry ->
                            val name = entry["reagentName"]?.toString() ?: return@forEach
                            val qty = (entry["quantity"] as? Number)?.toDouble() ?: 0.0
                            val unit = entry["unitType"]?.toString() ?: ""
                            reagentTotals[name] = (reagentTotals[name] ?: 0.0) + qty
                            reagentUnits[name] = unit
                        }
                    }
                }
            }

            val reagents = reagentTotals.map { (name, qty) ->
                AnomalyReagentEntry(
                    reagentName = name,
                    unitType = reagentUnits[name] ?: "",
                    totalQuantity = qty,
                    analyzerId = analyzerIdForService,
                    analyzerName = analyzerIdForService?.let { analyzerNameById[it] },
                )
            }

            AnomalyServiceEntry(
                serviceName = serviceName,
                serviceCategory = serviceRecords.firstOrNull()?.serviceCategory,
                anomalyCount = serviceRecords.size,
                analyzerId = analyzerIdForService,
                analyzerName = analyzerIdForService?.let { analyzerNameById[it] },
                reagents = reagents,
            )
        }.sortedByDescending { it.anomalyCount }

        // Legitimate services from parsed samples (with LIS order)
        val legitimateSamples = allSamples.filter {
            it.classification == SampleClassification.LEGITIMATE
        }

        val legitServiceGroups = legitimateSamples
            .groupBy { if (!it.serviceName.isNullOrBlank()) it.serviceName!! else "(без услуги)" }

        val legitimateServices = legitServiceGroups.map { (serviceName, serviceSamples) ->
            val aId = serviceSamples.firstOrNull()?.analyzerId
            val svcId = serviceSamples.firstOrNull()?.serviceId
            val uniqueBarcodes = serviceSamples.map { it.barcode }.filter { it.isNotBlank() }.toSet().size

            // Reagent consumption for legitimate samples
            val normalizedName = serviceName.lowercase().trim()
            val matchingNorms = allNorms.filter { norm ->
                norm.serviceNameNormalized.lowercase().let { normName ->
                    normalizedName.contains(normName) || normName.contains(normalizedName)
                }
            }
            val legitReagentTotals = mutableMapOf<String, Double>()
            val legitReagentUnits = mutableMapOf<String, String>()
            repeat(serviceSamples.size) {
                matchingNorms.forEach { norm ->
                    legitReagentTotals[norm.reagentName] = (legitReagentTotals[norm.reagentName] ?: 0.0) + norm.quantityPerService.toDouble()
                    legitReagentUnits[norm.reagentName] = norm.unitType.name
                }
            }
            val reagents = legitReagentTotals.map { (name, qty) ->
                AnomalyReagentEntry(
                    reagentName = name,
                    unitType = legitReagentUnits[name] ?: "",
                    totalQuantity = qty,
                    analyzerId = aId,
                    analyzerName = aId?.let { analyzerNameById[it] },
                )
            }.sortedByDescending { it.totalQuantity }

            LegitimateServiceEntry(
                serviceName = serviceName,
                serviceId = svcId,
                analyzerId = aId,
                analyzerName = aId?.let { analyzerNameById[it] },
                count = serviceSamples.size,
                uniquePatients = uniqueBarcodes,
                reagents = reagents,
            )
        }.sortedByDescending { it.count }

        // Unique patients = unique barcodes from legitimate samples
        val uniquePatients = legitimateSamples.map { it.barcode }.filter { it.isNotBlank() }.toSet().size
        val avgServicesPerPatient = if (uniquePatients > 0) {
            legitimateSamples.size.toDouble() / uniquePatients
        } else 0.0

        // Top reagents for anomaly samples
        val allReagentTotals = mutableMapOf<String, Double>()
        val allReagentUnits = mutableMapOf<String, String>()
        val allReagentAnalyzerIds = mutableMapOf<String, String?>()
        anomalyServices.forEach { service ->
            service.reagents.forEach { r ->
                allReagentTotals[r.reagentName] = (allReagentTotals[r.reagentName] ?: 0.0) + r.totalQuantity
                allReagentUnits[r.reagentName] = r.unitType
                allReagentAnalyzerIds[r.reagentName] = r.analyzerId
            }
        }
        val topReagents = allReagentTotals
            .map { (name, qty) ->
                val aId = allReagentAnalyzerIds[name]
                AnomalyReagentEntry(
                    reagentName = name,
                    unitType = allReagentUnits[name] ?: "",
                    totalQuantity = qty,
                    analyzerId = aId,
                    analyzerName = aId?.let { analyzerNameById[it] },
                )
            }
            .sortedByDescending { it.totalQuantity }

        // Top reagents for legitimate samples
        val legitReagentTotals = mutableMapOf<String, Double>()
        val legitReagentUnits = mutableMapOf<String, String>()
        val legitReagentAnalyzerIds = mutableMapOf<String, String?>()
        legitimateServices.forEach { service ->
            service.reagents.forEach { r ->
                legitReagentTotals[r.reagentName] = (legitReagentTotals[r.reagentName] ?: 0.0) + r.totalQuantity
                legitReagentUnits[r.reagentName] = r.unitType
                legitReagentAnalyzerIds[r.reagentName] = r.analyzerId
            }
        }
        val topReagentsLegitimate = legitReagentTotals
            .map { (name, qty) ->
                val aId = legitReagentAnalyzerIds[name]
                AnomalyReagentEntry(
                    reagentName = name,
                    unitType = legitReagentUnits[name] ?: "",
                    totalQuantity = qty,
                    analyzerId = aId,
                    analyzerName = aId?.let { analyzerNameById[it] },
                )
            }
            .sortedByDescending { it.totalQuantity }

        // Use direct sample counts as primary source of truth; fall back to daily summary aggregates
        // when no raw samples are available (e.g. older data not re-parsed).
        val totalLegitimate: Int
        val totalAnomalies: Int
        val totalWashTests: Int
        val effectiveByDay: List<LogAnomalyDailySummary>

        if (allSamples.isNotEmpty()) {
            totalLegitimate = allSamples.count { it.classification == SampleClassification.LEGITIMATE }
            totalAnomalies = allSamples.count { it.classification in anomalyClassificationsSet }
            totalWashTests = allSamples.count { it.classification == SampleClassification.WASH_TEST }

            // Build per-day summaries directly from raw samples if daily summary table is empty
            effectiveByDay = if (dailySummaries.isNotEmpty()) {
                dailySummaries
            } else {
                allSamples.groupBy { it.sampleTimestamp.toLocalDate() }
                    .map { (date, daySamples) ->
                        val groupedByAnalyzer = daySamples.groupBy { it.analyzerId }
                        groupedByAnalyzer.map { (aId, aSamples) ->
                            LogAnomalyDailySummary(
                                id = "${date}_${aId}",
                                summaryDate = date,
                                analyzerId = aId,
                                totalSamples = aSamples.size,
                                legitimateCount = aSamples.count { it.classification == SampleClassification.LEGITIMATE },
                                anomalyCount = aSamples.count { it.classification in anomalyClassificationsSet },
                                suspiciousCount = aSamples.count { it.classification == SampleClassification.SUSPICIOUS },
                                noLisOrderCount = aSamples.count { it.classification == SampleClassification.PROBABLE_RERUN },
                                errorCount = aSamples.count { it.classification == SampleClassification.ERROR || it.classification == SampleClassification.XML_RESULT },
                                washTestCount = aSamples.count { it.classification == SampleClassification.WASH_TEST },
                            )
                        }
                    }.flatten()
                    .sortedBy { it.summaryDate }
            }
        } else {
            totalLegitimate = dailySummaries.sumOf { it.legitimateCount }
            totalAnomalies = dailySummaries.sumOf { it.anomalyCount }
            totalWashTests = dailySummaries.sumOf { it.washTestCount }
            effectiveByDay = dailySummaries
        }
        val totalSamples = totalLegitimate + totalAnomalies + totalWashTests

        // ── Damumed LIS report facts ──────────────────────────────────────────────
        val damumedServiceStats = runCatching {
            val latestUpload = damumedUploadRepository
                .findAllByOrderByUploadedAtDesc()
                .toList()
                .firstOrNull {
                    it.reportKind == DamumedLabReportKind.WORKPLACE_COMPLETED_STUDIES &&
                        it.normalizationStatus == DamumedReportNormalizationStatus.NORMALIZED
                }

            if (latestUpload != null) {
                val facts = damumedFactRepository
                    .findAllByUploadIdOrderBySheetIdAscSourceRowIndexAscSourceColumnIndexAsc(latestUpload.id)
                    .toList()
                val factIds = facts.map { it.entityId }
                val allDimensions = if (factIds.isNotEmpty()) {
                    damumedFactDimensionRepository.findAllByFactIdInOrderByAxisKeyAsc(factIds).toList()
                } else emptyList()
                val dimensionsByFactId = allDimensions.groupBy { it.factId }
                buildDamumedServiceStats(facts, dimensionsByFactId)
            } else {
                emptyList()
            }
        }.getOrElse { emptyList() }

        val damumedTotalCompleted = damumedServiceStats.sumOf { it.completedCount }

        val analyzerServiceCounts = allSamples
            .filter { !it.serviceName.isNullOrBlank() }
            .groupBy { it.serviceName!!.trim() }
            .mapValues { it.value.size }

        val enrichedDamumedStats = damumedServiceStats.map { stat ->
            val count = analyzerServiceCounts[stat.serviceName]
                ?: analyzerServiceCounts.entries
                    .firstOrNull { (k, _) -> k.equals(stat.serviceName, ignoreCase = true) }?.value
                ?: 0
            stat.copy(analyzerCount = count)
        }

        val totalAnalyzerServiceCount = allSamples
            .filter { !it.serviceName.isNullOrBlank() }
            .size

        return LogAnalyticsResult(
            periodFrom = periodFrom,
            periodTo = periodTo,
            totalSamples = totalSamples,
            totalAnomalies = totalAnomalies,
            totalLegitimate = totalLegitimate,
            totalWashTests = totalWashTests,
            uniquePatients = uniquePatients,
            avgServicesPerPatient = avgServicesPerPatient,
            damumedTotalCompleted = damumedTotalCompleted,
            totalAnalyzerServiceCount = totalAnalyzerServiceCount,
            damumedServiceStats = enrichedDamumedStats,
            byDay = effectiveByDay,
            byAnalyzer = byAnalyzer,
            anomalyServices = anomalyServices,
            legitimateServices = legitimateServices,
            topReagents = topReagents,
            topReagentsLegitimate = topReagentsLegitimate,
            records = records,
        )
    }

    private fun buildDamumedServiceStats(
        facts: List<DamumedNormalizedFactEntity>,
        dimensionsByFactId: Map<String, List<DamumedNormalizedFactDimensionEntity>>,
    ): List<DamumedServiceStat> {
        return facts
            .filter { it.metricKey == "completed_count" && (it.numericValue ?: 0.0) > 0 }
            .mapNotNull { fact ->
                val dims = dimensionsByFactId[fact.entityId] ?: emptyList()
                val serviceName = dims.firstOrNull { it.axisKey == "service" }?.rawValue?.trim()
                    ?.takeIf { it.isNotBlank() }
                    ?: return@mapNotNull null
                val count = fact.numericValue?.toInt()?.takeIf { it > 0 } ?: return@mapNotNull null
                DamumedServiceStat(
                    serviceName = serviceName,
                    completedCount = count,
                    category = detectServiceCategory(serviceName),
                )
            }
            .groupBy { it.serviceName }
            .map { (serviceName, entries) ->
                DamumedServiceStat(
                    serviceName = serviceName,
                    completedCount = entries.sumOf { it.completedCount },
                    category = entries.firstOrNull()?.category,
                )
            }
            .sortedByDescending { it.completedCount }
    }

    private fun detectServiceCategory(serviceName: String): String? {
        val n = serviceName.lowercase()
        return when {
            n.contains("гематолог") || n.contains("общ") && n.contains("крови") ||
                n.contains("лейкоцит") || n.contains("тромбоцит") || n.contains("гемоглобин") -> "Гематология"
            n.contains("биохим") || n.contains("глюкоз") || n.contains("холестерин") ||
                n.contains("белок") || n.contains("ферм") || n.contains("фибриноген") -> "Биохимия"
            n.contains("иммунол") || n.contains("гормон") || n.contains("антитело") ||
                n.contains("антиген") || n.contains("иммунофер") -> "Иммунология"
            n.contains("коагул") || n.contains("свёрт") || n.contains("протромб") ||
                n.contains("мно ") -> "Коагуляция"
            n.contains("мочи") || n.contains("урин") || n.contains("моча") -> "Мочи"
            else -> null
        }
    }
}
