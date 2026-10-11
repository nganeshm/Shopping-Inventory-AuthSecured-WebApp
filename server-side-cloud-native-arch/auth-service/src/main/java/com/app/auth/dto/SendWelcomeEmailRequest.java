package com.app.auth.dto;

public record SendWelcomeEmailRequest(
        String to,
        String username,
        String purpose,
        String subject
) {}
