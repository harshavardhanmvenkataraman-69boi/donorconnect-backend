package com.donorconnect.donorservice.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI donorServiceOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("Donor Service API")
                        .description("API documentation for donor registration and management endpoints")
                        .version("v1")
                        .contact(new Contact().name("DonorConnect Team"))
                        .license(new License().name("Internal Use")));
    }
}

