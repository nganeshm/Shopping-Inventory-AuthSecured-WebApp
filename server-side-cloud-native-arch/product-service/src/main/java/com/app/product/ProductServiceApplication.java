package com.app.product;

import com.app.product.config.ProductImportProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * Owns the product catalog domain: Category, Product, Inventory, Order,
 * OrderItem — i.e. "what can be bought, how much is in stock, and what has
 * been ordered."
 *
 * @EnableFeignClients scans for interfaces annotated @FeignClient (see
 * client/CustomerServiceClient) and generates their HTTP-calling
 * implementations at startup — this is how this service talks to
 * customer-service without writing any RestTemplate/WebClient boilerplate.
 */
@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients
@EnableConfigurationProperties(ProductImportProperties.class)
public class ProductServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(ProductServiceApplication.class, args);
    }
}
