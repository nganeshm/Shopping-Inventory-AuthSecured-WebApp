package com.app.customer.dto;

import com.app.customer.entity.AddressType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record AddressRequest(
        @NotNull(message = "addressType is required")
        AddressType addressType,

        @NotBlank(message = "line1 is required")
        String line1,

        String line2,

        @NotBlank(message = "city is required")
        String city,

        @NotBlank(message = "state is required")
        String state,

        @NotBlank(message = "postalCode is required")
        String postalCode,

        @NotBlank(message = "country is required")
        String country,

        boolean defaultAddress
) {}
