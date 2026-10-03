package project.gigienist_reports.feature.authorization.domain.usecases

import project.gigienist_reports.feature.authorization.domain.services.FirebaseAuthService
import org.springframework.stereotype.Service
import project.gigienist_reports.feature.authorization.presentation.dto.AuthResponseDto
import java.util.Base64

interface AuthUseCase {
    suspend operator fun invoke(encodedToken: String) : AuthResponseDto
}

@Service
internal class AuthUseCaseImpl(
    private val service: FirebaseAuthService
) : AuthUseCase {
    override suspend fun invoke(encodedToken: String) : AuthResponseDto {
        val decodedBytes = try {
            Base64.getDecoder().decode(encodedToken)
        } catch (_: IllegalArgumentException) {
            throw IllegalArgumentException("Invalid credentials!")
        }
        val decodedToken = String(decodedBytes)
        val separator = decodedToken.indexOf(':')
        if (separator <= 0) {
            throw IllegalArgumentException("Invalid credentials!")
        }
        val email = decodedToken.substring(0, separator)
        val password = decodedToken.substring(separator + 1)
        return service.auth(email, password)
    }
}