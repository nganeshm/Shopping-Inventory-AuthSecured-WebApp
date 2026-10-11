package com.app.product.dto;

/**
 * Mirrors the subset of customer-service's CustomerResponse that
 * product-service actually needs for placing an order (its default shipping
 * address). This is a classic Feign-client-side DTO: product-service defines
 * its OWN copy of the contract it depends on, rather than sharing a jar of
 * entities with customer-service — keeping the two independently deployable
 * (changing a field customer-service doesn't expose here can't break this
 * service at compile time).
 */
public record CustomerSnapshotDto(
        Long id,
        String firstName,
        String lastName,
        String defaultAddressLine1,
        String defaultAddressCity,
        String defaultAddressState,
        String defaultAddressPostalCode,
        String defaultAddressCountry
) {}
