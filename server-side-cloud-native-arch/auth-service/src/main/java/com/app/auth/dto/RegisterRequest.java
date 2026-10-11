package com.app.auth.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Identity fields are required. Profile/address are optional so existing
 * clients can still register; they are copied into customer-service after OTP.
 */
public record RegisterRequest(

        @NotBlank(message = "username is required")
        @Size(min = 3, max = 50, message = "username must be 3-50 characters")
        String username,

        @NotBlank(message = "email is required")
        @Email(message = "email must be a valid address")
        String email,

        @NotBlank(message = "password is required")
        @Size(min = 6, message = "password must be at least 6 characters")
        String password,

        @JsonAlias({"first_name"})
        String firstName,

        @JsonAlias({"last_name"})
        String lastName,

        @Valid
        AddressDetailsRequest address,

        @Valid
        ContactDetailsRequest contact
) {}
