import org.springframework.security.core.Authentication
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken
import org.springframework.security.web.server.authentication.ServerAuthenticationConverter
import org.springframework.stereotype.Component
import org.springframework.web.server.ServerWebExchange
import reactor.core.publisher.Mono

@Component
class CookieAuthenticationConverter : ServerAuthenticationConverter {
    override fun convert(exchange: ServerWebExchange): Mono<Authentication> {
        val request = exchange.request
        val cookies = request.cookies["SESSIONID"]
        if (cookies.isNullOrEmpty()) {
            // Куки нет — выходим
            return Mono.empty()
        }

        // Берём первое значение куки
        val tokenValue = cookies.first().value
        if (tokenValue.isBlank()) {
            return Mono.empty()
        }

        // Собираем PreAuthenticatedAuthenticationToken:
        // principal = null или "COOKIE", credentials = сам токен
        val authentication = PreAuthenticatedAuthenticationToken(
                /* aPrincipal = */ "COOKIE",
                /* aCredentials = */ tokenValue
        )
        return Mono.just(authentication)
    }
}