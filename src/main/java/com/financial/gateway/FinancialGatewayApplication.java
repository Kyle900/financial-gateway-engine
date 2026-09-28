package com.financial.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class FinancialGatewayApplication {
    public static void main(String[] args) {
        SpringApplication.run(FinancialGatewayApplication.class, args);
    }
}
