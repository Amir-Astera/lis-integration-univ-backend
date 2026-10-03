package lab.dev.med.univ.feature.director.data.entity

import org.springframework.data.annotation.Id
import org.springframework.data.annotation.Version
import org.springframework.data.relational.core.mapping.Table
import java.time.LocalDateTime

@Table("director_assistant_messages")
data class DirectorAssistantMessageEntity(
    @Id
    val id: String,
    val userLogin: String,
    val messageRole: String,
    val content: String,
    val model: String? = null,
    val evidenceGeneratedAt: LocalDateTime? = null,
    val evidenceScopeJson: String? = null,
    val evidenceSnapshotJson: String? = null,
    val createdAt: LocalDateTime = LocalDateTime.now(),
    @Version
    val version: Long? = null,
)
