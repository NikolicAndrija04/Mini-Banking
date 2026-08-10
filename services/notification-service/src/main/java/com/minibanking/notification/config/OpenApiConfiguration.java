package com.minibanking.notification.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;

@Configuration
public class OpenApiConfiguration {

    @Bean
    OpenAPI notificationServiceOpenApi() {
        return new OpenAPI().info(new Info()
                .title("MiniBanking Notification Service API")
                .version("1.0")
                .description("Persistent customer notifications with event-level deduplication"));
    }
}
