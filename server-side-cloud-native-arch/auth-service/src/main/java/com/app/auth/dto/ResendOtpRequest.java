package com.app.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import com.app.auth.entity.OtpPurpose;

public record ResendOtpRequest(
        @NotBlank @Email String email,
        @NotNull OtpPurpose purpose
) {}
