package com.app.customer.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CustomerRequest(
        @NotBlank(message = "firstName is required")
        String firstName,

        @NotBlank(message = "lastName is required")
        String lastName,

        @NotBlank(message = "email is required")
        @Email(message = "email must be valid")
        String email,

        @NotNull(message = "at least one address is required")
        @Valid
        AddressRequest primaryAddress,

        @NotNull(message = "contact detail is required")
        @Valid
        ContactDetailRequest contactDetail
) {}
