package project.gigienist_reports.feature.clinic.data

import java.math.BigDecimal
import java.time.LocalDateTime

data class ClinicEntity(
    val id: String,
    val code: String,
    val name: String,
    val address: String? = null,
    val phone: String? = null,
    val email: String? = null,
    val distanceFromLab: Int? = null,
    val active: Boolean = true,
    val priceVersion: Long? = null,
    val cityOfficeId: String? = null,
    val version: Long? = null,
    val createdAt: LocalDateTime? = null,
    val updatedAt: LocalDateTime = LocalDateTime.now(),
)

data class PriceItemEntity(
    val id: String,
    val customerId: String,
    val code: String,
    val name: String,
    val price: BigDecimal,
    val currency: String? = null,
    val effectiveFrom: LocalDateTime? = null,
    val effectiveTo: LocalDateTime? = null,
    val priceVersion: Long? = null,
    val version: Long? = null,
    val createdAt: LocalDateTime? = null,
    val updatedAt: LocalDateTime? = null,
)
