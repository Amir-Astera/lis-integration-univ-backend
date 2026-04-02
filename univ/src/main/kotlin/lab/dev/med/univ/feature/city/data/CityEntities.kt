package project.gigienist_reports.feature.city.data

import java.time.LocalDateTime

data class CityEntity(
    val id: String,
    val name: String,
    val version: Long? = null,
    val createdAt: LocalDateTime? = null,
    val updatedAt: LocalDateTime = LocalDateTime.now(),
)

data class CityOfficeEntity(
    val id: String,
    val cityId: String,
    val name: String,
    val version: Long? = null,
    val createdAt: LocalDateTime? = null,
    val updatedAt: LocalDateTime = LocalDateTime.now(),
)
