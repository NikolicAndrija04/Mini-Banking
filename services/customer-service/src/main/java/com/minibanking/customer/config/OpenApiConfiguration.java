package com.minibanking.customer.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;

@Configuration
public class OpenApiConfiguration {

    @Bean
    OpenAPI customerServiceOpenApi() {
        return new OpenAPI().info(new Info()
                .title("MiniBanking Customer Service API")
                .description("CRUD API for customer profiles and lifecycle management")
                .version("1.0.0")
                .contact(new Contact().name("MiniBanking Faculty Project")));
    }
}
