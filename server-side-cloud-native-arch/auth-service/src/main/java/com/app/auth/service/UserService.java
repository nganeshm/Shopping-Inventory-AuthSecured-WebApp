package com.app.auth.service;

import com.app.auth.dto.AuthRequest;
import com.app.auth.dto.AuthResponse;
import com.app.auth.dto.ForgotPasswordRequest;
import com.app.auth.dto.RegisterRequest;
import com.app.auth.dto.ResendOtpRequest;
import com.app.auth.dto.ResetPasswordRequest;
import com.app.auth.dto.VerifyEmailRequest;
import com.app.auth.entity.OtpPurpose;
import com.app.auth.entity.Role;
import com.app.auth.entity.User;
import com.app.auth.exception.EmailNotVerifiedException;
import com.app.auth.exception.InvalidOtpException;
import com.app.auth.exception.UserAlreadyExistsException;
import com.app.auth.repository.UserRepository;
import com.app.auth.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.Set;

/**
 * Identity use cases: register (pending email), verify, login, forgot/reset
 * password. OTP persistence and SMTP sit in other types (SRP).
 */
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final UserDetailsService userDetailsService;
    private final JwtUtil jwtUtil;
    private final OtpService otpService;
    private final EmailNotificationService emailNotificationService;
    private final CustomerOnboardingService customerOnboardingService;

    @Transactional
    public void register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.username())) {
            throw new UserAlreadyExistsException("Username already taken: " + request.username());
        }
        if (userRepository.existsByEmail(request.email())) {
            throw new UserAlreadyExistsException("Email already registered: " + request.email());
        }

        User user = User.builder()
                .username(request.username())
                .email(request.email().toLowerCase())
                .password(passwordEncoder.encode(request.password()))
                .roles(Set.of(Role.ROLE_USER))
                .enabled(false)
                .build();
        userRepository.save(user);
        customerOnboardingService.storePending(request);

        String otp = otpService.issue(user.getEmail(), OtpPurpose.REGISTRATION);
        emailNotificationService.sendOtp(user.getEmail(), user.getUsername(), OtpPurpose.REGISTRATION, otp);
    }

    @Transactional
    public void verifyEmail(VerifyEmailRequest request) {
        User user = userRepository.findByEmail(request.email().toLowerCase())
                .orElseThrow(() -> new InvalidOtpException("Invalid or expired OTP"));
        if (user.isEnabled()) {
            publishCustomerProfileAfterCommit(user.getEmail());
            return;
        }
        otpService.consume(user.getEmail(), OtpPurpose.REGISTRATION, request.otp());
        user.setEnabled(true);
        userRepository.save(user);
        afterVerified(user.getEmail(), user.getUsername(), true);
    }

    private void publishCustomerProfileAfterCommit(String email) {
        afterVerified(email, null, false);
    }

    private void afterVerified(String email, String username, boolean sendWelcome) {
        Runnable work = () -> {
            customerOnboardingService.publishAfterVerification(email);
            if (sendWelcome) {
                emailNotificationService.sendRegistrationSuccess(email, username);
            }
        };
        if (!TransactionSynchronizationManager.isSynchronizationActive()) {
            work.run();
            return;
        }
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                work.run();
            }
        });
    }

    @Transactional
    public void resendOtp(ResendOtpRequest request) {
        String email = request.email().toLowerCase();
        if (request.purpose() == OtpPurpose.REGISTRATION) {
            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new InvalidOtpException("Invalid or expired OTP"));
            if (user.isEnabled()) {
                throw new InvalidOtpException("Email is already verified");
            }
            String otp = otpService.issue(email, OtpPurpose.REGISTRATION);
            emailNotificationService.sendOtp(email, user.getUsername(), OtpPurpose.REGISTRATION, otp);
            return;
        }
        userRepository.findByEmail(email)
                .filter(User::isEnabled)
                .ifPresent(user -> {
                    String otp = otpService.issue(email, OtpPurpose.PASSWORD_RESET);
                    emailNotificationService.sendOtp(email, user.getUsername(), OtpPurpose.PASSWORD_RESET, otp);
                });
    }

    public void forgotPassword(ForgotPasswordRequest request) {
        String email = request.email().toLowerCase();
        userRepository.findByEmail(email)
                .filter(User::isEnabled)
                .ifPresent(user -> {
                    String otp = otpService.issue(email, OtpPurpose.PASSWORD_RESET);
                    emailNotificationService.sendOtp(email, user.getUsername(), OtpPurpose.PASSWORD_RESET, otp);
                });
    }

    @Transactional
    public void resetPassword(ResetPasswordRequest request) {
        User user = userRepository.findByEmail(request.email().toLowerCase())
                .orElseThrow(() -> new InvalidOtpException("Invalid or expired OTP"));
        if (!user.isEnabled()) {
            throw new EmailNotVerifiedException("Verify your email before resetting the password");
        }
        otpService.consume(user.getEmail(), OtpPurpose.PASSWORD_RESET, request.otp());
        user.setPassword(passwordEncoder.encode(request.newPassword()));
        userRepository.save(user);
    }

    public AuthResponse login(AuthRequest request) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.username(), request.password()));
        } catch (DisabledException ex) {
            throw new EmailNotVerifiedException("Verify your email before signing in");
        }

        UserDetails userDetails = userDetailsService.loadUserByUsername(request.username());
        String email = userRepository.findByUsername(request.username())
                .map(User::getEmail)
                .orElse(null);
        String token = jwtUtil.generateToken(userDetails, email);
        return new AuthResponse(token, "Bearer", jwtUtil.getExpirationMs());
    }
}
