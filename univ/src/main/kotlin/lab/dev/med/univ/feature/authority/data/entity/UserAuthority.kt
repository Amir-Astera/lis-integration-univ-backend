package project.gigienist_reports.feature.authority.data.entity

import org.springframework.data.annotation.Id
import org.springframework.data.annotation.Version
import org.springframework.data.relational.core.mapping.Table
import java.time.LocalDateTime

@Table(name = "user_authority")
data class UserAuthorityEntity(
        @Id
        val id: String,
        val userId: String,
        val authorityId: String,
        @Version
        val version: Long?,
        val createdAt: LocalDateTime? = null,
        val updatedAt: LocalDateTime
)
