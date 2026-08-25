package lab.dev.med.univ.feature.reagents.data.entity

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import lab.dev.med.univ.feature.reagents.domain.models.AnalyzerType
import lab.dev.med.univ.feature.reagents.domain.models.ServiceCatalogEntry
import lab.dev.med.univ.feature.reagents.domain.models.ServiceCategory
import org.springframework.data.annotation.Id
import org.springframework.data.annotation.Version
import org.springframework.data.relational.core.mapping.Table
import java.math.BigDecimal
import java.time.LocalDateTime

@Table("service_catalog")
data class ServiceCatalogEntity(
    @Id val id: String,
    val canonicalName: String,
    val category: String,
    val lisAliases: String = "[]",
    val analyzerServiceIds: String = "[]",
    val analyzerParameters: String = "[]",
    val analyzerTypes: String = "[]",
    val lisPriceTenge: BigDecimal? = null,
    val graceHours: Int = 0,
    val isActive: Boolean = true,
    val notes: String? = null,
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val updatedAt: LocalDateTime = LocalDateTime.now(),
    @Version val version: Long? = null,
)

private val STRING_LIST_TYPE  = object : TypeReference<List<String>>() {}
private val INT_LIST_TYPE     = object : TypeReference<List<Int>>() {}

fun ServiceCatalogEntity.toModel(mapper: ObjectMapper): ServiceCatalogEntry {
    val lisAliasesList      = runCatching { mapper.readValue(lisAliases, STRING_LIST_TYPE) }.getOrDefault(emptyList())
    val serviceIdsList      = runCatching { mapper.readValue(analyzerServiceIds, INT_LIST_TYPE) }.getOrDefault(emptyList())
    val parametersList      = runCatching { mapper.readValue(analyzerParameters, STRING_LIST_TYPE) }.getOrDefault(emptyList())
    val analyzerTypesList   = runCatching {
        mapper.readValue(analyzerTypes, STRING_LIST_TYPE).mapNotNull { runCatching { AnalyzerType.valueOf(it) }.getOrNull() }
    }.getOrDefault(emptyList())
    val categoryEnum        = runCatching { ServiceCategory.valueOf(category) }.getOrDefault(ServiceCategory.OTHER)

    return ServiceCatalogEntry(
        id                = id,
        canonicalName     = canonicalName,
        category          = categoryEnum,
        lisAliases        = lisAliasesList,
        analyzerServiceIds = serviceIdsList,
        analyzerParameters = parametersList,
        analyzerTypes     = analyzerTypesList,
        lisPriceTenge     = lisPriceTenge,
        graceHours        = graceHours,
        isActive          = isActive,
        notes             = notes,
        createdAt         = createdAt,
        updatedAt         = updatedAt,
        version           = version,
    )
}

fun ServiceCatalogEntry.toEntity(mapper: ObjectMapper): ServiceCatalogEntity = ServiceCatalogEntity(
    id                 = id,
    canonicalName      = canonicalName,
    category           = category.name,
    lisAliases         = mapper.writeValueAsString(lisAliases),
    analyzerServiceIds = mapper.writeValueAsString(analyzerServiceIds),
    analyzerParameters = mapper.writeValueAsString(analyzerParameters),
    analyzerTypes      = mapper.writeValueAsString(analyzerTypes.map { it.name }),
    lisPriceTenge      = lisPriceTenge,
    graceHours         = graceHours,
    isActive           = isActive,
    notes              = notes,
    createdAt          = createdAt,
    updatedAt          = updatedAt,
    version            = version,
)
