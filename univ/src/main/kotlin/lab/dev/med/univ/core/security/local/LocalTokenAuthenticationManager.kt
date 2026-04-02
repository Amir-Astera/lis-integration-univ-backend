package project.gigienist_reports.core.security.local

import kotlinx.coroutines.reactor.mono
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.authentication.ReactiveAuthenticationManager
import org.springframework.security.core.Authentication
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken
import project.gigienist_reports.feature.authorization.domain.services.LocalSessionAuthenticationService
import reactor.core.publisher.Mono

class LocalTokenAuthenticationManager(
    private val localSessionAuthenticationService: LocalSessionAuthenticationService,
) : ReactiveAuthenticationManager {
    override fun authenticate(authentication: Authentication): Mono<Authentication> {
        if (authentication.isAuthenticated) {
            return Mono.just(authentication)
        }

        return Mono.just(authentication)
            .cast(PreAuthenticatedAuthenticationToken::class.java)
            .flatMap { token ->
                val rawToken = token.credentials as? String
                    ?: return@flatMap Mono.error(BadCredentialsException("Invalid Credentials"))
                mono {
                    localSessionAuthenticationService.resolveSessionUser(rawToken)
                }
            }
            .onErrorResume { error ->
                Mono.error(BadCredentialsException("Invalid Credentials", error))
            }
            .map { sessionUser ->
                PreAuthenticatedAuthenticationToken(sessionUser, authentication.credentials, sessionUser.authorities)
            }
    }
}
