package lab.dev.med.univ.feature.agent.domain

import kotlinx.coroutines.reactor.mono
import lab.dev.med.univ.feature.agent.data.AgentApiKeyRepository
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.security.core.context.ReactiveSecurityContextHolder
import org.springframework.stereotype.Component
import org.springframework.web.server.ServerWebExchange
import org.springframework.web.server.WebFilter
import org.springframework.web.server.WebFilterChain
import reactor.core.publisher.Mono
import java.security.MessageDigest
import java.time.LocalDateTime

private const val AGENT_KEY_HEADER = "X-Agent-Key"
private const val AGENT_PATH_PREFIX = "/api/agent/"

/**
 * Authenticates requests to /api/agent/** by an API key passed in X-Agent-Key.
 * Sets a [AgentAuthentication] in the reactive security context so controllers
 * can identify the calling agent via [ReactiveSecurityContextHolder].
 *
 * Touches `last_seen_at` / `last_ip` as a fire-and-forget side effect — failures
 * to update those columns must NOT break the request itself.
 */
@Component
class AgentSecurityFilter(
    private val agentApiKeyRepository: AgentApiKeyRepository,
) : WebFilter {

    private val log = LoggerFactory.getLogger(AgentSecurityFilter::class.java)

    override fun filter(exchange: ServerWebExchange, chain: WebFilterChain): Mono<Void> {
        val path = exchange.request.path.value()
        if (!path.startsWith(AGENT_PATH_PREFIX)) {
            return chain.filter(exchange)
        }

        val rawKey = exchange.request.headers.getFirst(AGENT_KEY_HEADER)
        if (rawKey.isNullOrBlank()) {
            log.warn("Agent request to {} missing X-Agent-Key header", path)
            return unauthorized(exchange)
        }

        val hash = sha256Hex(rawKey)

        return mono { agentApiKeyRepository.findByKeyHash(hash) }
            .flatMap { entity ->
                if (entity == null || entity.revokedAt != null) {
                    log.warn("Agent request to {} rejected — key not found or revoked", path)
                    unauthorized(exchange)
                } else {
                    // Update last_seen_at as fire-and-forget — never break the request.
                    val clientIp = exchange.request.remoteAddress?.address?.hostAddress
                    mono {
                        runCatching {
                            agentApiKeyRepository.save(
                                entity.copy(
                                    lastSeenAt = LocalDateTime.now(),
                                    lastIp = clientIp,
                                ),
                            )
                        }.onFailure { ex ->
                            log.debug("Failed to update agent last_seen_at (non-fatal)", ex)
                        }
                    }.subscribe()

                    chain.filter(exchange).contextWrite(
                        ReactiveSecurityContextHolder.withAuthentication(
                            AgentAuthentication(agentId = entity.id, agentName = entity.name),
                        ),
                    )
                }
            }
            .onErrorResume { ex ->
                log.error("Agent auth failed unexpectedly", ex)
                unauthorized(exchange)
            }
    }

    private fun unauthorized(exchange: ServerWebExchange): Mono<Void> {
        exchange.response.statusCode = HttpStatus.UNAUTHORIZED
        return exchange.response.setComplete()
    }

    private fun sha256Hex(input: String): String {
        val digest = MessageDigest.getInstance("SHA-256").digest(input.toByteArray(Charsets.UTF_8))
        return digest.joinToString("") { "%02x".format(it) }
    }
}
