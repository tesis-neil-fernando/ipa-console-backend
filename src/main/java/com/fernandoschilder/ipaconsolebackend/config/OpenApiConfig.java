package com.fernandoschilder.ipaconsolebackend.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;

/**
 * OpenAPI configuration to enable the Swagger UI "Authorize" button for JWT Bearer tokens.
 *
 * After adding this class Springdoc will expose a security scheme named "bearerAuth" and
 * apply it as a global requirement. Use the Swagger UI -> Authorize -> value: "Bearer {token}".
 */
@OpenAPIDefinition(
        info = @Info(title = "IPA Console Backend API", version = "v1"),
        security = @SecurityRequirement(name = "bearerAuth")
)
@SecurityScheme(
        name = "bearerAuth",
        type = SecuritySchemeType.HTTP,
        scheme = "bearer",
        bearerFormat = "JWT"
)
public class OpenApiConfig {
    // Marker configuration class - annotations above are sufficient for springdoc
}
