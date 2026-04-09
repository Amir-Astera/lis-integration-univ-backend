package project.gigienist_reports.feature.authorization.domain.services

import org.springframework.context.annotation.Profile
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.security.core.authority.SimpleGrantedAuthority
import project.gigienist_reports.core.security.SessionUser
import project.gigienist_reports.feature.authorization.data.entity.LocalAuthCredentialEntity
import project.gigienist_reports.feature.authorization.data.entity.LocalAuthSessionEntity
import project.gigienist_reports.feature.authorization.data.repository.LocalAuthCredentialRepository
import project.gigienist_reports.feature.authorization.data.repository.LocalAuthSessionRepository
import project.gigienist_reports.feature.authorization.domain.errors.FirebaseAuthException
import project.gigienist_reports.feature.authorization.presentation.dto.AuthResponseDto
import project.gigienist_reports.core.config.properties.LocalAuthProperties
import project.gigienist_reports.feature.users.domain.services.UserAggregateService
import org.springframework.beans.factory.ObjectProvider
import java.time.LocalDateTime
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

interface LocalSessionAuthenticationService {
    suspend fun authenticate(email: String, password: String): AuthResponseDto
    suspend fun verifyToken(token: String): Boolean
    suspend fun resolveSessionUser(token: String): SessionUser
    suspend fun upsertCredential(userId: String, email: String?, password: String?)
}

@Service
@Profile("!firebase")
class LocalSessionAuthenticationServiceImpl(
    private val credentialRepositoryProvider: ObjectProvider<LocalAuthCredentialRepository>,
    private val sessionRepositoryProvider: ObjectProvider<LocalAuthSessionRepository>,
    private val localAuthProperties: LocalAuthProperties,
    private val userAggregateServiceProvider: ObjectProvider<UserAggregateService>,
) : LocalSessionAuthenticationService {
    private val passwordEncoder = BCryptPasswordEncoder()
    private val inMemorySessions = ConcurrentHashMap<String, LocalAuthSessionEntity>()

    override suspend fun authenticate(email: String, password: String): AuthResponseDto {
        val normalizedEmail = normalizeEmail(email)
        if (isConfiguredLocalAdmin(normalizedEmail, password)) {
            return issueToken(userId = configuredLocalAdminUserId())
        }

        val credentialRepository = credentialRepositoryProvider.ifAvailable
            ?: throw FirebaseAuthException("Local credential repository is not available.")
        val credential = credentialRepository.findByEmail(normalizedEmail)
            ?: throw FirebaseAuthException("Invalid email or password.")

        if (!passwordEncoder.matches(password, credential.passwordHash)) {
            throw FirebaseAuthException("Invalid email or password.")
        }

        return issueToken(userId = credential.userId)
    }

    override suspend fun verifyToken(token: String): Boolean {
        return findActiveSession(token) != null
    }

    override suspend fun resolveSessionUser(token: String): SessionUser {
        val session = findActiveSession(token) ?: throw FirebaseAuthException("Token not valid!")
        if (session.userId == configuredLocalAdminUserId()) {
            return SessionUser(
                name = localAuthProperties.bootstrapAdminName,
                login = localAuthProperties.bootstrapAdminEmail.trim().lowercase(),
                isEmailVerified = true,
                issuer = "dev",
                picture = "",
                authorities = listOf(SimpleGrantedAuthority("admin")),
            )
        }
        val userAggregateService = userAggregateServiceProvider.ifAvailable
            ?: throw FirebaseAuthException("User service is not available.")
        val user = userAggregateService.get(session.userId)
        return SessionUser(
            name = user.name,
            login = user.login,
            isEmailVerified = true,
            issuer = "dev",
            picture = "",
            authorities = user.authorities,
        )
    }

    override suspend fun upsertCredential(userId: String, email: String?, password: String?) {
        val normalizedEmail = normalizeEmail(email)
        val rawPassword = password?.trim()?.takeIf { it.isNotEmpty() }
            ?: throw IllegalArgumentException("Password is required for dev authentication.")
        val credentialRepository = credentialRepositoryProvider.ifAvailable
            ?: throw FirebaseAuthException("Local credential repository is not available.")
        val existingCredential = credentialRepository.findByEmail(normalizedEmail)
        if (existingCredential != null && existingCredential.userId != userId) {
            throw FirebaseAuthException("Credential with this email already exists.")
        }
        credentialRepository.save(
            LocalAuthCredentialEntity(
                userId = userId,
                email = normalizedEmail,
                passwordHash = passwordEncoder.encode(rawPassword),
                updatedAt = LocalDateTime.now(),
                version = existingCredential?.version,
            ),
        )
    }

    private suspend fun issueToken(userId: String): AuthResponseDto {
        val token = UUID.randomUUID().toString()
        val expiresAt = LocalDateTime.now().plusHours(12)
        val session = LocalAuthSessionEntity(
            token = token,
            userId = userId,
            expiresAt = expiresAt,
        )
        if (userId == configuredLocalAdminUserId()) {
            inMemorySessions[token] = session
        } else {
            val sessionRepository = sessionRepositoryProvider.ifAvailable
                ?: throw FirebaseAuthException("Local session repository is not available.")
            sessionRepository.save(session)
        }

        return AuthResponseDto(
            tokenType = "Bearer",
            accessToken = token,
            refreshToken = "",
            expiresIn = (12 * 60 * 60).toString(),
        )
    }

    private fun isConfiguredLocalAdmin(email: String, password: String): Boolean {
        return email == localAuthProperties.bootstrapAdminEmail.trim().lowercase() &&
            password == localAuthProperties.bootstrapAdminPassword
    }

    private fun configuredLocalAdminUserId(): String {
        return "dev-bootstrap-admin"
    }

    private suspend fun findActiveSession(token: String): LocalAuthSessionEntity? {
        val inMemorySession = inMemorySessions[token]
        if (inMemorySession != null) {
            if (inMemorySession.expiresAt.isBefore(LocalDateTime.now())) {
                inMemorySessions.remove(token)
                return null
            }
            return inMemorySession
        }
        val sessionRepository = sessionRepositoryProvider.ifAvailable ?: return null
        val session = sessionRepository.findById(token) ?: return null
        if (session.expiresAt.isBefore(LocalDateTime.now())) {
            sessionRepository.deleteById(token)
            return null
        }
        return session
    }

    private fun normalizeEmail(email: String?): String {
        return email?.trim()?.lowercase()?.takeIf { it.isNotEmpty() }
            ?: throw IllegalArgumentException("Email is required for dev authentication.")
    }
}
