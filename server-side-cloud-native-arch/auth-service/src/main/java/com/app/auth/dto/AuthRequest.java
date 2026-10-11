package com.app.auth.dto;

import jakarta.validation.constraints.NotBlank;

/** Inbound payload for POST /api/auth/login. */
public record AuthRequest(
        @NotBlank(message = "username is required") String username,
        @NotBlank(message = "password is required") String password
) {}
