package project.gigienist_reports.core.config

import project.gigienist_reports.core.config.properties.SecurityProperties
import project.gigienist_reports.core.security.UnauthorizedAuthenticationEntryPoint
import project.gigienist_reports.core.security.local.LocalTokenAuthenticationManager
import project.gigienist_reports.feature.authorization.domain.services.LocalSessionAuthenticationService
import org.springframework.boot.actuate.autoconfigure.security.reactive.EndpointRequest
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpMethod
import org.springframework.security.authentication.ReactiveAuthenticationManager
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity
import org.springframework.security.config.web.server.SecurityWebFiltersOrder
import org.springframework.security.config.web.server.ServerHttpSecurity
import org.springframework.security.web.server.SecurityWebFilterChain
import org.springframework.security.web.server.authentication.AuthenticationWebFilter
import org.springframework.security.web.server.context.WebSessionServerSecurityContextRepository
import org.springframework.web.cors.reactive.CorsConfigurationSource
import org.springframework.web.reactive.config.WebFluxConfigurer
import lab.dev.med.univ.feature.agent.domain.AgentSecurityFilter
import project.gigienist_reports.core.security.TokenAuthenticationConverter
import project.gigienist_reports.core.security.firebase.FirebaseHeadersExchangeMatcher

// Firebase Auth removed. All profiles now use LocalSessionAuthenticationService.
// To enable Firebase: activate the "firebase" Spring profile.
//
// /api/agent/** is protected by AgentSecurityFilter (X-Agent-Key header) and
// must be excluded from the Firebase/session filter chain.

@Configuration
@EnableWebFluxSecurity
@EnableMethodSecurity(securedEnabled = true, jsr250Enabled = true, prePostEnabled = true)
class SecurityConfig(
    private val securityProperties: SecurityProperties,
) : WebFluxConfigurer {

    @Bean
    fun springSecurityFilterChain(
        http: ServerHttpSecurity,
        entryPoint: UnauthorizedAuthenticationEntryPoint,
        authWebFilter: AuthenticationWebFilter,
        agentSecurityFilter: AgentSecurityFilter,
        corsSource: CorsConfigurationSource,
    ): SecurityWebFilterChain {
        http.csrf { it.disable() }
            .formLogin { it.disable() }
            .logout { it.disable() }
            .httpBasic { it.disable() }
            .cors { it.configurationSource(corsSource) }

        http.exceptionHandling { it.authenticationEntryPoint(entryPoint) }
            .authorizeExchange { it.pathMatchers(HttpMethod.OPTIONS).permitAll() }
            .authorizeExchange { it.pathMatchers(*securityProperties.allowedPublicApis.toTypedArray()).permitAll() }
            // Agent paths are handled exclusively by AgentSecurityFilter — permit at chain level.
            .authorizeExchange { it.pathMatchers("/api/agent/**").permitAll() }
            .authorizeExchange { it.pathMatchers("/").permitAll() }
            .authorizeExchange { it.matchers(EndpointRequest.toAnyEndpoint()).authenticated() }
            .addFilterAt(authWebFilter, SecurityWebFiltersOrder.AUTHORIZATION)
            // AgentSecurityFilter runs before AUTHORIZATION so it can set the SecurityContext.
            .addFilterBefore(agentSecurityFilter, SecurityWebFiltersOrder.AUTHENTICATION)
            .authorizeExchange { it.anyExchange().authenticated() }

        return http.build()
    }

    @Bean
    fun authWebFilter(authenticationManager: ReactiveAuthenticationManager): AuthenticationWebFilter {
        val authenticationWebFilter = AuthenticationWebFilter(authenticationManager)
        authenticationWebFilter.setServerAuthenticationConverter(TokenAuthenticationConverter())
        authenticationWebFilter.setRequiresAuthenticationMatcher(FirebaseHeadersExchangeMatcher())
        authenticationWebFilter.setSecurityContextRepository(WebSessionServerSecurityContextRepository())
        return authenticationWebFilter
    }

    @Bean
    fun authenticationManager(
        localSessionAuthenticationService: LocalSessionAuthenticationService,
    ): ReactiveAuthenticationManager {
        return LocalTokenAuthenticationManager(localSessionAuthenticationService)
    }
}
