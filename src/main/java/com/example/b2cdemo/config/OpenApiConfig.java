package com.example.b2cdemo.config;

import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configures SpringDoc OpenAPI with a Bearer JWT security scheme.
 * Access Swagger UI at: /swagger-ui.html
 */
@Configuration
@SecurityScheme(
        name       = "bearerAuth",
        type       = SecuritySchemeType.HTTP,
        scheme     = "bearer",
        bearerFormat = "JWT",
        description = "Paste your Azure AD B2C access token (without the 'Bearer ' prefix)"
)
public class OpenApiConfig {

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Azure B2C Resource Server API")
                        .version("1.0.0")
                        .description("""
                                Spring Boot 3.x REST API protected by Azure AD B2C.

                                **Required scopes:** `products.read`, `products.write`
                                **Required roles:** `Admin` (Azure App Role)

                                Obtain a token from your B2C tenant and paste it into the 'Authorize' button.
                                """)
                        .license(new License().name("MIT"))
                );
    }
}
