package com.app.email;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * Standalone email bounded context. auth-service (and later others) send
 * OTPs through this service's HTTP API; they never hold SMTP credentials.
 */
@SpringBootApplication
@EnableDiscoveryClient
public class EmailServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(EmailServiceApplication.class, args);
    }
}
