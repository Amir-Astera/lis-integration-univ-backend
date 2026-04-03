package project.gigienist_reports.feature.authorization.domain.services

import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Service
import project.gigienist_reports.feature.authorization.presentation.dto.AuthResponseDto

@Service
@Profile("!firebase")
class LocalFirebaseAuthService(
    private val localSessionAuthenticationService: LocalSessionAuthenticationService,
) : FirebaseAuthService {
    override suspend fun auth(email: String, password: String): AuthResponseDto {
        return localSessionAuthenticationService.authenticate(email, password)
    }

    override suspend fun verifyToken(token: String): Boolean = localSessionAuthenticationService.verifyToken(token)
}
