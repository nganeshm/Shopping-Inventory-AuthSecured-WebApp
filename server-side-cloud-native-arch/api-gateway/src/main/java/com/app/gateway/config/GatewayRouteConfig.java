package com.app.gateway.config;

import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GatewayRouteConfig {

    @Bean
    public RouteLocator routeLocator(RouteLocatorBuilder builder) {
        return builder.routes()
                .route("auth-service", r -> r.path("/api/auth/**")
                        .uri("lb://auth-service"))

                .route("auth-service-users", r -> r.path("/api/users/**")
                        .uri("lb://auth-service"))

                .route("customer-service", r -> r.path("/api/customers/**")
                        .uri("lb://customer-service"))

                .route("product-service", r -> r.path("/api/products/**", "/api/categories/**", "/api/inventory/**")
                        .filters(f -> f.circuitBreaker(c -> c
                                .setName("productServiceCB")
                                .setFallbackUri("forward:/fallback/product-service")))
                        .uri("lb://product-service"))

                .route("order-service", r -> r.path("/api/orders/**")
                        .filters(f -> f.circuitBreaker(c -> c
                                .setName("productServiceCB") // orders live inside product-service too
                                .setFallbackUri("forward:/fallback/product-service")))
                        .uri("lb://product-service"))

                .build();
        //ThisIsADemoSecretKeyForJWTSigningPleaseChangeInProd1234567890  -- IMP Config Key for Test
    }
}
