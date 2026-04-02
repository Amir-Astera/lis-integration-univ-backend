package project.gigienist_reports.feature.authority.data.entity

import org.springframework.data.annotation.Id
import org.springframework.data.annotation.Version
import org.springframework.data.relational.core.mapping.Table
import java.time.LocalDateTime

@Table(name = "authority")
data class AuthorityEntity(
        @Id
        val id: String,
        val name: String,
        val description: String,
        @Version
        val version: Long? = null,
        val createdAt: LocalDateTime? = null,
        val updatedAt: LocalDateTime
)
