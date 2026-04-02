package project.gigienist_reports.core.config

import project.gigienist_reports.core.config.properties.SecurityProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.reactive.CorsConfigurationSource
import org.springframework.web.cors.reactive.CorsWebFilter
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource

@Configuration
class CorsConfig(
        private val securityProperties: SecurityProperties
) {


    @Bean
    fun corsConfigurationSource(): CorsConfigurationSource {
        val cors = CorsConfiguration().apply {
            allowCredentials = securityProperties.allowCredentials
            // ВАЖНО: без завершающего слэша и ровно https
//            addAllowedOrigin("https://gigienist-reports.kz")
            allowedOrigins = securityProperties.allowedOrigins
            // addAllowedOrigin("*")

            // проще не промахнуться — разрешаем все методы и заголовки
            allowedMethods = securityProperties.allowedMethods
            allowedHeaders = securityProperties.allowedHeaders
            exposedHeaders = securityProperties.exposedHeaders
            maxAge = 3600
        }
        return UrlBasedCorsConfigurationSource().apply {
            registerCorsConfiguration("/**", cors)
        }
    }

    @Bean
    fun corsWebFilter(source: CorsConfigurationSource) = CorsWebFilter(source)

//    @Bean
//    fun corsConfigurationSource(): CorsConfigurationSource {
//        val corsConfig = CorsConfiguration()
//        corsConfig.allowedOrigins = listOf("https://gigienist-reports.kz")  // укажите точный адрес
//        corsConfig.allowedMethods = listOf(*securityProperties.allowedMethods.toTypedArray())
//        corsConfig.allowedHeaders = listOf(*securityProperties.allowedHeaders.toTypedArray())
//        corsConfig.exposedHeaders = listOf(*securityProperties.exposedHeaders.toTypedArray())
//        corsConfig.allowCredentials = true
//        corsConfig.maxAge = 3600
//
//        val source = UrlBasedCorsConfigurationSource()
//        source.registerCorsConfiguration("/**", corsConfig)
//        return source
//    }
//
//    @Bean
//    fun corsWebFilter(corsConfigurationSource: CorsConfigurationSource): CorsWebFilter {
//        return CorsWebFilter(corsConfigurationSource)
//    }
}

//    override fun addCorsMappings(corsRegistry: CorsRegistry) {
//        corsRegistry.addMapping("/**")
//            .allowedMethods("*")
//                .allowedOrigins("http://localhost:5173")//(*securityProperties.allowedOrigins.toTypedArray())
//                .allowedHeaders(*securityProperties.allowedHeaders.toTypedArray())
//                .allowedMethods(*securityProperties.allowedMethods.toTypedArray())
//                .exposedHeaders(*securityProperties.exposedHeaders.toTypedArray())
//                .allowCredentials(true)
//                .maxAge(3600)
//    }
