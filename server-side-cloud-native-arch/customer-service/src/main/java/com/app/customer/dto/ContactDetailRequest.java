package com.app.customer.dto;

import com.app.customer.entity.ContactMethod;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record ContactDetailRequest(
        @NotBlank(message = "primaryPhone is required")
        @Pattern(regexp = "^[+]?[0-9\\-() ]{7,20}$", message = "primaryPhone must be a valid phone number")
        String primaryPhone,

        @Pattern(regexp = "^[+]?[0-9\\-() ]{7,20}$", message = "secondaryPhone must be a valid phone number")
        String secondaryPhone,

        String alternateEmail,

        ContactMethod preferredContactMethod
) {}
