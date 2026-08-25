package lab.dev.med.univ.feature.agent.data

import org.springframework.data.annotation.Id
import org.springframework.data.annotation.Version
import org.springframework.data.relational.core.mapping.Table
import java.time.LocalDateTime

@Table("agent_api_keys")
data class AgentApiKeyEntity(
    @Id
    val id: String,
    val name: String,
    val keyHash: String,
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val lastSeenAt: LocalDateTime? = null,
    val lastIp: String? = null,
    val revokedAt: LocalDateTime? = null,
    @Version
    val version: Long? = null,
)
