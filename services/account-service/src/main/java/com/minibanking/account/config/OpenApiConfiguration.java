package com.minibanking.account.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;

@Configuration
public class OpenApiConfiguration {

    @Bean
    OpenAPI accountServiceOpenApi() {
        return new OpenAPI().info(new Info()
                .title("MiniBanking Account Service API")
                .version("1.0")
                .description("Account lifecycle, balance operations and atomic idempotent transfers"));
    }
}
