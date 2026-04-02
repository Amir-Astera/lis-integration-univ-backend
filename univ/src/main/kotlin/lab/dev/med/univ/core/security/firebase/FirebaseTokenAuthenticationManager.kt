package project.gigienist_reports.core.security.firebase

import project.gigienist_reports.feature.authorization.domain.usecases.SaveSessionUserUseCase
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.reactor.mono
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.authentication.ReactiveAuthenticationManager
import org.springframework.security.core.Authentication
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken
import reactor.core.publisher.Mono
import reactor.core.scheduler.Schedulers
import reactor.kotlin.core.publisher.toMono

class FirebaseTokenAuthenticationManager(
        private val auth: FirebaseAuth,
        private val saveSessionUserUseCase: SaveSessionUserUseCase
) : ReactiveAuthenticationManager {

    override fun authenticate(authentication: Authentication): Mono<Authentication> {
        if (authentication.isAuthenticated) {
            return Mono.just(authentication)
        }

        return Mono.just(authentication)
            .switchIfEmpty(Mono.defer(::raiseBadCredentials))
            .cast(PreAuthenticatedAuthenticationToken::class.java)
            .flatMap { token -> authenticateToken(token) }
            .publishOn(Schedulers.parallel())
            .onErrorResume { e -> raiseBadCredentials(e) }
            .switchIfEmpty(Mono.defer(::raiseBadCredentials))
            .map { u -> PreAuthenticatedAuthenticationToken(u, authentication.credentials, u.authorities) }

    }

    private fun <T> raiseBadCredentials(): Mono<T>? {
        return Mono.error(BadCredentialsException("Invalid Credentials"))
    }

    private fun <T> raiseBadCredentials(e: Throwable): Mono<T>? {
        return Mono.error(BadCredentialsException("Invalid Credentials", e))
    }

    private fun authenticateToken(authenticationToken: PreAuthenticatedAuthenticationToken): Mono<UserDetails> {
        val token = authenticationToken.credentials as? String
        val authentication = SecurityContextHolder.getContext().authentication

        if (token != null && authentication == null) {
            return auth
                .verifyIdTokenAsync(token, false)
                .toMono()
                .publishOn(Schedulers.boundedElastic())
                .map { it.get() }
                .flatMap {
                    mono {
                        saveSessionUserUseCase(it)
                    }
                }
        }
        return Mono.empty()

//        val token = authenticationToken.credentials as? String
//                ?: return Mono.empty()
//        // Раньше вы смотрели в SecurityContextHolder, можно убрать это если не нужно
//        SecurityContextHolder.getContext().authentication
//                ?: // verifyIdTokenAsync возвращает ApiFuture -> надо преобразовать в Mono
//                // (см. "apiFutureToMono" паттерн)
//                return apiFutureToMono(auth.verifyIdTokenAsync(token))
//                        .map { decodedToken ->
//                            // Либо используйте get() если возвращается ApiFuture<ApiFuture<T>>
//                            decodedToken // <--- FirebaseToken
//                        }
//                        .flatMap { decodedToken ->
//                            // Сохраняем/обновляем пользователя
//                            mono {
//                                saveSessionUserUseCase(decodedToken)
//                            }
//                        }
//        return Mono.empty()
    }
}