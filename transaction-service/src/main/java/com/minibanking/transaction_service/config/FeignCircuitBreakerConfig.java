package com.minibanking.transaction_service.config;

import feign.Target;
import org.springframework.cloud.openfeign.CircuitBreakerNameResolver;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.lang.reflect.Method;

@Configuration
public class FeignCircuitBreakerConfig {

    @Bean
    public CircuitBreakerNameResolver circuitBreakerNameResolver() {
        return (String feignClientName, Target<?> target, Method method) -> {
            if ("account-service".equals(feignClientName)) {
                return "accountService";
            }

            return target.type().getSimpleName() + method.getName();
        };
    }
}