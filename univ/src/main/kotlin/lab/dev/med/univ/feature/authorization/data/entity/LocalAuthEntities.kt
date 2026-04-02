package project.gigienist_reports.feature.authorization.data.entity

import org.springframework.data.annotation.Id
import org.springframework.data.annotation.Version
import org.springframework.data.relational.core.mapping.Table
import java.time.LocalDateTime

@Table("local_auth_credentials")
data class LocalAuthCredentialEntity(
    @Id
    val userId: String,
    val email: String,
    val passwordHash: String,
    val updatedAt: LocalDateTime = LocalDateTime.now(),
    @Version
    val version: Long? = null,
)

@Table("local_auth_sessions")
data class LocalAuthSessionEntity(
    @Id
    val token: String,
    val userId: String,
    val expiresAt: LocalDateTime,
    val createdAt: LocalDateTime = LocalDateTime.now(),
    @Version
    val version: Long? = null,
)
