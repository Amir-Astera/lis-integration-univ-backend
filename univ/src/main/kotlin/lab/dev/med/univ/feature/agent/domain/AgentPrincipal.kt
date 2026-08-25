package lab.dev.med.univ.feature.agent.domain

import org.springframework.security.authentication.AbstractAuthenticationToken
import org.springframework.security.core.authority.SimpleGrantedAuthority

/**
 * Represents an authenticated agent (Windows log-uploader service).
 * Carries the agent's ID and name so controllers can identify the source.
 */
class AgentAuthentication(
    val agentId: String,
    val agentName: String,
) : AbstractAuthenticationToken(listOf(SimpleGrantedAuthority("ROLE_AGENT"))) {

    init { isAuthenticated = true }

    override fun getCredentials(): Any = agentId
    override fun getPrincipal(): Any = agentName
}
