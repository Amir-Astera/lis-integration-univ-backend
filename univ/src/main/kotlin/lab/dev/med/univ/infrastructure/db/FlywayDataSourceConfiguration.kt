package lab.dev.med.univ.infrastructure.db

import org.flywaydb.core.Flyway
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.LazyInitializationExcludeFilter
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.autoconfigure.flyway.FlywayMigrationInitializer
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.jdbc.datasource.DriverManagerDataSource
import javax.sql.DataSource

/**
 * The application talks to PostgreSQL through R2DBC, which suppresses JDBC
 * DataSource auto-configuration. Flyway still requires a JDBC DataSource
 * before its own auto-configuration can run.
 */
@Configuration
@ConditionalOnProperty(prefix = "spring.flyway", name = ["url"])
class FlywayDataSourceConfiguration(
    @Value("\${spring.flyway.url}") private val url: String,
    @Value("\${spring.flyway.user}") private val user: String,
    @Value("\${spring.flyway.password}") private val password: String,
) {
    @Bean
    fun flywayDataSource(): DataSource = DriverManagerDataSource(url, user, password)
}

/**
 * Kept separate and unproxied so Spring can construct the filter while it is
 * still deciding which beans stay eager. Production uses lazy initialization.
 */
@Configuration(proxyBeanMethods = false)
class FlywayEagerConfiguration {
    @Bean
    fun flywayLazyInitializationExcludeFilter(): LazyInitializationExcludeFilter =
        LazyInitializationExcludeFilter.forBeanTypes(
            Flyway::class.java,
            FlywayMigrationInitializer::class.java,
        )
}
