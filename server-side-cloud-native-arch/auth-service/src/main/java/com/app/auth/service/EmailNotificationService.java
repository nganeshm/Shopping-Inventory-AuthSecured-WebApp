package com.app.auth.service;

import com.app.auth.client.EmailServiceClient;
import com.app.auth.dto.SendOtpEmailRequest;
import com.app.auth.dto.SendWelcomeEmailRequest;
import com.app.auth.entity.OtpPurpose;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Isolates Feign + fallback logging from identity use cases. Subject and
 * username are supplied here so email-service can fill the HTML at send time.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class EmailNotificationService {

    private final EmailServiceClient emailServiceClient;
    private final OtpService otpService;

    public void sendOtp(String email, String username, OtpPurpose purpose, String otp) {
        String displayName = username == null || username.isBlank() ? "there" : username.trim();
        String subject = purpose == OtpPurpose.PASSWORD_RESET
                ? displayName + ", reset your E-Market Place password"
                : "Welcome " + displayName + " — verify your E-Market Place email";
        try {
            emailServiceClient.sendOtp(new SendOtpEmailRequest(
                    email, displayName, purpose.name(), subject, otp, otpService.ttlMinutes()));
        } catch (Exception ex) {
            log.warn("email-service unavailable; OTP for {} purpose={} is {}",
                    email, purpose, otp);
        }
    }

    public void sendRegistrationSuccess(String email, String username) {
        String displayName = username == null || username.isBlank() ? "there" : username.trim();
        try {
            emailServiceClient.sendWelcome(new SendWelcomeEmailRequest(
                    email,
                    displayName,
                    "REGISTRATION_SUCCESS",
                    "Congratulations " + displayName + " — you're in at E-Market Place"));
        } catch (Exception ex) {
            log.warn("email-service unavailable; welcome email for {} skipped", email);
        }
    }
}
