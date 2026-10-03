package lab.dev.med.univ.feature.onec.presentation.dto

import com.fasterxml.jackson.databind.JsonNode
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime

data class OnecReadonlySnapshotDto(
    val status: String = "ready",
    val snapshotAt: LocalDateTime? = null,
    val periodFrom: LocalDate? = null,
    val periodTo: LocalDate? = null,
    val source: OnecSnapshotSourceDto? = null,
    val nomenclature: List<OnecNomenclatureDto> = emptyList(),
    val inventory: List<OnecInventoryDto> = emptyList(),
    val receipts: List<OnecReceiptDto> = emptyList(),
    val writeOffs: List<OnecWriteOffDto> = emptyList(),
    val counterparties: List<OnecCounterpartyDto> = emptyList(),
)

data class OnecSnapshotSourceDto(
    val sourceName: String,
    val sourceKind: String,
    val sourceChecksum: String,
    val exportedAt: LocalDateTime? = null,
    val dataActualityAt: LocalDateTime? = null,
    val notes: String? = null,
)

data class OnecNomenclatureDto(
    val code: String? = null,
    val name: String,
    val unit: String? = null,
    val article: String? = null,
    val isMedication: Boolean? = null,
    val isMedicalDevice: Boolean? = null,
    val medicineRegistrationNumber: String? = null,
)

data class OnecInventoryDto(
    val nomenclatureCode: String? = null,
    val nomenclatureName: String,
    val warehouse: String? = null,
    val quantity: BigDecimal? = null,
    val unit: String? = null,
    val cost: BigDecimal? = null,
    val lotNumber: String? = null,
    val expiryDate: LocalDate? = null,
    val sourceActualityAt: LocalDateTime? = null,
)

data class OnecReceiptDto(
    val date: LocalDate? = null,
    val documentNumber: String? = null,
    val nomenclatureName: String? = null,
    val quantity: BigDecimal? = null,
    val unitPrice: BigDecimal? = null,
    val amount: BigDecimal? = null,
    val supplierName: String? = null,
    val supplierInn: String? = null,
)

data class OnecWriteOffDto(
    val date: LocalDate? = null,
    val documentNumber: String? = null,
    val nomenclatureName: String? = null,
    val quantity: BigDecimal? = null,
    val amount: BigDecimal? = null,
    val reason: String? = null,
)

data class OnecCounterpartyDto(
    val inn: String? = null,
    val name: String,
    val type: String? = null,
    val code: String? = null,
)

data class OnecReadonlySnapshotImportResponseDto(
    val id: String,
    val idempotent: Boolean,
    val importedAt: LocalDateTime,
    val nomenclatureCount: Int,
    val inventoryCount: Int,
    val receiptCount: Int,
    val writeOffCount: Int,
    val counterpartyCount: Int,
)

/** Lightweight first response for the 1C page; excludes large collections. */
data class OnecReadonlySnapshotSummaryDto(
    val status: String = "ready",
    val snapshotAt: LocalDateTime? = null,
    val periodFrom: LocalDate? = null,
    val periodTo: LocalDate? = null,
    val source: OnecSnapshotSourceDto? = null,
    val nomenclatureCount: Int = 0,
    val inventoryCount: Int = 0,
    val receiptCount: Int = 0,
    val writeOffCount: Int = 0,
    val counterpartyCount: Int = 0,
)

/** A bounded page from one sanitized snapshot collection. */
data class OnecReadonlySnapshotCollectionPageDto(
    val collection: String,
    val page: Int,
    val size: Int,
    val total: Int,
    val rows: List<JsonNode>,
)
