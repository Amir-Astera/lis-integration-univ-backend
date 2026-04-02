package project.gigienist_reports.core.security.firebase

import org.springframework.http.HttpHeaders
import org.springframework.security.web.server.util.matcher.ServerWebExchangeMatcher
import org.springframework.web.server.ServerWebExchange
import reactor.core.publisher.Mono

class FirebaseHeadersExchangeMatcher : ServerWebExchangeMatcher {
    override fun matches(exchange: ServerWebExchange): Mono<ServerWebExchangeMatcher.MatchResult> {
        val request = Mono.just(exchange).map { obj: ServerWebExchange -> obj.request }

        return request
            .filter{ !it.path.toString().endsWith( "/auth") }
            .map { it.headers }
            .filter { h: HttpHeaders -> h.containsKey(HttpHeaders.AUTHORIZATION) }
            .flatMap { ServerWebExchangeMatcher.MatchResult.match() }
            .switchIfEmpty(ServerWebExchangeMatcher.MatchResult.notMatch())
    }
}
