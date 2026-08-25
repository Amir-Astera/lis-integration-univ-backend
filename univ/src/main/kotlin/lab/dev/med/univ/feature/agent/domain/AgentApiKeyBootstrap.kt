package lab.dev.med.univ.feature.agent.domain

import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import lab.dev.med.univ.feature.agent.data.AgentApiKeyEntity
import lab.dev.med.univ.feature.agent.data.AgentApiKeyRepository
import org.slf4j.LoggerFactory
import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import org.springframework.stereotype.Component
import java.security.MessageDigest
import java.security.SecureRandom
import java.time.LocalDateTime
import java.util.Base64
import java.util.UUID

/**
 * On first startup, if there are no agent API keys in the database, generates one,
 * stores only the SHA-256 hash, and prints the raw key once to the application log.
 *
 * The raw key is NEVER stored — copy it immediately and put it in the
 * Windows service config (appsettings.Production.json → AgentApiKey).
 *
 * On subsequent startups the key is already in the DB so nothing is printed.
 */
@Component
class AgentApiKeyBootstrap(
    private val agentApiKeyRepository: AgentApiKeyRepository,
) : ApplicationRunner {

    private val log = LoggerFactory.getLogger(AgentApiKeyBootstrap::class.java)

    override fun run(args: ApplicationArguments?) = runBlocking {
        try {
            val existing = agentApiKeyRepository.findAll().toList()
            if (existing.isNotEmpty()) {
                return@runBlocking
            }

            val rawKey = generateKey()
            val keyHash = sha256Hex(rawKey)

            agentApiKeyRepository.save(
                AgentApiKeyEntity(
                    id = UUID.randomUUID().toString(),
                    name = "lab-pc-1",
                    keyHash = keyHash,
                    createdAt = LocalDateTime.now(),
                ),
            )

            log.warn("=======================================================================")
            log.warn("  AGENT API KEY GENERATED — copy now, it is shown only ONCE:")
            log.warn("  Key: {}", rawKey)
            log.warn("  Place this value in the agent's appsettings.Production.json:")
            log.warn("    \"AgentApiKey\": \"{}\"", rawKey)
            log.warn("=======================================================================")
        } catch (ex: Exception) {
            log.error("AgentApiKeyBootstrap skipped — DB may not be ready yet", ex)
        }
    }

    private fun generateKey(): String {
        val bytes = ByteArray(32)
        SecureRandom().nextBytes(bytes)
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes)
    }

    private fun sha256Hex(input: String): String {
        val digest = MessageDigest.getInstance("SHA-256").digest(input.toByteArray(Charsets.UTF_8))
        return digest.joinToString("") { "%02x".format(it) }
    }
}
