package com.movieapp.userservice.configurations;



import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.License;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;

@Configuration
@OpenAPIDefinition(
    info = @Info(
        title = "Movie App - User Service API",
        version = "1.0.0",
        description = "User management APIs including authentication, registration, and JWT-based authorization.",
        contact = @Contact(
            name = "Movie App Team",
            email = "support@movieapp.com"
        ),
        license = @License(
            name = "Internal Use Only"
        )
    ),
    security = @SecurityRequirement(name = "bearerAuth") // Apply globally
)
@SecurityScheme(
    name = "bearerAuth",
    description = "JWT Bearer Token Authentication",
    scheme = "bearer",
    type = SecuritySchemeType.HTTP,
    bearerFormat = "JWT",
    in = SecuritySchemeIn.HEADER
)
public class OpenAPIConfiguration {

}

