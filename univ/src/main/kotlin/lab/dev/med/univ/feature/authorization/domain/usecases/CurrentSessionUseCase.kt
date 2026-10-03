package project.gigienist_reports.feature.authorization.domain.usecases

import project.gigienist_reports.feature.authorization.presentation.dto.UserInfo
import project.gigienist_reports.core.security.SessionUser
import kotlinx.coroutines.reactive.awaitFirst
import org.springframework.http.HttpStatus
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken
import org.springframework.stereotype.Service
import org.springframework.web.server.ResponseStatusException
import org.springframework.web.server.ServerWebExchange
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.switchIfEmpty

interface CurrentSessionUseCase {
    suspend operator fun invoke(exchange: ServerWebExchange): Mono<UserInfo>
}

@Service
internal class CurrentSessionUseCaseImpl: CurrentSessionUseCase {
    override suspend fun invoke(exchange: ServerWebExchange): Mono<UserInfo> {
        return exchange.getPrincipal<PreAuthenticatedAuthenticationToken>().map { principal ->
            val sessionUser = principal.principal as? SessionUser
            val authorities = sessionUser?.authorities
                ?.map { it.authority }
                .orEmpty()
            Mono.just(
                UserInfo(
                    email = sessionUser?.login ?: principal.name,
                    authorities = authorities,
                    isAdmin = authorities.any { it.equals("admin", ignoreCase = true) },
                ),
            )
        }.switchIfEmpty {
            Mono.error(ResponseStatusException(HttpStatus.UNAUTHORIZED, "Пользователь не авторизован"))
        }.awaitFirst()
    }
}