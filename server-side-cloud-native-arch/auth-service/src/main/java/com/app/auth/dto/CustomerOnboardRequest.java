package com.app.auth.dto;

/**
 * auth-service's copy of customer-service's register contract.
 * Same JSON shape; no shared jar (SOA).
 */
public record CustomerOnboardRequest(
        String firstName,
        String lastName,
        String email,
        CustomerAddressPayload primaryAddress,
        CustomerContactPayload contactDetail
) {
    public record CustomerAddressPayload(
            String addressType,
            String line1,
            String line2,
            String city,
            String state,
            String postalCode,
            String country,
            boolean defaultAddress
    ) {}

    public record CustomerContactPayload(
            String primaryPhone,
            String secondaryPhone,
            String alternateEmail,
            String preferredContactMethod
    ) {}
}
