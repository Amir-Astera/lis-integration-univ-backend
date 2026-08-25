package lab.dev.med.univ.feature.agent.data

import org.springframework.data.repository.kotlin.CoroutineCrudRepository

interface AgentApiKeyRepository : CoroutineCrudRepository<AgentApiKeyEntity, String> {
    suspend fun findByKeyHash(keyHash: String): AgentApiKeyEntity?
}
