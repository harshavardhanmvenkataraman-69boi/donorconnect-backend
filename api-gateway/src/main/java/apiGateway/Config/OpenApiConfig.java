package apiGateway.Config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * OpenAPI (Swagger) Configuration for API Gateway
 * Aggregates docs from all microservices via springdoc-openapi-starter-webflux-ui
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("DonorConnect API Gateway")
                        .version("1.0.0")
                        .description("API Gateway for DonorConnect Blood Management System. " +
                                "This gateway aggregates OpenAPI documentation from all microservices.")
                        .contact(new Contact()
                                .name("DonorConnect Team")
                                .email("support@donorconnect.com")));
    }
}

