package project.gigienist_reports.feature.authorization.data.repository

import org.springframework.data.repository.kotlin.CoroutineCrudRepository
import project.gigienist_reports.feature.authorization.data.entity.LocalAuthCredentialEntity
import project.gigienist_reports.feature.authorization.data.entity.LocalAuthSessionEntity

interface LocalAuthCredentialRepository : CoroutineCrudRepository<LocalAuthCredentialEntity, String> {
    suspend fun findByEmail(email: String): LocalAuthCredentialEntity?
}

interface LocalAuthSessionRepository : CoroutineCrudRepository<LocalAuthSessionEntity, String>
