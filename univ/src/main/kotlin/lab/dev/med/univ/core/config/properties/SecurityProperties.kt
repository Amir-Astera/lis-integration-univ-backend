package project.gigienist_reports.core.config.properties

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties("security")
data class SecurityProperties(
        val cookieProps: CookieProperties,
        val firebaseProps: FirebaseProperties,
        val allowCredentials: Boolean,
        val allowedOrigins: List<String>,
        val allowedHeaders: List<String>,
        val exposedHeaders: List<String>,
        val allowedMethods: List<String>,
        val allowedPublicApis: List<String>
)