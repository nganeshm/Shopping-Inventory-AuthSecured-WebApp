package com.app.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record ContactDetailsRequest(
        @NotBlank(message = "primaryPhone is required")
        @Pattern(regexp = "^[+]?[0-9\\-() ]{7,20}$", message = "primaryPhone must be a valid phone number")
        String primaryPhone,
        String secondaryPhone,
        String alternateEmail,
        String preferredContactMethod
) {}
