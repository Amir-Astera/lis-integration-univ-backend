package project.gigienist_reports.core.config

import io.swagger.v3.oas.annotations.enums.ParameterIn
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType
import io.swagger.v3.oas.annotations.security.OAuthFlow
import io.swagger.v3.oas.annotations.security.OAuthFlows
import io.swagger.v3.oas.annotations.security.SecurityScheme
import io.swagger.v3.oas.models.Components
import io.swagger.v3.oas.models.media.StringSchema
import io.swagger.v3.oas.models.parameters.Parameter
import org.springframework.context.annotation.Bean
import org.springdoc.core.customizers.OpenApiCustomizer
import org.springdoc.core.models.GroupedOpenApi
import org.springframework.context.annotation.Configuration

@SecurityScheme(
    name = "security_auth",
    type = SecuritySchemeType.OAUTH2,
    flows = OAuthFlows(
        clientCredentials = OAuthFlow(tokenUrl = "auth")
    ),
    bearerFormat = "JWT",
    scheme = "bearer"
)
class OpenApiConfig

//@SecurityScheme(
//    name = "security_auth",
//    type = SecuritySchemeType.OAUTH2,
//    flows = OAuthFlows(
//        clientCredentials = OAuthFlow(tokenUrl = "auth")
//    ),
//    bearerFormat = "JWT",
//    scheme = "bearer"
//)
//@Configuration
//class OpenApiConfig {
//
//    // Далее метод с OpenApiCustomiser, который глобально добавляет параметр `Cookie`:
//    @Bean
//    fun cookieOpenApiCustomizer(): OpenApiCustomizer {
//        return OpenApiCustomizer { openApi ->
//            val cookieParam = Parameter()
//                    .name("Cookie")
//                    .`in`(ParameterIn.HEADER.toString())
//                    .description("Укажите `SESSIONID=...` для cookie-аутентификации")
//                    .schema(StringSchema())
//                    .example("SESSIONID=eyJhbGciOiJIUz...")
//            if (openApi.components == null) {
//                openApi.components = Components()
//            }
//            openApi.components.addParameters("CookieParam", cookieParam)
//            openApi.paths?.forEach { (_, pathItem) ->
//                pathItem.readOperations().forEach { operation ->
//                    operation.addParametersItem(
//                            Parameter().`$ref`("#/components/parameters/CookieParam")
//                    )
//                }
//            }
//        }
//    }
//
//    @Bean
//    fun publicApi(): GroupedOpenApi {
//        return GroupedOpenApi.builder()
//                .group("public")
//                .pathsToMatch("/**")
//                .addOpenApiCustomizer(cookieOpenApiCustomizer())
//                .build()
//    }
//}
