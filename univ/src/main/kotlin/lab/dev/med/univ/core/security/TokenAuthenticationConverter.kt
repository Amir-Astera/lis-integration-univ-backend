package project.gigienist_reports.core.security

import project.gigienist_reports.core.security.firebase.FirebaseSecurityUtils
import org.springframework.security.core.Authentication
import org.springframework.security.web.server.authentication.ServerAuthenticationConverter
import org.springframework.web.server.ServerWebExchange
import reactor.core.publisher.Mono
import java.util.*
import java.util.function.Function
import java.util.function.Predicate

class TokenAuthenticationConverter: ServerAuthenticationConverter {

    companion object {
        private const val BEARER = "Bearer "
        private val matchBearerValue = Predicate { authValue: String -> authValue.startsWith(BEARER) && authValue.length > BEARER.length }
        private val isolateBearerValue = Function { authValue: String -> authValue.substring(BEARER.length) }
    }

    override fun convert(exchange: ServerWebExchange): Mono<Authentication> {
        return Mono.justOrEmpty(exchange)
            .map(FirebaseSecurityUtils::getTokenFromRequest)
            .filter { obj -> Objects.nonNull(obj) }
            .filter(matchBearerValue)
            .map(isolateBearerValue)
            .filter { token -> token.isNotEmpty() }
            .map(FirebaseSecurityUtils::getAuthentication)
    }
}