package project.gigienist_reports.feature.authorization.domain.services

import kotlinx.coroutines.runBlocking
import org.slf4j.Logger
import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component
import project.gigienist_reports.core.config.properties.LocalAuthProperties
import project.gigienist_reports.feature.authority.domain.models.Authority
import project.gigienist_reports.feature.authority.domain.services.AuthorityAggregateService
import project.gigienist_reports.feature.users.domain.models.UserAggregate
import project.gigienist_reports.feature.users.domain.services.UserAggregateService
import java.time.LocalDateTime

@Component
@Profile("dev | local")
@ConditionalOnProperty(prefix = "dev.auth.bootstrap", name = ["enabled"], havingValue = "true", matchIfMissing = true)
class LocalBootstrapAdminInitializer(
    private val localAuthProperties: LocalAuthProperties,
    private val authorityAggregateService: AuthorityAggregateService,
    private val userAggregateService: UserAggregateService,
    private val localSessionAuthenticationService: LocalSessionAuthenticationService,
    private val logger: Logger,
) : ApplicationRunner {
    override fun run(args: ApplicationArguments?) = runBlocking {
        try {
            val adminAuthority = ensureAuthority("admin", "Administrator access")
            ensureAuthority("publisher", "Publisher access")
            ensureAuthority("user", "User access")

            val adminEmail = localAuthProperties.bootstrapAdminEmail.trim().lowercase()
            val existingAdmin = userAggregateService.getByEmail(adminEmail)
            val adminUser = if (existingAdmin == null) {
                UserAggregate(
                    name = localAuthProperties.bootstrapAdminName,
                    login = adminEmail,
                    surname = null,
                    email = adminEmail,
                    phone = localAuthProperties.bootstrapAdminPhone,
                ).also {
                    it.addAuthority(adminAuthority.id)
                    userAggregateService.save(it)
                }
            } else {
                if (!existingAdmin.checkAdminAuthority()) {
                    existingAdmin.addAuthority(adminAuthority.id)
                    userAggregateService.save(existingAdmin)
                }
                existingAdmin
            }

            localSessionAuthenticationService.upsertCredential(
                userId = adminUser.id,
                email = adminEmail,
                password = localAuthProperties.bootstrapAdminPassword,
            )
        } catch (ex: Exception) {
            logger.error("Skipping bootstrap admin initialization because required database tables are not ready.", ex)
        }
    }

    private suspend fun ensureAuthority(name: String, description: String): Authority {
        val existing = authorityAggregateService.findByName(name)
        if (existing != null) {
            return existing
        }
        val authority = Authority(
            name = name,
            description = description,
            updatedAt = LocalDateTime.now(),
        )
        authorityAggregateService.save(authority)
        return authority
    }
}
