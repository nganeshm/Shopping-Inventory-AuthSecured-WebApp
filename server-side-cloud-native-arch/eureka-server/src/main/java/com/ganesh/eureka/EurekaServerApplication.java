package com.ganesh.eureka;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.server.EnableEurekaServer;

/**
 * The service registry for the whole platform.
 *
 * Every other microservice (auth-service, product-service, customer-service,
 * api-gateway) starts an Eureka CLIENT that registers itself here by name and
 * periodically sends heartbeats. Services look each other up by logical name
 * (e.g. "CUSTOMER-SERVICE") instead of hardcoded host:port — this is the
 * foundation that makes the system "cloud native": instances can scale up,
 * restart, or move hosts and callers keep working because they always ask
 * Eureka for the current address book.
 *
 * @EnableEurekaServer turns this otherwise-plain Spring Boot app into the
 * registry server itself (as opposed to a client of it).
 */
@SpringBootApplication
@EnableEurekaServer
public class EurekaServerApplication {
    public static void main(String[] args) {
        SpringApplication.run(EurekaServerApplication.class, args);
    }
}
