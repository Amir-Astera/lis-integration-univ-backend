package lab.dev.med.univ.feature.reagents.domain.services

import kotlinx.coroutines.flow.toList
import lab.dev.med.univ.feature.reagents.data.entity.ReconciliationSummaryEntity
import lab.dev.med.univ.feature.reagents.data.entity.SampleReconciliationEntity
import lab.dev.med.univ.feature.reagents.data.entity.toModel
import lab.dev.med.univ.feature.reagents.data.repository.AnalyzerRepository
import lab.dev.med.univ.feature.reagents.data.repository.ParsedAnalyzerSampleRepository
import lab.dev.med.univ.feature.reagents.data.repository.ReconciliationSummaryRepository
import lab.dev.med.univ.feature.reagents.data.repository.SampleReconciliationRepository
import lab.dev.med.univ.feature.reagents.domain.models.ReconciliationAnalyzerSummary
import lab.dev.med.univ.feature.reagents.domain.models.ReconciliationDailySummary
import lab.dev.med.univ.feature.reagents.domain.models.ReconciliationDailyPoint
import lab.dev.med.univ.feature.reagents.domain.models.ReconciliationKpiSummary
import lab.dev.med.univ.feature.reagents.domain.models.ReconciliationServiceRow
import lab.dev.med.univ.feature.reagents.domain.models.ReconciliationStatus
import lab.dev.med.univ.feature.reagents.domain.models.SampleClassification
import lab.dev.med.univ.feature.reagents.domain.models.SampleReconciliation
import lab.dev.med.univ.feature.reagents.domain.models.SampleReconciliationStatus
import lab.dev.med.univ.feature.reagents.domain.models.ServiceCatalogEntry
import lab.dev.med.univ.feature.reagents.domain.models.ServiceMatchConfidence
import lab.dev.med.univ.feature.reporting.data.repository.CompletedLabStudiesJournalReferralIndexRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.UUID

interface ReconciliationSummaryService {

    /**
     * Rebuild reconciliation_daily_summary rows for the given date range.
     * Deletes existing rows for that range+analyzer then recomputes from raw data.
     * Called after each log upload parse or LIS journal ingest.
     */
    suspend fun rebuildForDateRange(dateFrom: LocalDate, dateTo: LocalDate, analyzerId: String? = null)

    /** KPI card data for dashboard summary widget. */
    suspend fun getKpiSummary(dateFrom: LocalDate, dateTo: LocalDate, analyzerId: String? = null): ReconciliationKpiSummary

    /** Daily trend points for time-series chart. */
    suspend fun getDailyTimeline(dateFrom: LocalDate, dateTo: LocalDate, analyzerId: String? = null): List<ReconciliationDailyPoint>

    /** Per-analyzer leaderboard. */
    suspend fun getByAnalyzer(dateFrom: LocalDate, dateTo: LocalDate): List<ReconciliationAnalyzerSummary>

    /** Per-service comparison table with pagination. */
    suspend fun getByService(
        dateFrom: LocalDate,
        dateTo: LocalDate,
        analyzerId: String? = null,
        page: Int = 0,
        size: Int = 50,
    ): List<ReconciliationServiceRow>

    /**
     * Drill-down: return per-sample reconciliation rows for a narrow slice
     * (date range + optional catalog/analyzer/status).
     * Used by UI для списка конкретных проб расхождения.
     */
    suspend fun getDrillDown(
        dateFrom: LocalDate,
        dateTo: LocalDate,
        catalogId: String? = null,
        analyzerId: String? = null,
        status: SampleReconciliationStatus? = null,
        page: Int = 0,
        size: Int = 50,
    ): DrillDownPage

    /**
     * Promote samples whose grace window has expired from PENDING_GRACE to DISCREPANCY.
     * Called by the scheduler (hourly) and after any LIS upload.
     * Returns number of samples promoted.
     */
    suspend fun promoteExpiredGraceSamples(batchSize: Int = 500): Int
}

/** Paginated drill-down result. */
data class DrillDownPage(
    val rows: List<SampleReconciliation>,
    val total: Long,
    val page: Int,
    val size: Int,
)

@Service
class ReconciliationSummaryServiceImpl(
    private val parsedSampleRepository: ParsedAnalyzerSampleRepository,
    private val journalIndexRepository: CompletedLabStudiesJournalReferralIndexRepository,
    private val reconciliationRepository: ReconciliationSummaryRepository,
    private val sampleReconciliationRepository: SampleReconciliationRepository,
    private val analyzerRepository: AnalyzerRepository,
    private val serviceMatchingService: ServiceMatchingService,
) : ReconciliationSummaryService {

    private val log = LoggerFactory.getLogger(javaClass)

    // ─── Public API ────────────────────────────────────────────────────────────

    override suspend fun rebuildForDateRange(dateFrom: LocalDate, dateTo: LocalDate, analyzerId: String?) {
        log.info("Rebuilding reconciliation summary: {} – {}, analyzer={}", dateFrom, dateTo, analyzerId ?: "ALL")

        // Ensure matching catalog is fresh
        serviceMatchingService.reload()

        // Load samples for the period
        val startTs = dateFrom.atStartOfDay()
        val endTs   = dateTo.plusDays(1).atStartOfDay()

        val samples = if (analyzerId != null) {
            parsedSampleRepository.findAllByAnalyzerIdAndSampleTimestampBetweenOrderBySampleTimestampAsc(analyzerId, startTs, endTs)
        } else {
            parsedSampleRepository.findAllBySampleTimestampBetweenOrderBySampleTimestampAsc(startTs, endTs)
        }.toList()

        if (samples.isEmpty()) {
            log.debug("No samples found for period {} – {}; clearing existing rows only", dateFrom, dateTo)
            deleteExistingRows(dateFrom, dateTo, analyzerId)
            return
        }

        // Load LIS service stats for the same period
        val lisServiceStats: Map<String, Int> = try {
            journalIndexRepository.loadServiceStatsByDateRange(dateFrom, dateTo)
        } catch (ex: Exception) {
            log.warn("Failed to load LIS service stats for {} – {}: {}", dateFrom, dateTo, ex.message)
            emptyMap()
        }

        // Precompute once: catalogId → total LIS count across the period.
        // Only matches at or stronger than BY_LIS_ALIAS are trusted for the LIS↔logs join
        // (BY_FUZZY is too noisy for financial aggregation).
        val lisCountByCatalogId: Map<String, Int> = buildMap {
            for ((lisName, count) in lisServiceStats) {
                val m = serviceMatchingService.matchByName(lisName)
                val id = m.entry?.id ?: continue
                if (!m.confidence.isAtLeastAsStrongAs(ServiceMatchConfidence.BY_LIS_ALIAS)) continue
                merge(id, count) { a, b -> a + b }
            }
        }

        val now = LocalDateTime.now()

        // ─── Group samples by (date × analyzerId × catalogEntryId) ──────────
        // key = Triple(summaryDate, analyzerId, catalogEntry.id or UNKNOWN_KEY)
        data class GroupKey(val date: LocalDate, val analyzerId: String?, val catalogId: String, val catalogEntry: ServiceCatalogEntry?, val rawServiceName: String)

        val grouped = linkedMapOf<GroupKey, MutableList<SampleRow>>()
        val perSampleEntities = mutableListOf<SampleReconciliationEntity>()

        for (sample in samples) {
            val sampleDate = sample.sampleTimestamp.toLocalDate()
            val effectiveAnalyzerId = sample.analyzerId

            // Find catalog entry: try service_id first, then service_name
            val matchResult = when {
                sample.serviceId != null -> {
                    val byId = serviceMatchingService.matchByServiceId(sample.serviceId)
                    if (byId.isMatch) byId else serviceMatchingService.matchByName(sample.serviceName.orEmpty())
                }
                sample.serviceName != null -> serviceMatchingService.matchByName(sample.serviceName)
                else -> null
            }

            val catalogEntry   = matchResult?.entry
            val catalogId      = catalogEntry?.id ?: UNKNOWN_CATALOG_KEY
            val rawServiceName = sample.serviceName.orEmpty().ifBlank { catalogEntry?.canonicalName ?: "Неизвестно" }
            val graceHours     = catalogEntry?.graceHours ?: 0

            val sampleRow = SampleRow(
                classification   = sample.classification,
                sampleTimestamp  = sample.sampleTimestamp,
                graceHours       = graceHours,
                now              = now,
            )

            val key = GroupKey(sampleDate, effectiveAnalyzerId, catalogId, catalogEntry, rawServiceName)
            grouped.getOrPut(key) { mutableListOf() } += sampleRow

            // ── Per-sample reconciliation row ──────────────────────────────
            val perSampleStatus = resolveSampleStatus(sampleRow)
            val graceDeadline   = if (graceHours > 0) sample.sampleTimestamp.plusHours(graceHours.toLong()) else null
            val lisPrice        = catalogEntry?.lisPriceTenge
            val waste           = if (perSampleStatus == SampleReconciliationStatus.DISCREPANCY && lisPrice != null) lisPrice else BigDecimal.ZERO

            perSampleEntities += SampleReconciliationEntity(
                id                    = UUID.randomUUID().toString(),
                parsedSampleId        = sample.id,
                sampleDate            = sampleDate,
                analyzerId            = effectiveAnalyzerId,
                serviceCatalogId      = catalogEntry?.id,
                serviceNameRaw        = sample.serviceName,
                serviceNameCanonical  = catalogEntry?.canonicalName,
                category              = catalogEntry?.category?.name,
                reconciliationStatus  = perSampleStatus.name,
                reason                = sample.classificationReason,
                graceHours            = graceHours,
                graceDeadlineAt       = graceDeadline,
                matchConfidence       = matchResult?.confidence?.name,
                lisPricePerTestTenge  = lisPrice,
                estimatedWasteTenge   = waste,
                lisReferralKey        = null,
                reconciledAt          = now,
            )
        }

        // ─── Build reconciliation rows ────────────────────────────────────────
        deleteExistingRows(dateFrom, dateTo, analyzerId)
        sampleReconciliationRepository.deleteByDateRangeAndAnalyzer(dateFrom, dateTo, analyzerId)

        // LIS counts are period-totals per catalog entry. To avoid double counting
        // when the same catalog entry spans multiple (date, analyzer) groups, we
        // attribute the full LIS count only to the FIRST group that references
        // the catalog entry; subsequent groups get lisCount = 0 for that entry.
        val lisCountConsumed = mutableSetOf<String>()

        val entitiesToSave = grouped.map { (key, sampleRows) ->
            val logsCount       = sampleRows.count { !it.isWashTest }
            val washTestCount   = sampleRows.count { it.isWashTest }
            val reconciledCount = sampleRows.count { it.isReconciled }
            val pendingGrace    = sampleRows.count { !it.isReconciled && !it.isWashTest && it.isInGraceWindow }
            val discrepancy     = maxOf(0, logsCount - reconciledCount - pendingGrace)

            val lisCount = key.catalogEntry?.id?.let { catId ->
                if (lisCountConsumed.add(catId)) lisCountByCatalogId[catId] ?: 0 else 0
            } ?: 0

            val lisPrice = key.catalogEntry?.lisPriceTenge
            val wastedCost = if (lisPrice != null && discrepancy > 0) {
                lisPrice.multiply(BigDecimal(discrepancy))
            } else {
                BigDecimal.ZERO
            }

            ReconciliationSummaryEntity(
                id                       = UUID.randomUUID().toString(),
                summaryDate              = key.date,
                analyzerId               = key.analyzerId,
                serviceCatalogId         = key.catalogEntry?.id,
                serviceNameCanonical     = key.catalogEntry?.canonicalName ?: key.rawServiceName,
                category                 = key.catalogEntry?.category?.name,
                logsCount                = logsCount,
                lisCount                 = lisCount,
                reconciledCount          = reconciledCount,
                discrepancyCount         = discrepancy,
                pendingGraceCount        = pendingGrace,
                washTestCount            = washTestCount,
                estimatedWastedCostTenge = wastedCost,
                lisPricePerTestTenge     = lisPrice,
                lastRebuiltAt            = now,
            )
        }

        reconciliationRepository.saveAll(entitiesToSave).toList()
        if (perSampleEntities.isNotEmpty()) {
            sampleReconciliationRepository.saveAll(perSampleEntities).toList()
        }
        log.info(
            "Saved {} daily summary rows and {} per-sample rows for {} – {}",
            entitiesToSave.size, perSampleEntities.size, dateFrom, dateTo,
        )
    }

    override suspend fun getKpiSummary(dateFrom: LocalDate, dateTo: LocalDate, analyzerId: String?): ReconciliationKpiSummary {
        val rows = loadRows(dateFrom, dateTo, analyzerId)

        val totalLogs        = rows.sumOf { it.logsCount }
        val totalLis         = rows.sumOf { it.lisCount }
        val totalReconciled  = rows.sumOf { it.reconciledCount }
        val totalDiscrep     = rows.sumOf { it.discrepancyCount }
        val totalGrace       = rows.sumOf { it.pendingGraceCount }
        val totalWash        = rows.sumOf { it.washTestCount }
        val totalWasted      = rows.fold(BigDecimal.ZERO) { acc, r -> acc + r.estimatedWastedCostTenge }

        val analyzerIds = rows.mapNotNull { it.analyzerId }.toSet()
        val affectedServices = rows.filter { it.discrepancyCount > 0 }.map { it.serviceNameCanonical }.toSet()

        return ReconciliationKpiSummary(
            periodFrom           = dateFrom,
            periodTo             = dateTo,
            totalLogsCount       = totalLogs,
            totalLisCount        = totalLis,
            totalReconciledCount = totalReconciled,
            totalDiscrepancyCount = totalDiscrep,
            totalPendingGraceCount = totalGrace,
            totalWashTestCount   = totalWash,
            totalWastedCostTenge = totalWasted,
            discrepancyRate      = if (totalLogs > 0) totalDiscrep.toDouble() / totalLogs else 0.0,
            analyzerCount        = analyzerIds.size,
            affectedServiceCount = affectedServices.size,
        )
    }

    override suspend fun getDailyTimeline(dateFrom: LocalDate, dateTo: LocalDate, analyzerId: String?): List<ReconciliationDailyPoint> {
        return reconciliationRepository.findDailyTimeline(dateFrom, dateTo, analyzerId)
            .toList()
            .map { entity ->
                ReconciliationDailyPoint(
                    date              = entity.summaryDate,
                    logsCount         = entity.logsCount,
                    lisCount          = entity.lisCount,
                    discrepancyCount  = entity.discrepancyCount,
                    pendingGraceCount = entity.pendingGraceCount,
                    wastedCostTenge   = entity.estimatedWastedCostTenge,
                )
            }
    }

    override suspend fun getByAnalyzer(dateFrom: LocalDate, dateTo: LocalDate): List<ReconciliationAnalyzerSummary> {
        val rows = loadRows(dateFrom, dateTo, null)
        val analyzerNames = analyzerRepository.findAllByOrderByNameAsc().toList()
            .associate { it.id to it.name }

        return rows
            .groupBy { it.analyzerId }
            .map { (aid, aRows) ->
                val logs       = aRows.sumOf { it.logsCount }
                val lis        = aRows.sumOf { it.lisCount }
                val reconciled = aRows.sumOf { it.reconciledCount }
                val discrep    = aRows.sumOf { it.discrepancyCount }
                val wasted     = aRows.fold(BigDecimal.ZERO) { acc, r -> acc + r.estimatedWastedCostTenge }
                ReconciliationAnalyzerSummary(
                    analyzerId      = aid,
                    analyzerName    = aid?.let { analyzerNames[it] },
                    logsCount       = logs,
                    lisCount        = lis,
                    discrepancyCount = discrep,
                    reconciledCount  = reconciled,
                    discrepancyRate  = if (logs > 0) discrep.toDouble() / logs else 0.0,
                    wastedCostTenge  = wasted,
                )
            }
            .sortedByDescending { it.discrepancyCount }
    }

    override suspend fun getByService(
        dateFrom: LocalDate,
        dateTo: LocalDate,
        analyzerId: String?,
        page: Int,
        size: Int,
    ): List<ReconciliationServiceRow> {
        return reconciliationRepository.findAggregatedByService(
            from       = dateFrom,
            to         = dateTo,
            analyzerId = analyzerId,
            pageSize   = size,
            offset     = page * size,
        ).toList().map { entity ->
            val discrep = entity.discrepancyCount
            val logs    = entity.logsCount
            ReconciliationServiceRow(
                serviceCatalogId      = entity.serviceCatalogId,
                serviceNameCanonical  = entity.serviceNameCanonical,
                category              = entity.category,
                logsCount             = logs,
                lisCount              = entity.lisCount,
                discrepancyCount      = discrep,
                reconciledCount       = entity.reconciledCount,
                pendingGraceCount     = entity.pendingGraceCount,
                discrepancyRate       = if (logs > 0) discrep.toDouble() / logs else 0.0,
                wastedCostTenge       = entity.estimatedWastedCostTenge,
                lisPricePerTestTenge  = entity.lisPricePerTestTenge,
                status                = when {
                    logs == 0              -> ReconciliationStatus.NO_DATA
                    discrep > 0            -> ReconciliationStatus.DISCREPANCY
                    entity.pendingGraceCount > 0 -> ReconciliationStatus.PENDING_GRACE
                    else                   -> ReconciliationStatus.CLEAN
                },
            )
        }
    }

    override suspend fun getDrillDown(
        dateFrom: LocalDate,
        dateTo: LocalDate,
        catalogId: String?,
        analyzerId: String?,
        status: SampleReconciliationStatus?,
        page: Int,
        size: Int,
    ): DrillDownPage {
        val statusStr = status?.name
        val rows = sampleReconciliationRepository.findDrillDown(
            from       = dateFrom,
            to         = dateTo,
            catalogId  = catalogId,
            analyzerId = analyzerId,
            status     = statusStr,
            pageSize   = size,
            offset     = page * size,
        ).toList().map { it.toModel() }

        val total = sampleReconciliationRepository.countDrillDown(dateFrom, dateTo, catalogId, analyzerId, statusStr)
        return DrillDownPage(rows = rows, total = total, page = page, size = size)
    }

    override suspend fun promoteExpiredGraceSamples(batchSize: Int): Int {
        val now = LocalDateTime.now()
        val expired = sampleReconciliationRepository.findExpiredGraceSamples(now, batchSize).toList()
        if (expired.isEmpty()) return 0

        val promoted = expired.map { entity ->
            entity.copy(
                reconciliationStatus = SampleReconciliationStatus.DISCREPANCY.name,
                reason               = buildString {
                    append("Grace window expired at ")
                    append(entity.graceDeadlineAt ?: "?")
                    if (!entity.reason.isNullOrBlank()) {
                        append("; original: ").append(entity.reason)
                    }
                },
                estimatedWasteTenge  = entity.lisPricePerTestTenge ?: BigDecimal.ZERO,
                reconciledAt         = now,
            )
        }
        sampleReconciliationRepository.saveAll(promoted).toList()

        // Rebuild affected daily summaries so the aggregate reflects the new DISCREPANCY count.
        // Collect unique (date, analyzerId) tuples and rebuild each date (use wide range to keep it simple).
        val affectedDates = promoted.map { it.sampleDate }.toSet()
        if (affectedDates.isNotEmpty()) {
            val minDate = affectedDates.min()
            val maxDate = affectedDates.max()
            log.info("Rebuilding daily summaries for {} – {} after promoting {} expired-grace samples", minDate, maxDate, promoted.size)
            rebuildForDateRange(minDate, maxDate, null)
        }
        return promoted.size
    }

    // ─── Private helpers ───────────────────────────────────────────────────────

    private fun resolveSampleStatus(row: SampleRow): SampleReconciliationStatus = when {
        row.isWashTest                     -> SampleReconciliationStatus.WASH_TEST
        row.classification == SampleClassification.PROBABLE_RERUN -> SampleReconciliationStatus.RERUN
        row.isReconciled                   -> SampleReconciliationStatus.LEGITIMATE
        row.isInGraceWindow                -> SampleReconciliationStatus.PENDING_GRACE
        else                               -> SampleReconciliationStatus.DISCREPANCY
    }

    private suspend fun loadRows(dateFrom: LocalDate, dateTo: LocalDate, analyzerId: String?): List<ReconciliationDailySummary> =
        if (analyzerId != null) {
            reconciliationRepository
                .findAllByAnalyzerIdAndSummaryDateBetweenOrderBySummaryDateAscServiceNameCanonicalAsc(analyzerId, dateFrom, dateTo)
        } else {
            reconciliationRepository
                .findAllBySummaryDateBetweenOrderBySummaryDateAscServiceNameCanonicalAsc(dateFrom, dateTo)
        }.toList().map { it.toModel() }

    private suspend fun deleteExistingRows(dateFrom: LocalDate, dateTo: LocalDate, analyzerId: String?) {
        if (analyzerId != null) {
            reconciliationRepository.deleteAllByAnalyzerIdAndSummaryDateBetween(analyzerId, dateFrom, dateTo)
        } else {
            reconciliationRepository.deleteAllBySummaryDateBetween(dateFrom, dateTo)
        }
    }

    // ─── Helper value class for processing ────────────────────────────────────

    private data class SampleRow(
        val classification: SampleClassification,
        val sampleTimestamp: LocalDateTime,
        val graceHours: Int,
        val now: LocalDateTime,
    ) {
        val isWashTest: Boolean get() =
            classification == SampleClassification.WASH_TEST

        val isReconciled: Boolean get() =
            classification == SampleClassification.LEGITIMATE ||
            classification == SampleClassification.PROBABLE_RERUN

        val isInGraceWindow: Boolean get() {
            if (graceHours <= 0) return false
            val graceDeadline = sampleTimestamp.plusHours(graceHours.toLong())
            return now.isBefore(graceDeadline)
        }
    }

    companion object {
        private const val UNKNOWN_CATALOG_KEY = "__unknown__"
    }
}
