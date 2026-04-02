package project.gigienist_reports.feature.authorization.presentation.rest

import project.gigienist_reports.core.config.api.Controller
import project.gigienist_reports.feature.authorization.domain.usecases.AuthUseCase
import project.gigienist_reports.feature.authorization.domain.usecases.CurrentSessionUseCase
import project.gigienist_reports.feature.authorization.domain.usecases.LogoutUseCase
import project.gigienist_reports.feature.authorization.domain.usecases.StoreTokenUseCase
import project.gigienist_reports.feature.authorization.presentation.dto.StoreToken
import project.gigienist_reports.feature.authorization.presentation.dto.UserInfo
import io.swagger.v3.oas.annotations.Hidden
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import kotlinx.coroutines.reactive.awaitFirst
import org.slf4j.Logger
import org.springframework.http.CacheControl
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.http.server.reactive.ServerHttpRequest
import org.springframework.http.server.reactive.ServerHttpResponse
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.server.ServerWebExchange
import project.gigienist_reports.feature.authorization.presentation.dto.AuthResponseDto


@Hidden
@RestController
class AuthController(
    logger: Logger,
    private val authUseCase: AuthUseCase,
    private val logoutUseCase: LogoutUseCase,
    private val storeTokenUseCase: StoreTokenUseCase,
    private val currentSessionUseCase: CurrentSessionUseCase
) : Controller(logger) {

    @PostMapping("/auth")
    suspend fun create(
        @Parameter(hidden = true)
        request: ServerHttpRequest
    ): ResponseEntity<AuthResponseDto> {
        val authorizationHeader = request.headers.getFirst(HttpHeaders.AUTHORIZATION) ?: ""
        val encodedToken = authorizationHeader.split(' ').lastOrNull() ?: ""
        val response = authUseCase(encodedToken)
        return ResponseEntity
            .status(HttpStatus.OK)
            .cacheControl(CacheControl.noCache())
            .body(response)
    }

    @SecurityRequirement(name = "security_auth")
    @PostMapping("/logout")
    suspend fun logout(
            @Parameter(hidden = true)
            request: ServerHttpRequest,
            @Parameter(hidden = true)
            response: ServerHttpResponse
    ): ResponseEntity<Any> {
        logoutUseCase(response)
        return ResponseEntity
                .status(HttpStatus.OK)
                .cacheControl(CacheControl.noCache())
                .body(response)
    }

    @PostMapping("/auth/me")
    suspend fun currentSession(
            @Parameter(hidden = true)
            exchange: ServerWebExchange
    ): ResponseEntity<UserInfo> {
        return ResponseEntity.ok(currentSessionUseCase(exchange).awaitFirst())
    }
}