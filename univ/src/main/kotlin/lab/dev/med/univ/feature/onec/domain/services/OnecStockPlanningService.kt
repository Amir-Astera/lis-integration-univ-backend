package lab.dev.med.univ.feature.onec.domain.services

import com.fasterxml.jackson.databind.ObjectMapper
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.reactor.awaitSingleOrNull
import lab.dev.med.univ.feature.onec.data.entity.OnecLimsItemMappingEntity
import lab.dev.med.univ.feature.onec.data.repository.OnecLimsItemMappingRepository
import lab.dev.med.univ.feature.onec.data.repository.OnecReadonlySnapshotRepository
import lab.dev.med.univ.feature.onec.presentation.dto.OnecInventoryDto
import lab.dev.med.univ.feature.onec.presentation.dto.OnecLimsItemMappingDto
import lab.dev.med.univ.feature.onec.presentation.dto.OnecReadonlySnapshotDto
import lab.dev.med.univ.feature.onec.presentation.dto.OnecStockCoverageDto
import lab.dev.med.univ.feature.onec.presentation.dto.OnecStockCoverageRowDto
import lab.dev.med.univ.feature.onec.presentation.dto.UpsertOnecLimsItemMappingRequestDto
import lab.dev.med.univ.feature.reagents.data.entity.toModel
import lab.dev.med.univ.feature.reagents.data.repository.DamumedReportReagentConsumptionRepository
import org.springframework.stereotype.Service
import org.springframework.r2dbc.core.DatabaseClient
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit
import java.util.UUID

@Service
class OnecStockPlanningService(
    private val snapshotRepository: OnecReadonlySnapshotRepository,
    private val mappingRepository: OnecLimsItemMappingRepository,
    private val damumedConsumptionRepository: DamumedReportReagentConsumptionRepository,
    private val objectMapper: ObjectMapper,
    private val databaseClient: DatabaseClient,
) {
    suspend fun listMappings(): List<OnecLimsItemMappingDto> {
        val snapshot = snapshotRepository.findFirstByOrderByImportedAtDesc() ?: return emptyList()
        return mappingRepository.findAllBySnapshotIdOrderByOnecItemNameAsc(snapshot.id)
            .toList()
            .map { entity -> entity.toMappingDto() }
    }

    suspend fun upsertMapping(
        id: String,
        request: UpsertOnecLimsItemMappingRequestDto,
        actor: String?,
    ): OnecLimsItemMappingDto {
        require(request.targetKind in TARGET_KINDS) { "Недопустимый тип целевой позиции." }
        require(request.mappingStatus in MAPPING_STATUSES) { "Недопустимый статус сопоставления." }
        require(request.conversionFactor > BigDecimal.ZERO) { "Коэффициент пересчёта должен быть больше нуля." }
        require(snapshotRepository.findById(request.snapshotId) != null) { "Snapshot 1С не найден." }

        val existing = mappingRepository.findById(id)
        val now = LocalDateTime.now()
        val saved = mappingRepository.save(
            OnecLimsItemMappingEntity(
                id = id,
                snapshotId = request.snapshotId,
                onecItemRef = request.onecItemRef?.trim()?.takeIf(String::isNotBlank),
                onecItemCode = request.onecItemCode?.trim()?.takeIf(String::isNotBlank),
                onecItemName = request.onecItemName.trim(),
                targetKind = request.targetKind,
                targetName = request.targetName.trim(),
                targetUnit = request.targetUnit.trim(),
                conversionFactor = request.conversionFactor,
                mappingStatus = request.mappingStatus,
                notes = request.notes?.trim()?.takeIf(String::isNotBlank),
                createdAt = existing?.createdAt ?: now,
                createdBy = existing?.createdBy ?: actor,
                updatedAt = now,
                updatedBy = actor,
                version = existing?.version,
            ),
        )
        return saved.toMappingDto()
    }

    suspend fun createMapping(
        request: UpsertOnecLimsItemMappingRequestDto,
        actor: String?,
    ): OnecLimsItemMappingDto = upsertMapping(UUID.randomUUID().toString(), request, actor)

    suspend fun stockCoverage(
        planningDays: Int,
        damumedUploadId: String?,
    ): OnecStockCoverageDto {
        require(planningDays in 1..365) { "Период планирования должен быть от 1 до 365 дней." }

        val snapshotEntity = snapshotRepository.findFirstByOrderByImportedAtDesc()
            ?: throw IllegalStateException("Read-only snapshot 1С ещё не загружен.")
        val snapshot = objectMapper.readValue(snapshotEntity.payloadJson, OnecReadonlySnapshotDto::class.java)
        val mappings = mappingRepository.findAllBySnapshotIdOrderByOnecItemNameAsc(snapshotEntity.id).toList()
        val confirmedByOnecKey = mappings
            .filter { it.mappingStatus == "CONFIRMED" }
            .associateBy { mappingKey(it.onecItemCode, it.onecItemName) }

        val observedDamumedByTarget = damumedUploadId?.let { uploadId ->
            damumedConsumptionRepository.findAllByUploadIdOrderByCalculatedAtDesc(uploadId)
                .toList()
                .flatMap { it.toModel().consumptionEntries }
                .groupBy { normalize(it.reagentName) }
                .mapValues { (_, entries) ->
                    entries.fold(BigDecimal.ZERO) { total, entry -> total + entry.quantity }
                }
        }.orEmpty()
        val damumedPeriod = if (damumedUploadId == null) null else loadCompletedStudiesPeriod(damumedUploadId)

        val mappedTargetNames = confirmedByOnecKey.values.map { normalize(it.targetName) }.toSet()
        val unmappedDamumedReagents = observedDamumedByTarget.keys
            .filterNot(mappedTargetNames::contains)
            .sorted()

        var unmappedInventoryLines = 0
        val matchedInventory = snapshot.inventory.mapNotNull { inventory ->
            val mapping = confirmedByOnecKey[mappingKey(inventory.nomenclatureCode, inventory.nomenclatureName)]
            if (mapping == null) {
                unmappedInventoryLines += 1
                return@mapNotNull null
            }
            MatchedInventoryLine(inventory, mapping)
        }

        val rows = matchedInventory
            .groupBy { it.mapping.id }
            .values
            .map { lines ->
                val mapping = lines.first().mapping
                val observed = observedDamumedByTarget[normalize(mapping.targetName)]
                    ?.multiply(mapping.conversionFactor)
                val averageDaily = DamumedCoverageProjection.averageDaily(observed, damumedPeriod?.calendarDays)
                val projected = DamumedCoverageProjection.projectedQuantity(averageDaily, planningDays)
                val quantityOnHand = lines
                    .mapNotNull { it.inventory.quantity }
                    .takeIf { it.size == lines.size }
                    ?.fold(BigDecimal.ZERO, BigDecimal::add)
                val inventoryValue = lines
                    .mapNotNull { it.inventory.cost }
                    .takeIf { it.size == lines.size }
                    ?.fold(BigDecimal.ZERO, BigDecimal::add)
                val coverageDays = DamumedCoverageProjection.coverageDays(quantityOnHand, averageDaily)
                val warehouses = lines.mapNotNull { it.inventory.warehouse?.trim()?.takeIf(String::isNotBlank) }
                    .distinct()
                    .sorted()
                val lots = lines.mapNotNull { it.inventory.lotNumber?.trim()?.takeIf(String::isNotBlank) }
                    .distinct()
                    .sorted()
                OnecStockCoverageRowDto(
                    onecItemRef = mapping.onecItemRef,
                    onecItemCode = mapping.onecItemCode,
                    onecItemName = mapping.onecItemName,
                    warehouse = when (warehouses.size) {
                        0 -> null
                        1 -> warehouses.single()
                        else -> "${warehouses.size} складов"
                    },
                    lotNumber = when (lots.size) {
                        0 -> null
                        1 -> lots.single()
                        else -> "${lots.size} серий"
                    },
                    quantityOnHand = quantityOnHand,
                    inventoryValue = inventoryValue,
                    inventoryLineCount = lines.size,
                    targetKind = mapping.targetKind,
                    targetName = mapping.targetName,
                    targetUnit = mapping.targetUnit,
                    mappingStatus = mapping.mappingStatus,
                    observedDamumedQuantity = observed,
                    expectedDamumedQuantity = projected,
                    averageDailyExpectedQuantity = averageDaily,
                    estimatedCoverageDays = coverageDays,
                    suggestedPurchaseQuantity = null,
                    confidence = when {
                        observed == null -> "NO_DAMUMED_CALCULATION"
                        damumedPeriod == null -> "DAMUMED_PERIOD_UNAVAILABLE"
                        else -> "CONFIRMED_MAPPING"
                    },
                )
            }

        return OnecStockCoverageDto(
            snapshotId = snapshotEntity.id,
            sourceActualityAt = snapshot.snapshotAt,
            planningDays = planningDays,
            matchedDamumedUploadId = damumedUploadId,
            damumedPeriodStart = damumedPeriod?.start,
            damumedPeriodEnd = damumedPeriod?.end,
            damumedObservationDays = damumedPeriod?.calendarDays,
            rows = rows,
            unmappedInventoryLines = unmappedInventoryLines,
            unmappedDamumedReagents = unmappedDamumedReagents,
        )
    }

    private suspend fun loadCompletedStudiesPeriod(uploadId: String): DamumedPeriod? {
        val sql = """
            SELECT
                MIN(TO_DATE(d.raw_value, 'DD.MM.YYYY')) AS period_start,
                MAX(TO_DATE(d.raw_value, 'DD.MM.YYYY')) AS period_end
            FROM damumed_report_normalized_facts f
            INNER JOIN damumed_report_normalized_fact_dimensions d
                ON d.fact_id = f.id
               AND d.axis_key = 'completed_at'
            WHERE f.upload_id = :uploadId
              AND d.raw_value ~ '^\d{2}\.\d{2}\.\d{4}$'
        """.trimIndent()
        return databaseClient.sql(sql)
            .bind("uploadId", uploadId)
            .map { row, _ ->
                val start = row.get("period_start", LocalDate::class.java)
                val end = row.get("period_end", LocalDate::class.java)
                if (start != null && end != null) DamumedPeriod(start, end) else null
            }
            .one()
            .awaitSingleOrNull()
    }

    private data class MatchedInventoryLine(
        val inventory: OnecInventoryDto,
        val mapping: OnecLimsItemMappingEntity,
    )

    private data class DamumedPeriod(
        val start: LocalDate,
        val end: LocalDate,
    ) {
        val calendarDays: Int = ChronoUnit.DAYS.between(start, end).toInt() + 1
    }

    private fun OnecLimsItemMappingEntity.toMappingDto() = OnecLimsItemMappingDto(
        id = id,
        snapshotId = snapshotId,
        onecItemRef = onecItemRef,
        onecItemCode = onecItemCode,
        onecItemName = onecItemName,
        targetKind = targetKind,
        targetName = targetName,
        targetUnit = targetUnit,
        conversionFactor = conversionFactor,
        mappingStatus = mappingStatus,
        notes = notes,
        updatedAt = updatedAt,
    )

    private fun mappingKey(code: String?, name: String): String =
        code?.trim()?.takeIf(String::isNotBlank)?.let { "code:$it" } ?: "name:${normalize(name)}"

    private fun normalize(value: String): String = value
        .lowercase()
        .replace(Regex("\\s+"), " ")
        .trim()

    private companion object {
        val TARGET_KINDS = setOf("REAGENT", "CONSUMABLE")
        val MAPPING_STATUSES = setOf("SUGGESTED", "CONFIRMED", "DISABLED")
    }
}
