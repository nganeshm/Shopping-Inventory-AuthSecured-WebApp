package com.app.auth.dto;

public record SendOtpEmailRequest(
        String to,
        String username,
        String purpose,
        String subject,
        String otp,
        int ttlMinutes
) {}
