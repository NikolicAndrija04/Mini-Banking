package com.minibanking.overview.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;

@Configuration
public class OpenApiConfiguration {

    @Bean
    OpenAPI overviewServiceOpenApi() {
        return new OpenAPI().info(new Info()
                .title("MiniBanking Overview Service API")
                .version("1.0")
                .description("Resilient aggregation across customer, account, card and transaction services"));
    }
}
