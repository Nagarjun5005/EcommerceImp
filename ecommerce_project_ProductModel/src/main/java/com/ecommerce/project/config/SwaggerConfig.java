package com.ecommerce.project.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Swagger Configuration for JWT Bearer Authentication.
 *
 * This class customizes the OpenAPI specification (used by Swagger UI)
 * so that secured endpoints can be tested directly with JWT tokens.
 *
 * Once configured, Swagger UI will show an "Authorize" button,
 * where you can enter your JWT in the format:
 *     Bearer <your-jwt-token>
 */
@Configuration
public class SwaggerConfig {

    /**
     * Registers a custom OpenAPI bean that adds JWT Bearer authentication support.
     *
     * @return an OpenAPI instance with security scheme and requirement configured.
     */
    @Bean
    public OpenAPI customOpenApi() {

        // Define a Security Scheme — describes how Swagger should authenticate requests
        SecurityScheme securityScheme = new SecurityScheme()
                .type(SecurityScheme.Type.HTTP)      // HTTP Authentication
                .scheme("bearer")                    // Use "bearer" token scheme
                .bearerFormat("JWT")                 // Token format is JWT
                .description("Enter JWT Bearer token (e.g., Bearer eyJhbGciOiJIUzI1Ni...)");

        // Define Security Requirement — applies the above scheme globally
        SecurityRequirement bearerRequirement = new SecurityRequirement()
                .addList("Bearer Authentication");

        // Build and return OpenAPI object with security setup
        return new OpenAPI()
                .components(new Components()
                        .addSecuritySchemes("Bearer Authentication", securityScheme))
                .addSecurityItem(bearerRequirement);
    }
}
