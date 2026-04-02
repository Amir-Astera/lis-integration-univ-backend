package project.gigienist_reports.feature.authorization.domain.usecases

import project.gigienist_reports.feature.authorization.domain.services.FirebaseAuthService
import org.springframework.http.ResponseCookie
import org.springframework.http.server.reactive.ServerHttpResponse
import org.springframework.stereotype.Service
import project.gigienist_reports.feature.authorization.presentation.dto.AuthResponseDto
import java.time.Duration
import java.util.*

interface AuthUseCase {
    suspend operator fun invoke(encodedToken: String) : AuthResponseDto
}

@Service
internal class AuthUseCaseImpl(
    private val service: FirebaseAuthService
) : AuthUseCase {
    override suspend fun invoke(encodedToken: String) : AuthResponseDto {
        println(encodedToken)
        val decodedBytes = Base64.getDecoder().decode(encodedToken)
        val decodedToken = String(decodedBytes)
        val credentials = decodedToken.split(":")
        if (credentials.size != 2) {
            throw IllegalArgumentException("Invalid credentials!")
        }
        val email = credentials.first()
        val password = credentials.last()
        return service.auth(email, password)
    }
}