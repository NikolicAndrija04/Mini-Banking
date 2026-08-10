package com.minibanking.card.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;

@Configuration
public class OpenApiConfiguration {

    @Bean
    OpenAPI cardServiceOpenApi() {
        return new OpenAPI().info(new Info()
                .title("MiniBanking Card Service API")
                .version("1.0")
                .description("Secure card issuance and lifecycle management without storing raw PAN or CVV"));
    }
}
