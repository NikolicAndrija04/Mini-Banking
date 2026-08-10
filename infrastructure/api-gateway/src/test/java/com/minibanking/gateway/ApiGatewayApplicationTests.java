package com.minibanking.gateway;

import java.time.Duration;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.gateway.route.RouteDefinition;
import org.springframework.cloud.gateway.route.RouteDefinitionLocator;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {
                "spring.cloud.config.enabled=false",
                "eureka.client.enabled=false"
        }
)
class ApiGatewayApplicationTests {

    @Autowired
    private RouteDefinitionLocator routeDefinitionLocator;

    @Test
    void loadsAllBusinessRoutesFromYaml() {
        List<RouteDefinition> routes = routeDefinitionLocator.getRouteDefinitions()
                .collectList()
                .block(Duration.ofSeconds(5));

        assertThat(routes)
                .isNotNull()
                .extracting(RouteDefinition::getId)
                .containsExactlyInAnyOrder(
                        "customer-service",
                        "account-service",
                        "transaction-service",
                        "card-service",
                        "notification-service",
                        "overview-service"
                );
    }
}
