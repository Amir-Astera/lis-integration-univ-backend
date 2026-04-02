package project.gigienist_reports.core.config

import org.springframework.security.web.server.util.matcher.ServerWebExchangeMatcher
import org.springframework.stereotype.Component
import org.springframework.web.server.ServerWebExchange
import reactor.core.publisher.Mono

@Component
class CookieExchangeMatcher : ServerWebExchangeMatcher {
    override fun matches(exchange: ServerWebExchange): Mono<ServerWebExchangeMatcher.MatchResult> {
        val request = exchange.request
        val cookies = request.cookies["SESSIONID"]
        return if (!cookies.isNullOrEmpty()) {
            ServerWebExchangeMatcher.MatchResult.match()
        } else {
            ServerWebExchangeMatcher.MatchResult.notMatch()
        }
    }
}