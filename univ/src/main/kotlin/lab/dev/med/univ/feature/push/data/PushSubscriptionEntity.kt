package project.gigienist_reports.feature.push.data

import java.time.LocalDateTime

data class PushSubscriptionEntity(
    val id: String,
    val userId: String,
    val endpoint: String,
    val p256dh: String,
    val auth: String,
    val userAgent: String? = null,
    val active: Boolean = true,
    val version: Long? = null,
    val createdAt: LocalDateTime? = null,
    val updatedAt: LocalDateTime? = null,
)
