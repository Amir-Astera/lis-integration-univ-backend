package project.gigienist_reports.core.config.properties

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties("dev.auth")
data class LocalAuthProperties(
    val bootstrapAdminEmail: String,
    val bootstrapAdminPassword: String,
    val bootstrapAdminName: String,
    val bootstrapAdminPhone: String,
)
