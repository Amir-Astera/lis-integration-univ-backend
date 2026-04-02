package project.gigienist_reports.feature.authorization.domain.usecases

import project.gigienist_reports.feature.authorization.domain.errors.FirebaseAuthException
import project.gigienist_reports.feature.authorization.domain.services.FirebaseAuthService
import project.gigienist_reports.feature.authorization.presentation.dto.StoreToken
import org.springframework.http.ResponseCookie
import org.springframework.http.server.reactive.ServerHttpResponse
import org.springframework.stereotype.Service
import java.time.Duration


interface StoreTokenUseCase {
    suspend operator fun invoke(reqToken: StoreToken, response: ServerHttpResponse)
}

@Service
internal class StoreTokenUseCaseImpl(
        private val firebaseAuthService: FirebaseAuthService
): StoreTokenUseCase {
    override suspend fun invoke(reqToken: StoreToken, response: ServerHttpResponse) {
        if (!firebaseAuthService.verifyToken(reqToken.token)) throw FirebaseAuthException("Token not valid!")
        val cookie = ResponseCookie.from("SESSIONID", reqToken.token)
                .httpOnly(true)
                .secure(true) //для https - ture
                .path("/")
                .maxAge(Duration.ofHours(1))
                .sameSite("None")
                .build()
        response.addCookie(cookie)
    }
}