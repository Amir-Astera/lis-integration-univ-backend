package lab.dev.med.univ.feature.reagents.presentation.dto

import lab.dev.med.univ.feature.reagents.domain.models.AnalyzerType
import lab.dev.med.univ.feature.reagents.domain.models.ServiceCatalogEntry
import lab.dev.med.univ.feature.reagents.domain.models.ServiceCategory
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.UUID

/** Public view of a service catalog entry. */
data class ServiceCatalogEntryDto(
    val id: String,
    val canonicalName: String,
    val category: String,
    val categoryLabel: String,
    val lisAliases: List<String>,
    val analyzerServiceIds: List<Int>,
    val analyzerParameters: List<String>,
    val analyzerTypes: List<String>,
    val lisPriceTenge: BigDecimal?,
    val graceHours: Int,
    val isActive: Boolean,
    val notes: String?,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime,
)

fun ServiceCatalogEntry.toDto() = ServiceCatalogEntryDto(
    id                 = id,
    canonicalName      = canonicalName,
    category           = category.name,
    categoryLabel      = category.displayName,
    lisAliases         = lisAliases,
    analyzerServiceIds = analyzerServiceIds,
    analyzerParameters = analyzerParameters,
    analyzerTypes      = analyzerTypes.map { it.name },
    lisPriceTenge      = lisPriceTenge,
    graceHours         = graceHours,
    isActive           = isActive,
    notes              = notes,
    createdAt          = createdAt,
    updatedAt          = updatedAt,
)

/** Write model for create/update. */
data class ServiceCatalogUpsertRequest(
    val id: String? = null,
    val canonicalName: String,
    val category: String,
    val lisAliases: List<String> = emptyList(),
    val analyzerServiceIds: List<Int> = emptyList(),
    val analyzerParameters: List<String> = emptyList(),
    val analyzerTypes: List<String> = emptyList(),
    val lisPriceTenge: BigDecimal? = null,
    val graceHours: Int = 0,
    val isActive: Boolean = true,
    val notes: String? = null,
) {
    /**
     * Convert to domain entry. Validates enum values; unknown enum names
     * are silently dropped (prevents runtime crashes on schema evolution).
     */
    fun toDomain(existing: ServiceCatalogEntry? = null): ServiceCatalogEntry {
        val categoryEnum = runCatching { ServiceCategory.valueOf(category.uppercase()) }
            .getOrElse { ServiceCategory.OTHER }
        val typesEnum = analyzerTypes.mapNotNull {
            runCatching { AnalyzerType.valueOf(it.uppercase()) }.getOrNull()
        }
        val now = LocalDateTime.now()
        return ServiceCatalogEntry(
            id                 = id ?: existing?.id ?: UUID.randomUUID().toString(),
            canonicalName      = canonicalName.trim(),
            category           = categoryEnum,
            lisAliases         = lisAliases.map { it.trim() }.filter { it.isNotEmpty() }.distinct(),
            analyzerServiceIds = analyzerServiceIds.distinct(),
            analyzerParameters = analyzerParameters.map { it.trim() }.filter { it.isNotEmpty() }.distinct(),
            analyzerTypes      = typesEnum,
            lisPriceTenge      = lisPriceTenge,
            graceHours         = graceHours.coerceAtLeast(0),
            isActive           = isActive,
            notes              = notes?.trim()?.takeIf { it.isNotEmpty() },
            createdAt          = existing?.createdAt ?: now,
            updatedAt          = now,
            version            = existing?.version,
        )
    }
}
