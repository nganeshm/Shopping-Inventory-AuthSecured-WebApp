package com.app.customer.dto;

/**
 * The public, intentionally-minimal contract exposed to OTHER services
 * (today: product-service, via Feign) — a service-to-service "view" that
 * only reveals what callers legitimately need (name + default shipping
 * address) and nothing else about the customer (no email, no full address
 * history, no contact preferences). This is SOA's "service contract"
 * principle: the full CustomerResponse is for this service's own API
 * consumers; this slimmer shape is the deliberate, versioned inter-service
 * contract.
 */
public record CustomerSnapshotResponse(
        Long id,
        String firstName,
        String lastName,
        String defaultAddressLine1,
        String defaultAddressCity,
        String defaultAddressState,
        String defaultAddressPostalCode,
        String defaultAddressCountry
) {}
