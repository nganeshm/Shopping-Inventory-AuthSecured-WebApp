package com.app.auth.entity;

/**
 * Why the OTP was issued. Stored as a string on otp_tokens.purpose.
 */
public enum OtpPurpose {
    REGISTRATION,
    PASSWORD_RESET
}
