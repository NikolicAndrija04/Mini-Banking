package com.minibanking.gateway.security;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.jwt.Jwt;

import static org.assertj.core.api.Assertions.assertThat;

class KeycloakJwtRoleConverterTests {

    @Test
    void mapsRealmAndClientRolesToSpringAuthorities() {
        Jwt jwt = new Jwt(
                "token",
                Instant.now(),
                Instant.now().plusSeconds(300),
                Map.of("alg", "none"),
                Map.of(
                        "sub", "demo-user",
                        "realm_access", Map.of("roles", List.of("customer")),
                        "resource_access", Map.of(
                                "minibanking-cli",
                                Map.of("roles", List.of("admin"))
                        )
                )
        );

        assertThat(new KeycloakJwtRoleConverter("minibanking-cli").convert(jwt))
                .extracting("authority")
                .contains("ROLE_CUSTOMER", "ROLE_ADMIN");
    }
}
