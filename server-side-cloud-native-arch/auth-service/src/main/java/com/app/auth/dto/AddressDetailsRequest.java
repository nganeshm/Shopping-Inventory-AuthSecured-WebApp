package com.app.auth.dto;

import jakarta.validation.constraints.NotBlank;

public record AddressDetailsRequest(
        String addressType,
        @NotBlank(message = "line1 is required") String line1,
        String line2,
        @NotBlank(message = "city is required") String city,
        @NotBlank(message = "state is required") String state,
        @NotBlank(message = "postalCode is required") String postalCode,
        @NotBlank(message = "country is required") String country
) {}
