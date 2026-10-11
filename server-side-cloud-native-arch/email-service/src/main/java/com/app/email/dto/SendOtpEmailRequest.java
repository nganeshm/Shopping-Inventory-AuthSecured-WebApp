package com.app.email.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

public record SendOtpEmailRequest(
        @NotBlank @Email String to,
        String username,
        @NotBlank String purpose,
        String subject,
        @NotBlank String otp,
        @Positive int ttlMinutes
) {}
