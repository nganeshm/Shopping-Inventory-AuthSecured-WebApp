package com.app.auth.service;

import com.app.auth.entity.OtpPurpose;
import com.app.auth.entity.OtpToken;
import com.app.auth.exception.InvalidOtpException;
import com.app.auth.repository.OtpTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

/**
 * Issues and checks OTPs. Hashing and expiry live here so UserService only
 * orchestrates register / login / reset.
 */
@Service
@RequiredArgsConstructor
public class OtpService {

    private static final SecureRandom RANDOM = new SecureRandom();

    private final OtpTokenRepository otpTokenRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${email.otp-ttl-minutes:10}")
    private int ttlMinutes;

    @Transactional
    public String issue(String email, OtpPurpose purpose) {
        otpTokenRepository.markUnusedAsUsed(email.toLowerCase(), purpose);
        String otp = String.format("%06d", RANDOM.nextInt(1_000_000));
        otpTokenRepository.save(OtpToken.of(
                email.toLowerCase(),
                passwordEncoder.encode(otp),
                purpose,
                Instant.now().plus(ttlMinutes, ChronoUnit.MINUTES)));
        return otp;
    }

    @Transactional
    public void consume(String email, OtpPurpose purpose, String otp) {
        List<OtpToken> tokens = otpTokenRepository
                .findByEmailAndPurposeAndUsedFalseOrderByCreatedAtDesc(email.toLowerCase(), purpose);
        OtpToken match = tokens.stream()
                .filter(token -> !token.isExpired())
                .filter(token -> passwordEncoder.matches(otp, token.getCodeHash()))
                .findFirst()
                .orElseThrow(() -> new InvalidOtpException("Invalid or expired OTP"));
        match.setUsed(true);
        otpTokenRepository.save(match);
    }

    public int ttlMinutes() {
        return ttlMinutes;
    }
}
