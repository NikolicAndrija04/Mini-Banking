package com.minibanking.gateway;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.cloud.gateway.route.RouteDefinition;
import org.springframework.cloud.gateway.route.RouteDefinitionLocator;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpHeaders;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {
                "spring.cloud.config.enabled=false",
                "eureka.client.enabled=false",
                "spring.security.oauth2.resourceserver.jwt.issuer-uri=http://localhost:9090/realms/minibanking",
                "spring.security.oauth2.resourceserver.jwt.jwk-set-uri=http://localhost:1/not-used"
        }
)
class ApiGatewayApplicationTests {

    @Autowired
    private RouteDefinitionLocator routeDefinitionLocator;

    @LocalServerPort
    private int port;

    @MockitoBean
    private ReactiveJwtDecoder jwtDecoder;

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

    @Test
    void allowsPublicHealthButRejectsAnonymousBusinessRequest() {
        WebTestClient client = WebTestClient.bindToServer().baseUrl("http://localhost:" + port).build();

        client.get().uri("/actuator/health").exchange().expectStatus().isOk();
        client.get().uri("/api/accounts").exchange().expectStatus().isUnauthorized();
    }

    @Test
    void preventsCustomerRoleFromDeletingBusinessData() {
        Jwt customerJwt = new Jwt(
                "customer-token",
                Instant.now(),
                Instant.now().plusSeconds(300),
                Map.of("alg", "none"),
                Map.of(
                        "sub", "demo-customer",
                        "realm_access", Map.of("roles", List.of("CUSTOMER"))
                )
        );
        when(jwtDecoder.decode("customer-token")).thenReturn(Mono.just(customerJwt));
        WebTestClient client = WebTestClient.bindToServer()
                .baseUrl("http://localhost:" + port)
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer customer-token")
                .build();

        client.method(HttpMethod.DELETE)
                .uri("/api/customers/" + java.util.UUID.randomUUID())
                .exchange()
                .expectStatus().isForbidden();
    }
}
