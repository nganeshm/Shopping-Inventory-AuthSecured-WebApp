package com.app.email.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record SendWelcomeEmailRequest(
        @NotBlank @Email String to,
        String username,
        @NotBlank String purpose,
        String subject
) {}
