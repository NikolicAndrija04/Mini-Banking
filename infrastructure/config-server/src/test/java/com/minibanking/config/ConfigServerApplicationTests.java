package com.minibanking.config;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {
                "eureka.client.enabled=false",
                "spring.cloud.config.server.native.search-locations=classpath:/test-config"
        }
)
class ConfigServerApplicationTests {

    @Test
    void contextLoads() {
    }
}
