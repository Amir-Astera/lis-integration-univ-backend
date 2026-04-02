package project.gigienist_reports.feature.users.data.entity

import org.springframework.data.annotation.Id
import org.springframework.data.annotation.Version
import org.springframework.data.relational.core.mapping.Table
import java.time.LocalDateTime

@Table(name = "users")
class UserEntity(
        @Id
        val id: String,
        val name: String,
        val surname: String?,
        val email: String?,
        val phone: String?,
        val login: String,
        val logo: String?,
        @Version
        var version: Long?,
        val createdAt: LocalDateTime? = null,
        val updatedAt: LocalDateTime
)
