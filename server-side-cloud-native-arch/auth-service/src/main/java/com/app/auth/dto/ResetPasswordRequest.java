package com.app.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ResetPasswordRequest(
        @NotBlank @Email String email,
        @NotBlank String otp,
        @NotBlank @Size(min = 6, message = "password must be at least 6 characters") String newPassword
) {}
