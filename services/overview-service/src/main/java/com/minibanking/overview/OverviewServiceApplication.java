package com.minibanking.overview;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@EnableFeignClients
@SpringBootApplication
public class OverviewServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(OverviewServiceApplication.class, args);
    }
}
