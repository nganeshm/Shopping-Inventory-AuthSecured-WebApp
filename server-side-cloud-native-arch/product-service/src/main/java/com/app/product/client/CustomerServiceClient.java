package com.app.product.client;

import com.app.product.dto.CustomerSnapshotDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;

/**
 * Declarative HTTP client: Spring Cloud OpenFeign generates the
 * implementation of this interface at startup. "customer-service" is the
 * Eureka application name (spring.application.name in customer-service's
 * application.yml) — Feign resolves it to a live instance via the registry,
 * so no hardcoded host:port appears anywhere in this code.
 */
@FeignClient(name = "customer-service")
public interface CustomerServiceClient {

    @GetMapping("/api/customers/{id}/snapshot")
    CustomerSnapshotDto getCustomerSnapshot(@PathVariable("id") Long id,
                                             @RequestHeader("Authorization") String authorization);
}
