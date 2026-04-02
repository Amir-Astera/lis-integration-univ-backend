package project.gigienist_reports.feature.city.domain.models

import java.time.LocalDateTime

data class City(
    val id: String,
    val name: String,
    val version: Long? = null,
    val createdAt: LocalDateTime? = null,
    val updatedAt: LocalDateTime = LocalDateTime.now(),
)

data class CityOffice(
    val id: String,
    val name: String,
    val version: Long? = null,
    val createdAt: LocalDateTime? = null,
    val updatedAt: LocalDateTime? = LocalDateTime.now(),
)
