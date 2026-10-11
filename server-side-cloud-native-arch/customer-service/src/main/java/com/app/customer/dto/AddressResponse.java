package com.app.customer.dto;

public record AddressResponse(
        Long id,
        String addressType,
        String line1,
        String line2,
        String city,
        String state,
        String postalCode,
        String country,
        boolean defaultAddress
) {}
