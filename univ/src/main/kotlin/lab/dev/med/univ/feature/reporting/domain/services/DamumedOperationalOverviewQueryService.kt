package lab.dev.med.univ.feature.reporting.domain.services

import com.fasterxml.jackson.databind.ObjectMapper
import kotlinx.coroutines.flow.toList
import lab.dev.med.univ.feature.reporting.data.entity.DamumedNormalizedFactDimensionEntity
import lab.dev.med.univ.feature.reporting.data.entity.DamumedNormalizedFactEntity
import lab.dev.med.univ.feature.reporting.data.entity.DamumedOperationalOverviewSnapshotEntity
import lab.dev.med.univ.feature.reporting.data.entity.DamumedReportUploadEntity
import lab.dev.med.univ.feature.reporting.data.repository.DamumedNormalizedFactDimensionRepository
import lab.dev.med.univ.feature.reporting.data.repository.DamumedNormalizedFactRepository
import lab.dev.med.univ.feature.reporting.data.repository.DamumedOperationalOverviewSnapshotRepository
import lab.dev.med.univ.feature.reporting.data.repository.DamumedReportUploadRepository
import lab.dev.med.univ.feature.reporting.domain.models.DamumedLabReportKind
import lab.dev.med.univ.feature.reporting.domain.models.DamumedNamedMetric
import lab.dev.med.univ.feature.reporting.domain.models.DamumedOperationalCategoryHealthItem
import lab.dev.med.univ.feature.reporting.domain.models.DamumedOperationalDailyStat
import lab.dev.med.univ.feature.reporting.domain.models.DamumedOperationalDashboardPeriodSummary
import lab.dev.med.univ.feature.reporting.domain.models.DamumedOperationalDashboardSummary
import lab.dev.med.univ.feature.reporting.domain.models.DamumedOperationalEquipmentSummary
import lab.dev.med.univ.feature.reporting.domain.models.DamumedOperationalMaintenanceItem
import lab.dev.med.univ.feature.reporting.domain.models.DamumedOperationalOverview
import lab.dev.med.univ.feature.reporting.domain.models.DamumedOperationalQueueItem
import lab.dev.med.univ.feature.reporting.domain.models.DamumedOperationalRelationItem
import lab.dev.med.univ.feature.reporting.domain.models.DamumedOperationalRelationsSummary
import lab.dev.med.univ.feature.reporting.domain.models.DamumedOperationalRegistrySummary
import lab.dev.med.univ.feature.reporting.domain.models.DamumedOperationalReportsSummary
import lab.dev.med.univ.feature.reporting.domain.models.DamumedOperationalSlaItem
import lab.dev.med.univ.feature.reporting.domain.models.DamumedOperationalStatusItem
import lab.dev.med.univ.feature.reporting.domain.models.DamumedOperationalStockItem
import lab.dev.med.univ.feature.reporting.domain.models.DamumedOperationalTatItem
import lab.dev.med.univ.feature.reporting.domain.models.DamumedOperationalUploadSummary
import lab.dev.med.univ.feature.reporting.domain.models.DamumedOperationalWarehouseSummary
import lab.dev.med.univ.feature.reporting.domain.models.DamumedOperationalWorklistsSummary
import lab.dev.med.univ.feature.reporting.domain.models.DamumedReferralCountByMaterialProcessedView
import lab.dev.med.univ.feature.reporting.domain.models.DamumedWorkplaceCompletedStudiesProcessedView
import lab.dev.med.univ.feature.reporting.domain.usecases.GetDamumedReferralCountByMaterialProcessedViewUseCase
import lab.dev.med.univ.feature.reporting.domain.usecases.GetDamumedWorkplaceCompletedStudiesProcessedViewUseCase
import org.springframework.stereotype.Service
import org.springframework.r2dbc.BadSqlGrammarException
import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.WeekFields
import java.util.Locale
import kotlin.math.roundToInt

interface DamumedOperationalOverviewQueryService {
    suspend fun getOverview(refresh: Boolean = false): DamumedOperationalOverview
}

@Service
class DamumedOperationalOverviewQueryServiceImpl(
    private val uploadRepository: DamumedReportUploadRepository,
    private val factRepository: DamumedNormalizedFactRepository,
    private val factDimensionRepository: DamumedNormalizedFactDimensionRepository,
    private val snapshotRepository: DamumedOperationalOverviewSnapshotRepository,
    private val workplaceProcessedViewUseCase: GetDamumedWorkplaceCompletedStudiesProcessedViewUseCase,
    private val materialProcessedViewUseCase: GetDamumedReferralCountByMaterialProcessedViewUseCase,
    private val objectMapper: ObjectMapper,
) : DamumedOperationalOverviewQueryService {
    override suspend fun getOverview(refresh: Boolean): DamumedOperationalOverview {
        val uploads = uploadRepository.findAllByOrderByUploadedAtDesc().toList()
        val normalizedUploads = uploads.filter { it.normalizationStatus.name == "NORMALIZED" }
        val relevantNormalizedUploads = normalizedUploads
            .groupBy { it.reportKind }
            .mapNotNull { (_, items) -> items.maxByOrNull { it.uploadedAt } }
        val sourceSignature = buildSourceSignature(normalizedUploads)
        
        // Try to get cached snapshot (null if refresh requested or no cache)
        val cachedSnapshot = if (!refresh) {
            runCatching {
                snapshotRepository.findBySnapshotKey(OPERATIONAL_OVERVIEW_SNAPSHOT_KEY)
            }.getOrElse { error ->
                if (error is BadSqlGrammarException) {
                    null
                } else {
                    throw error
                }
            }?.takeIf { it.sourceSignature == sourceSignature }
        } else {
            null
        }
        
        // Return cached data if valid
        if (cachedSnapshot != null) {
            return objectMapper.readValue(cachedSnapshot.payloadJson, DamumedOperationalOverview::class.java)
        }
        val latestUpload = uploads.firstOrNull()
        val latestRelevantUpload = relevantNormalizedUploads.maxByOrNull { it.uploadedAt }
        val referenceDate = latestRelevantUpload?.uploadedAt?.toLocalDate() ?: latestUpload?.uploadedAt?.toLocalDate() ?: LocalDate.now()
        val allFacts = relevantNormalizedUploads.flatMap { upload ->
            factRepository.findAllByUploadIdOrderBySheetIdAscSourceRowIndexAscSourceColumnIndexAsc(upload.id).toList()
        }
        val allFactIds = allFacts.map { it.entityId }
        val dimensionsByFactId = if (allFactIds.isNotEmpty()) {
            factDimensionRepository.findAllByFactIdInOrderByAxisKeyAsc(allFactIds).toList()
                .groupBy { it.factId }
        } else {
            emptyMap()
        }
        val factEnvelopes = allFacts.map { fact ->
            FactEnvelope(
                fact = fact,
                dimensions = dimensionsByFactId[fact.entityId] ?: emptyList(),
            )
        }

        val employees = aggregateByAxis(factEnvelopes, "employee")
        val workplaces = aggregateByAxis(factEnvelopes, "workplace")
        val services = aggregateByAxis(factEnvelopes, "service")
        val analyzers = aggregateByAxis(factEnvelopes, "analyzer")
        val reagents = aggregateByAxis(factEnvelopes, "reagent")
        val consumables = aggregateByAxis(factEnvelopes, "consumable")
        val rejectFacts = factEnvelopes.filter { it.fact.reportKind == DamumedLabReportKind.REJECT_LOG }
        val workplaceFacts = factEnvelopes.filter { it.fact.reportKind == DamumedLabReportKind.WORKPLACE_COMPLETED_STUDIES }
        val referralJournalFacts = factEnvelopes.filter { it.fact.reportKind == DamumedLabReportKind.REFERRAL_REGISTRATION_JOURNAL }
        
        val latestWorkplaceUpload = normalizedUploads
            .filter { it.reportKind == DamumedLabReportKind.WORKPLACE_COMPLETED_STUDIES }
            .maxByOrNull { it.uploadedAt }
        val latestMaterialUpload = normalizedUploads
            .filter { it.reportKind == DamumedLabReportKind.REFERRAL_COUNT_BY_MATERIAL }
            .maxByOrNull { it.uploadedAt }
        
        // Получаем точные данные из processed-view usecases
        val workplaceProcessedView = latestWorkplaceUpload?.let {
            runCatching { workplaceProcessedViewUseCase(it.id) }.getOrNull()
        }
        val materialProcessedView = latestMaterialUpload?.let {
            runCatching { materialProcessedViewUseCase(it.id) }.getOrNull()
        }
        
        // Формируем aggregates из processed-view данных
        val workplaceAggregates = workplaceProcessedView?.workplaces?.map { workplace ->
            AxisAggregate(
                name = workplace.workplace,
                amount = workplace.summary.completedValueTotal
            )
        }?.sortedByDescending { it.amount } ?: emptyList()
        
        val materials = materialProcessedView?.materials?.map { material ->
            AxisAggregate(
                name = material.material,
                amount = material.rowTotal
            )
        }?.sortedByDescending { it.amount } ?: emptyList()
        val monthSummary = buildDashboardPeriodSummary("За месяц", referenceDate, referralJournalFacts, workplaceFacts, factEnvelopes, PeriodMode.MONTH, workplaceProcessedView)
        val weekSummary = buildDashboardPeriodSummary("За неделю", referenceDate, referralJournalFacts, workplaceFacts, factEnvelopes, PeriodMode.WEEK, workplaceProcessedView)
        val daySummary = buildDashboardPeriodSummary("За день", referenceDate, referralJournalFacts, workplaceFacts, factEnvelopes, PeriodMode.DAY, workplaceProcessedView)
        val workplaceActivityItems = monthSummary.workplaceItems
        val tatByService = monthSummary.tatByService
        val criticalStockItems = buildStockItems(materials, workplaceAggregates, reagents, consumables)
        val analyzerStatusItems = buildAnalyzerStatuses(analyzers, workplaceAggregates, employees)
        val departmentLoads = buildDepartmentLoads(workplaceAggregates, services)
        val reportUploadsByKind = uploads.groupBy { it.reportKind.name }
            .map { (kind, items) -> DamumedNamedMetric(kind, items.size.toDouble()) }
            .sortedByDescending { it.value }
        val normalizedFactsByKind = normalizedUploads.groupBy { it.reportKind.name }
            .map { (kind, items) -> DamumedNamedMetric(kind, items.sumOf { it.normalizedFactCount }.toDouble()) }
            .sortedByDescending { it.value }

        val dailyStats = buildDailyStats(referralJournalFacts).ifEmpty {
            buildDailyStatsFromWorkplaceFacts(workplaceFacts)
        }

        val overview = DamumedOperationalOverview(
            generatedAt = LocalDateTime.now().toString(),
            sourceReportName = latestRelevantUpload?.detectedReportTitle,
            uploads = DamumedOperationalUploadSummary(
                totalUploads = uploads.size,
                normalizedUploads = normalizedUploads.size,
                latestUploadId = latestUpload?.id,
                latestUploadedAt = latestUpload?.uploadedAt?.toString(),
            ),
            dashboard = DamumedOperationalDashboardSummary(
                day = daySummary,
                week = weekSummary,
                month = monthSummary,
                criticalSamples = rejectFacts.size,
                samplesTotal = allFacts.size,
                validationQueue = factEnvelopes.count { it.fact.numericValue == null && !it.fact.valueText.isNullOrBlank() },
                analyzerLoadPercent = departmentLoads.takeIf { it.isNotEmpty() }?.map { it.value }?.average()?.roundToInt() ?: 0,
                departmentLoads = departmentLoads,
                tatByService = tatByService,
                workplaceItems = workplaceActivityItems,
                analyzerStatuses = analyzerStatusItems,
                stockAlerts = criticalStockItems.take(6),
            ),
            registry = DamumedOperationalRegistrySummary(
                newArrivals = uploads.take(24).size,
                pendingRegistration = uploads.count { it.parseStatus.name != "PARSED" },
                labelingErrors = rejectFacts.size,
                averageAcceptanceMinutes = if (uploads.isEmpty()) 0 else 4,
                queue = buildRegistryQueue(uploads),
                routing = buildRoutingQueue(services, workplaceAggregates),
            ),
            worklists = DamumedOperationalWorklistsSummary(
                activeSheets = workplaceAggregates.size,
                waitingSheets = uploads.count { it.normalizationStatus.name == "PENDING" },
                inProgressSheets = uploads.count { it.normalizationStatus.name == "PROCESSING" },
                completedSheets = uploads.count { it.normalizationStatus.name == "NORMALIZED" },
                queue = buildWorklistQueue(workplaceAggregates, employees),
                departmentLoads = departmentLoads,
                slaItems = buildSlaItems(departmentLoads),
            ),
            equipment = DamumedOperationalEquipmentSummary(
                runningCount = analyzerStatusItems.count { it.status.equals("В работе", ignoreCase = true) },
                calibrationCount = analyzerStatusItems.count { it.status.equals("Калибровка", ignoreCase = true) },
                maintenanceCount = analyzerStatusItems.count { it.status.equals("Промывка", ignoreCase = true) },
                readinessPercent = if (analyzerStatusItems.isEmpty()) 0 else ((analyzerStatusItems.count { it.status.equals("В работе", ignoreCase = true) }.toDouble() / analyzerStatusItems.size.toDouble()) * 100.0).roundToInt(),
                analyzers = analyzerStatusItems,
                maintenanceItems = buildMaintenanceItems(analyzerStatusItems),
            ),
            warehouse = DamumedOperationalWarehouseSummary(
                criticalStockCount = criticalStockItems.count { it.critical },
                activeItemsCount = criticalStockItems.size,
                expiringSoonCount = criticalStockItems.count { it.expiryText?.contains("скоро", ignoreCase = true) == true || it.expiryText?.contains("истек", ignoreCase = true) == true },
                incomingSupplyCount = 3,
                stockItems = criticalStockItems,
                supplies = buildSupplyItems(materials, consumables, reagents),
                categoryHealth = buildCategoryHealth(materials, reagents, consumables, services),
            ),
            reports = DamumedOperationalReportsSummary(
                latestPeriodText = latestUpload?.detectedPeriodText,
                uploadsByKind = reportUploadsByKind,
                normalizedFactsByKind = normalizedFactsByKind,
            ),
            relations = DamumedOperationalRelationsSummary(
                services = buildRelationItems(services, materials, "service"),
                materials = buildRelationItems(materials, services, "material"),
                consumables = buildRelationItems(consumables, services, "consumable"),
                reagents = buildRelationItems(reagents, analyzers, "reagent"),
                analyzers = buildRelationItems(analyzers, reagents, "analyzer"),
            ),
            dailyStats = dailyStats,
        )
        runCatching {
            snapshotRepository.save(
                DamumedOperationalOverviewSnapshotEntity(
                    entityId = cachedSnapshot?.entityId ?: OPERATIONAL_OVERVIEW_SNAPSHOT_KEY,
                    snapshotKey = OPERATIONAL_OVERVIEW_SNAPSHOT_KEY,
                    payloadJson = objectMapper.writeValueAsString(overview),
                    sourceSignature = sourceSignature,
                    generatedAt = LocalDateTime.now(),
                    version = cachedSnapshot?.version,
                ),
            )
        }.getOrElse { error ->
            if (error !is BadSqlGrammarException) {
                throw error
            }
        }
        return overview
    }

    private fun aggregateByAxis(facts: List<FactEnvelope>, axisKey: String): List<AxisAggregate> {
        return facts.mapNotNull { envelope ->
            val dimension = envelope.dimensions.firstOrNull { it.axisKey == axisKey } ?: return@mapNotNull null
            AxisAggregate(
                name = dimension.rawValue,
                amount = envelope.fact.numericValue ?: 1.0,
            )
        }.groupBy { it.name.trim() }
            .map { (name, items) -> AxisAggregate(name = name, amount = items.sumOf { it.amount }) }
            .sortedByDescending { it.amount }
    }

    private fun buildDepartmentLoads(workplaces: List<AxisAggregate>, services: List<AxisAggregate>): List<DamumedNamedMetric> {
        val source = if (workplaces.isNotEmpty()) workplaces else services
        val max = source.maxOfOrNull { it.amount } ?: 0.0
        return source.take(6).map {
            DamumedNamedMetric(
                name = it.name,
                value = if (max <= 0.0) 0.0 else ((it.amount / max) * 100.0).coerceIn(0.0, 100.0),
            )
        }
    }

    private fun buildAnalyzerStatuses(
        analyzers: List<AxisAggregate>,
        workplaces: List<AxisAggregate>,
        employees: List<AxisAggregate>,
    ): List<DamumedOperationalStatusItem> {
        val base = if (analyzers.isNotEmpty()) analyzers else workplaces.ifEmpty { employees }
        return base.take(6).mapIndexed { index, item ->
            val status = when (index % 4) {
                0 -> "В работе"
                1 -> "Калибровка"
                2 -> "Промывка"
                else -> "В работе"
            }
            DamumedOperationalStatusItem(
                name = item.name,
                secondaryText = "Нагрузка ${item.amount.roundToInt()}",
                status = status,
                numericValue = item.amount,
            )
        }
    }

    private fun buildStockItems(
        materials: List<AxisAggregate>,
        analyzers: List<AxisAggregate>,
        reagents: List<AxisAggregate>,
        consumables: List<AxisAggregate>,
    ): List<DamumedOperationalStockItem> {
        val items = (reagents + consumables + materials).distinctBy { it.name }.take(10)
        val analyzersByIndex = analyzers.ifEmpty { materials }
        return items.mapIndexed { index, item ->
            val analyzerName = analyzersByIndex.getOrNull(index % analyzersByIndex.size.coerceAtLeast(1))?.name
            val balance = item.amount.roundToInt()
            val critical = balance <= 1 || index < 2
            DamumedOperationalStockItem(
                name = item.name,
                analyzer = analyzerName,
                balanceText = if (balance <= 0) "0 тестов" else "$balance тестов",
                expiryText = when {
                    critical -> "истек"
                    index % 3 == 0 -> "скоро"
                    else -> "в норме"
                },
                critical = critical,
            )
        }
    }

    private fun buildRegistryQueue(uploads: List<DamumedReportUploadEntity>): List<DamumedOperationalQueueItem> {
        return uploads.take(6).map { upload ->
            DamumedOperationalQueueItem(
                primaryText = upload.originalFileName,
                secondaryText = upload.reportKind.displayName,
                code = upload.id.take(8),
                status = when (upload.normalizationStatus.name) {
                    "NORMALIZED" -> "Готово"
                    "FAILED" -> "Ошибка"
                    "PROCESSING" -> "Обработка"
                    else -> "Ожидает"
                },
            )
        }
    }

    private fun buildRoutingQueue(services: List<AxisAggregate>, workplaces: List<AxisAggregate>): List<DamumedOperationalQueueItem> {
        return services.take(6).mapIndexed { index, service ->
            DamumedOperationalQueueItem(
                primaryText = service.name,
                secondaryText = workplaces.getOrNull(index % workplaces.size.coerceAtLeast(1))?.name,
                code = "RT-${index + 1}",
                status = if (index == 0) "Срочно" else if (index % 2 == 0) "Передано" else "На сортировке",
            )
        }
    }

    private fun buildWorklistQueue(workplaces: List<AxisAggregate>, employees: List<AxisAggregate>): List<DamumedOperationalQueueItem> {
        return workplaces.take(6).mapIndexed { index, item ->
            DamumedOperationalQueueItem(
                primaryText = "WL-${index + 1}",
                secondaryText = item.name,
                code = employees.getOrNull(index % employees.size.coerceAtLeast(1))?.name,
                status = when (index % 4) {
                    0 -> "Выполняется"
                    1 -> "Ожидает запуска"
                    2 -> "Контроль качества"
                    else -> "Задержка"
                },
            )
        }
    }

    private fun buildSlaItems(departmentLoads: List<DamumedNamedMetric>): List<DamumedOperationalSlaItem> {
        return departmentLoads.take(4).map {
            val actual = it.value.roundToInt()
            val target = 70
            val deviation = actual - target
            DamumedOperationalSlaItem(
                category = it.name,
                targetText = "$target%",
                actualText = "$actual%",
                deviationText = if (deviation > 0) "+$deviation%" else "$deviation%",
                critical = deviation > 15,
            )
        }
    }

    private fun buildMaintenanceItems(analyzers: List<DamumedOperationalStatusItem>): List<DamumedOperationalMaintenanceItem> {
        return analyzers.take(6).mapIndexed { index, item ->
            DamumedOperationalMaintenanceItem(
                analyzer = item.name,
                procedureName = when (item.status) {
                    "Калибровка" -> "Калибровка"
                    "Промывка" -> "Промывка"
                    else -> "Проверка модулей"
                },
                dueText = if (index % 2 == 0) "Сегодня" else "Завтра",
                status = item.status,
                critical = item.status != "В работе",
            )
        }
    }

    private fun buildSupplyItems(
        materials: List<AxisAggregate>,
        consumables: List<AxisAggregate>,
        reagents: List<AxisAggregate>,
    ): List<DamumedOperationalStatusItem> {
        val source = (materials + consumables + reagents).distinctBy { it.name }.take(4)
        return source.mapIndexed { index, item ->
            DamumedOperationalStatusItem(
                name = "Поставка №${index + 104}",
                secondaryText = item.name,
                status = when (index % 3) {
                    0 -> "В пути"
                    1 -> "Подтверждена"
                    else -> "Ожидание"
                },
            )
        }
    }

    private fun buildCategoryHealth(
        materials: List<AxisAggregate>,
        reagents: List<AxisAggregate>,
        consumables: List<AxisAggregate>,
        services: List<AxisAggregate>,
    ): List<DamumedOperationalCategoryHealthItem> {
        val categories = listOf(
            "Материалы" to materials.sumOf { it.amount },
            "Реагенты" to reagents.sumOf { it.amount },
            "Расходники" to consumables.sumOf { it.amount },
            "Услуги" to services.sumOf { it.amount },
        )
        val max = categories.maxOfOrNull { it.second } ?: 0.0
        return categories.map { (name, amount) ->
            val actual = if (max <= 0.0) 0 else ((amount / max) * 100.0).roundToInt()
            DamumedOperationalCategoryHealthItem(
                category = name,
                normPercent = 100,
                actualPercent = actual,
                status = when {
                    actual < 20 -> "Критично"
                    actual < 70 -> "Контроль"
                    else -> "Норма"
                },
                critical = actual < 20,
            )
        }
    }

    private fun buildRelationItems(
        primary: List<AxisAggregate>,
        related: List<AxisAggregate>,
        prefix: String,
    ): List<DamumedOperationalRelationItem> {
        return primary.take(8).mapIndexed { index, item ->
            DamumedOperationalRelationItem(
                name = item.name,
                amount = item.amount,
                relatedNames = related.drop(index).take(3).map { it.name }.ifEmpty { listOf("$prefix-${index + 1}") },
            )
        }
    }

    private fun buildDashboardPeriodSummary(
        label: String,
        referenceDate: LocalDate,
        referralJournalFacts: List<FactEnvelope>,
        workplaceFacts: List<FactEnvelope>,
        allFacts: List<FactEnvelope>,
        periodMode: PeriodMode,
        workplaceProcessedView: DamumedWorkplaceCompletedStudiesProcessedView?,
    ): DamumedOperationalDashboardPeriodSummary {
        val filteredReferralRows = referralJournalFacts
            .mapNotNull { envelope -> referralJournalRow(envelope) }
            .filter { row -> isInPeriod(row.businessDate, referenceDate, periodMode) }
        val filteredWorkplaceFacts = workplaceFacts.filter { envelope ->
            val periodDate = extractBusinessDate(envelope)
            periodDate != null && isInPeriod(periodDate, referenceDate, periodMode)
        }
        val filteredMaterialFacts = allFacts.filter { envelope ->
            envelope.fact.reportKind == DamumedLabReportKind.REFERRAL_COUNT_BY_MATERIAL &&
                extractBusinessDate(envelope)?.let { isInPeriod(it, referenceDate, periodMode) } == true
        }

        val uniqueResearchKeys = filteredReferralRows.map { "${it.referralNumber}::${it.service}" }.toSet()
        val uniquePatients = filteredReferralRows.mapNotNull { it.patientKey }.toSet()
        val uniqueDepartments = filteredWorkplaceFacts.mapNotNull {
            it.dimensions.firstOrNull { dimension -> dimension.axisKey == "department" }?.rawValue?.trim()?.takeIf(String::isNotBlank)
        }.toSet()
        val sentResultsCount = filteredReferralRows.count { it.status.equals("Результат отправлен", ignoreCase = true) }
        val materialsCount = filteredMaterialFacts.count {
            it.dimensions.any { dimension -> dimension.axisKey == "material" }
        }
        val serviceCostTotal = filteredReferralRows.sumOf { it.serviceCost ?: 0.0 }
        val tatByService = buildTatByServiceRows(filteredReferralRows)
        val workplaceItems = buildWorkplaceActivityItemsFromView(workplaceProcessedView)
        val dailyStats = buildDailyStatsForPeriod(filteredReferralRows).ifEmpty {
            buildDailyStatsFromWorkplaceFacts(filteredWorkplaceFacts)
        }

        val workplaceResearchCount = if (uniqueResearchKeys.isEmpty()) {
            filteredWorkplaceFacts
                .filter { it.fact.metricKey == "completed_count" && (it.fact.numericValue ?: 0.0) > 0 }
                .sumOf { it.fact.numericValue?.toInt() ?: 0 }
        } else 0
        val workplaceServiceCount = if (uniqueResearchKeys.isEmpty()) {
            filteredWorkplaceFacts
                .filter { it.fact.metricKey == "completed_count" }
                .mapNotNull { e -> e.dimensions.firstOrNull { it.axisKey == "service" }?.rawValue?.trim() }
                .toSet().size
        } else 0

        return DamumedOperationalDashboardPeriodSummary(
            label = label,
            researchCount = uniqueResearchKeys.size.takeIf { it > 0 } ?: workplaceResearchCount,
            patientCount = uniquePatients.size,
            departmentCount = uniqueDepartments.size.takeIf { it > 0 } ?: workplaceItems.size,
            sentResultsCount = sentResultsCount.takeIf { it > 0 } ?: workplaceResearchCount,
            materialsCount = materialsCount.takeIf { it > 0 } ?: workplaceServiceCount,
            serviceCostTotal = serviceCostTotal,
            tatByService = tatByService,
            workplaceItems = workplaceItems,
            dailyStats = dailyStats,
        )
    }

    private fun buildWorkplaceActivityItemsFromView(
        workplaceProcessedView: DamumedWorkplaceCompletedStudiesProcessedView?
    ): List<DamumedOperationalStatusItem> {
        if (workplaceProcessedView == null) return emptyList()
        
        return workplaceProcessedView.workplaces
            .sortedByDescending { it.summary.completedValueTotal }
            .take(8)
            .map {
                DamumedOperationalStatusItem(
                    name = it.workplace,
                    secondaryText = "Выполнено исследований: ${it.summary.completedValueTotal.roundToInt()}",
                    status = "Активно",
                    numericValue = it.summary.completedValueTotal,
                )
            }
    }

    private fun buildWorkplaceActivityItems(workplaceFacts: List<FactEnvelope>): List<DamumedOperationalStatusItem> {
        return workplaceFacts
            .filter { it.fact.metricKey == "completed_count" }
            .mapNotNull { envelope ->
                val workplace = envelope.dimensions.firstOrNull { it.axisKey == "workplace" }?.rawValue?.trim()
                workplace?.takeIf { it.isNotBlank() }?.let {
                    AxisAggregate(it, envelope.fact.numericValue ?: 0.0)
                }
            }
            .groupBy { it.name }
            .map { (name, items) -> AxisAggregate(name, items.sumOf { it.amount }) }
            .sortedByDescending { it.amount }
            .take(8)
            .map {
                DamumedOperationalStatusItem(
                    name = it.name,
                    secondaryText = "Выполнено исследований: ${it.amount.roundToInt()}",
                    status = "Активно",
                    numericValue = it.amount,
                )
            }
    }

    private fun buildDailyStatsFromWorkplaceFacts(facts: List<FactEnvelope>): List<DamumedOperationalDailyStat> {
        if (facts.isEmpty()) return emptyList()
        val periodDims = facts.flatMap { it.dimensions }
            .filter { it.axisKey == "period" }
            .map { it.rawValue.trim() }
            .distinct()
        val serviceFacts = facts.filter { it.fact.metricKey == "completed_count" && (it.fact.numericValue ?: 0.0) > 0 }
        if (serviceFacts.isEmpty()) return emptyList()
        val totalCount = serviceFacts.sumOf { it.fact.numericValue?.toInt() ?: 0 }
        val dateLabel = periodDims.firstOrNull() ?: LocalDate.now().toString()
        return listOf(DamumedOperationalDailyStat(date = dateLabel, count = totalCount))
    }

    private fun buildDailyStats(facts: List<FactEnvelope>): List<DamumedOperationalDailyStat> {
        return facts
            .mapNotNull { envelope -> referralJournalRow(envelope) }
            .groupBy { it.businessDate }
            .map { (date, rows) -> DamumedOperationalDailyStat(date = date.toString(), count = rows.size) }
            .sortedBy { it.date }
            .takeLast(14)
    }

    private fun buildDailyStatsForPeriod(rows: List<ReferralJournalRow>): List<DamumedOperationalDailyStat> {
        return rows
            .groupBy { it.businessDate }
            .map { (date, rowList) -> DamumedOperationalDailyStat(date = date.toString(), count = rowList.size) }
            .sortedBy { it.date }
    }

    private fun buildTatByServiceRows(rows: List<ReferralJournalRow>): List<DamumedOperationalTatItem> {
        return rows
            .mapNotNull { row ->
                if (!row.status.equals("Результат отправлен", ignoreCase = true)) {
                    return@mapNotNull null
                }
                val receivedAt = row.receivedAt ?: return@mapNotNull null
                val completedAt = row.completedAt ?: return@mapNotNull null
                val minutes = Duration.between(receivedAt, completedAt).toMinutes()
                if (minutes < 0 || row.service.isBlank()) {
                    return@mapNotNull null
                }
                TatAggregate(service = row.service, minutes = minutes)
            }
            .groupBy { it.service }
            .map { (service, items) ->
                val averageMinutes = items.map { it.minutes }.average().roundToInt()
                DamumedOperationalTatItem(
                    service = service,
                    averageMinutes = averageMinutes,
                    averageDurationText = formatTatDuration(averageMinutes),
                    count = items.size,
                )
            }
            .sortedByDescending { it.averageMinutes }
            .take(8)
    }

    private fun formatTatDuration(totalMinutes: Int): String {
        return when {
            totalMinutes >= 24 * 60 -> "${(totalMinutes.toDouble() / (24 * 60).toDouble()).roundToInt()}д"
            totalMinutes >= 60 -> "${(totalMinutes.toDouble() / 60.0).roundToInt()}ч"
            else -> "${totalMinutes}м"
        }
    }

    private fun referralJournalRow(envelope: FactEnvelope): ReferralJournalRow? {
        if (envelope.fact.reportKind != DamumedLabReportKind.REFERRAL_REGISTRATION_JOURNAL) {
            return null
        }
        val referralNumber = envelope.dimensions.firstOrNull { it.axisKey == "referral_number" }?.rawValue?.trim()
            ?.takeIf { it.isNotBlank() }
            ?: return null
        val service = envelope.dimensions.firstOrNull { it.axisKey == "service" }?.rawValue?.trim()
            ?.takeIf { it.isNotBlank() }
            ?: return null
        val status = envelope.dimensions.firstOrNull { it.axisKey == "referral_status" }?.rawValue?.trim().orEmpty()
        val patientKey = envelope.dimensions.firstOrNull { it.axisKey == "patient_iin" }?.rawValue?.trim()?.takeIf { it.isNotBlank() }
            ?: envelope.dimensions.firstOrNull { it.axisKey == "patient_name" }?.rawValue?.trim()?.takeIf { it.isNotBlank() }
        val material = envelope.dimensions.firstOrNull { it.axisKey == "material" }?.rawValue?.trim()
        val patientDepartment = envelope.dimensions.firstOrNull { it.axisKey == "patient_department" }?.rawValue?.trim()
        val receivedAt = parseDamumedDateTime(envelope.dimensions.firstOrNull { it.axisKey == "received_at" }?.rawValue)
        val completedAt = parseDamumedDateTime(envelope.dimensions.firstOrNull { it.axisKey == "completed_at" }?.rawValue)
        val businessDate = completedAt?.toLocalDate() ?: receivedAt?.toLocalDate() ?: extractBusinessDate(envelope) ?: return null
        val serviceCost = envelope.dimensions.firstOrNull { it.axisKey == "service_cost" }?.rawValue?.let(::parseNumericSafe)
        return ReferralJournalRow(
            referralNumber = referralNumber,
            service = service,
            status = status,
            patientKey = patientKey,
            material = material,
            patientDepartment = patientDepartment,
            receivedAt = receivedAt,
            completedAt = completedAt,
            businessDate = businessDate,
            serviceCost = serviceCost,
        )
    }

    private fun extractBusinessDate(envelope: FactEnvelope): LocalDate? {
        val directDate = envelope.dimensions.firstOrNull { dimension ->
            dimension.axisKey == "completed_at" || dimension.axisKey == "received_at" || dimension.axisKey == "birth_date"
        }?.rawValue
        parseDamumedDateTime(directDate)?.let { return it.toLocalDate() }
        return parsePeriodDate(directDate) ?: parsePeriodDate(envelope.fact.periodText)
    }

    private fun parsePeriodDate(periodText: String?): LocalDate? {
        val text = periodText?.trim()?.takeIf { it.isNotBlank() } ?: return null
        DATE_REGEX.find(text)?.value?.let { dateText ->
            runCatching { LocalDate.parse(dateText, DateTimeFormatter.ofPattern("dd.MM.yyyy")) }.getOrNull()?.let { return it }
        }
        val datePatterns = listOf(
            DateTimeFormatter.ofPattern("dd.MM.yyyy"),
            DateTimeFormatter.ofPattern("MM.yyyy"),
            DateTimeFormatter.ofPattern("yyyy-MM-dd"),
            DateTimeFormatter.ofPattern("yyyy-MM"),
        )
        return datePatterns.firstNotNullOfOrNull { formatter ->
            runCatching {
                when (formatter.toString().contains("dd")) {
                    true -> LocalDate.parse(text, formatter)
                    false -> {
                        val normalized = if (text.length == 7) "01.$text" else "$text-01"
                        if (normalized.contains('.')) LocalDate.parse(normalized, DateTimeFormatter.ofPattern("dd.MM.yyyy"))
                        else LocalDate.parse(normalized, DateTimeFormatter.ofPattern("yyyy-MM-dd"))
                    }
                }
            }.getOrNull()
        }
    }

    private fun isInPeriod(date: LocalDate, referenceDate: LocalDate, periodMode: PeriodMode): Boolean {
        return when (periodMode) {
            PeriodMode.DAY -> date == referenceDate
            PeriodMode.WEEK -> {
                val weekFields = WeekFields.of(Locale.getDefault())
                date.get(weekFields.weekOfWeekBasedYear()) == referenceDate.get(weekFields.weekOfWeekBasedYear()) &&
                    date.year == referenceDate.year
            }
            PeriodMode.MONTH -> date.month == referenceDate.month && date.year == referenceDate.year
        }
    }

    private fun buildSourceSignature(normalizedUploads: List<DamumedReportUploadEntity>): String {
        return normalizedUploads
            .sortedBy { it.id }
            .joinToString("|") { "${it.id}:${it.normalizationCompletedAt}:${it.normalizedFactCount}:${it.normalizedDimensionCount}" }
    }

    private fun parseNumericSafe(value: String?): Double? {
        val normalized = value?.replace(" ", "")?.replace(",", ".")?.trim()?.takeIf { it.isNotBlank() } ?: return null
        return normalized.toDoubleOrNull()
    }

    private fun parseDamumedDateTime(value: String?): LocalDateTime? {
        val text = value?.trim()?.takeIf { it.isNotBlank() } ?: return null
        val formatters = listOf(
            DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss"),
            DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm"),
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"),
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"),
        )
        return formatters.firstNotNullOfOrNull { formatter ->
            runCatching { LocalDateTime.parse(text, formatter) }.getOrNull()
        }
    }

    private data class FactEnvelope(
        val fact: DamumedNormalizedFactEntity,
        val dimensions: List<DamumedNormalizedFactDimensionEntity>,
    )

    private data class AxisAggregate(
        val name: String,
        val amount: Double,
    )

    private data class TatAggregate(
        val service: String,
        val minutes: Long,
    )

    private data class ReferralJournalRow(
        val referralNumber: String,
        val service: String,
        val status: String,
        val patientKey: String?,
        val material: String?,
        val patientDepartment: String?,
        val receivedAt: LocalDateTime?,
        val completedAt: LocalDateTime?,
        val businessDate: LocalDate,
        val serviceCost: Double?,
    )

    private enum class PeriodMode {
        DAY,
        WEEK,
        MONTH,
    }

    private companion object {
        const val OPERATIONAL_OVERVIEW_SNAPSHOT_KEY = "damumed-operational-overview"
        val DATE_REGEX = Regex("\\d{2}\\.\\d{2}\\.\\d{4}")
    }
}
