package lab.dev.med.univ.feature.onec.domain.services

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import lab.dev.med.univ.feature.onec.data.entity.OnecReadonlySnapshotEntity
import lab.dev.med.univ.feature.onec.data.repository.OnecReadonlySnapshotRepository
import lab.dev.med.univ.feature.onec.presentation.dto.OnecReadonlySnapshotDto
import lab.dev.med.univ.feature.onec.presentation.dto.OnecReadonlySnapshotImportResponseDto
import lab.dev.med.univ.feature.onec.presentation.dto.OnecReadonlySnapshotCollectionPageDto
import lab.dev.med.univ.feature.onec.presentation.dto.OnecReadonlySnapshotSummaryDto
import lab.dev.med.univ.feature.onec.presentation.dto.OnecSnapshotSourceDto
import org.springframework.stereotype.Service
import java.security.MessageDigest
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.UUID

@Service
class OnecReadonlySnapshotService(
    private val repository: OnecReadonlySnapshotRepository,
    private val objectMapper: ObjectMapper,
) {
    suspend fun getLatest(): OnecReadonlySnapshotDto? {
        val entity = repository.findFirstByOrderByImportedAtDesc() ?: return null
        val snapshot = objectMapper.readValue(entity.payloadJson, OnecReadonlySnapshotDto::class.java)
        return snapshot.copy(
            status = "ready",
            snapshotAt = entity.snapshotAt ?: snapshot.snapshotAt,
            periodFrom = entity.periodFrom ?: snapshot.periodFrom,
            periodTo = entity.periodTo ?: snapshot.periodTo,
            source = snapshot.source ?: OnecSnapshotSourceDto(
                sourceName = entity.sourceName,
                sourceKind = entity.sourceKind,
                sourceChecksum = entity.sourceChecksum,
                exportedAt = entity.importedAt,
            ),
        )
    }

    suspend fun getLatestSummary(): OnecReadonlySnapshotSummaryDto {
        val entity = repository.findFirstByOrderByImportedAtDesc()
            ?: return OnecReadonlySnapshotSummaryDto(status = "disconnected")
        val root = objectMapper.readTree(entity.payloadJson)
        val source = root.path("source").takeIf { it.isObject }
            ?.let { objectMapper.treeToValue(it, OnecSnapshotSourceDto::class.java) }
            ?: OnecSnapshotSourceDto(
                sourceName = entity.sourceName,
                sourceKind = entity.sourceKind,
                sourceChecksum = entity.sourceChecksum,
                exportedAt = entity.importedAt,
            )
        return OnecReadonlySnapshotSummaryDto(
            status = "ready",
            snapshotAt = entity.snapshotAt ?: root.path("snapshotAt").asText().takeIf(String::isNotBlank)?.let(LocalDateTime::parse),
            periodFrom = entity.periodFrom ?: root.path("periodFrom").asText().takeIf(String::isNotBlank)?.let(LocalDate::parse),
            periodTo = entity.periodTo ?: root.path("periodTo").asText().takeIf(String::isNotBlank)?.let(LocalDate::parse),
            source = source,
            nomenclatureCount = root.path("nomenclature").size(),
            inventoryCount = root.path("inventory").size(),
            receiptCount = root.path("receipts").size(),
            writeOffCount = root.path("writeOffs").size(),
            counterpartyCount = root.path("counterparties").size(),
        )
    }

    suspend fun getCollection(
        collection: String,
        page: Int,
        size: Int,
        query: String?,
    ): OnecReadonlySnapshotCollectionPageDto {
        require(collection in COLLECTIONS) { "Unsupported 1C snapshot collection." }
        require(page >= 0) { "Page cannot be negative." }
        require(size in 1..500) { "Page size must be between 1 and 500." }
        val entity = repository.findFirstByOrderByImportedAtDesc()
            ?: return OnecReadonlySnapshotCollectionPageDto(collection, page, size, 0, emptyList())
        val items = objectMapper.readTree(entity.payloadJson)
            .path(collection)
            .takeIf { it.isArray }
            ?.toList()
            .orEmpty()
        val normalizedQuery = query?.trim()?.lowercase().orEmpty()
        val filtered = if (normalizedQuery.isBlank()) {
            items
        } else {
            items.filter { item -> item.toString().lowercase().contains(normalizedQuery) }
        }
        val fromIndex = (page * size).coerceAtMost(filtered.size)
        val toIndex = (fromIndex + size).coerceAtMost(filtered.size)
        return OnecReadonlySnapshotCollectionPageDto(
            collection = collection,
            page = page,
            size = size,
            total = filtered.size,
            rows = filtered.subList(fromIndex, toIndex),
        )
    }

    suspend fun importSnapshot(
        bytes: ByteArray,
        sourceName: String,
        importedBy: String?,
    ): OnecReadonlySnapshotImportResponseDto {
        require(bytes.isNotEmpty()) { "Файл snapshot 1С пуст." }
        require(bytes.size <= MAX_SNAPSHOT_BYTES) { "Snapshot 1С превышает допустимый размер 25 МБ." }

        val root = objectMapper.readTree(bytes)
        validateSanitized(root)
        val snapshot = objectMapper.treeToValue(root, OnecReadonlySnapshotDto::class.java)
        require(snapshot.nomenclature.size <= MAX_ROWS_PER_COLLECTION) { "Слишком много строк номенклатуры." }
        require(snapshot.inventory.size <= MAX_ROWS_PER_COLLECTION) { "Слишком много строк остатков." }
        require(snapshot.counterparties.size <= MAX_ROWS_PER_COLLECTION) { "Слишком много строк контрагентов." }

        val checksum = snapshot.source?.sourceChecksum
            ?.lowercase()
            ?.takeIf { it.matches(Regex("[0-9a-f]{64}")) }
            ?: sha256(bytes)
        val existing = repository.findBySourceChecksum(checksum)
        if (existing != null) {
            return existing.toImportResponse(idempotent = true)
        }

        val source = snapshot.source
        val now = LocalDateTime.now()
        val entity = repository.save(
            OnecReadonlySnapshotEntity(
                id = UUID.randomUUID().toString(),
                sourceName = source?.sourceName ?: sourceName,
                sourceKind = source?.sourceKind ?: "ONEC_READONLY_MART",
                sourceChecksum = checksum,
                snapshotAt = snapshot.snapshotAt ?: source?.dataActualityAt,
                periodFrom = snapshot.periodFrom,
                periodTo = snapshot.periodTo,
                payloadJson = objectMapper.writeValueAsString(snapshot.copy(status = "ready")),
                metadataJson = source?.let(objectMapper::writeValueAsString),
                importedAt = now,
                importedBy = importedBy,
            ),
        )
        return entity.toImportResponse(idempotent = false)
    }

    private fun validateSanitized(node: JsonNode) {
        when {
            node.isObject -> {
                node.fields().forEachRemaining { (name, value) ->
                    val normalized = name.lowercase()
                    require(FORBIDDEN_FIELD_PARTS.none { forbidden -> normalized.contains(forbidden) }) {
                        "Snapshot содержит запрещённое поле '$name'. Передавайте только обезличенную read-only витрину."
                    }
                    validateSanitized(value)
                }
            }
            node.isArray -> node.forEach(::validateSanitized)
        }
    }

    private fun OnecReadonlySnapshotEntity.toImportResponse(
        idempotent: Boolean,
    ): OnecReadonlySnapshotImportResponseDto {
        val snapshot = objectMapper.readValue(payloadJson, OnecReadonlySnapshotDto::class.java)
        return OnecReadonlySnapshotImportResponseDto(
            id = id,
            idempotent = idempotent,
            importedAt = importedAt,
            nomenclatureCount = snapshot.nomenclature.size,
            inventoryCount = snapshot.inventory.size,
            receiptCount = snapshot.receipts.size,
            writeOffCount = snapshot.writeOffs.size,
            counterpartyCount = snapshot.counterparties.size,
        )
    }

    private fun sha256(bytes: ByteArray): String =
        MessageDigest.getInstance("SHA-256")
            .digest(bytes)
            .joinToString("") { "%02x".format(it) }

    private companion object {
        val COLLECTIONS = setOf("nomenclature", "inventory", "receipts", "writeOffs", "counterparties")
        const val MAX_SNAPSHOT_BYTES = 25 * 1024 * 1024
        const val MAX_ROWS_PER_COLLECTION = 100_000
        val FORBIDDEN_FIELD_PARTS = setOf(
            "patient",
            "пациент",
            "iin",
            "иин",
            "diagnos",
            "диагноз",
            "history",
            "истори",
            "birth",
            "рожд",
        )
    }
}
