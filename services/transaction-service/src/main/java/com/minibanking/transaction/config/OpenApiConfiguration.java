package com.minibanking.transaction.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;

@Configuration
public class OpenApiConfiguration {

    @Bean
    OpenAPI transactionServiceOpenApi() {
        return new OpenAPI().info(new Info()
                .title("MiniBanking Transaction Service API")
                .version("1.0")
                .description("Auditable and retry-safe bank transfer orchestration"));
    }
}
