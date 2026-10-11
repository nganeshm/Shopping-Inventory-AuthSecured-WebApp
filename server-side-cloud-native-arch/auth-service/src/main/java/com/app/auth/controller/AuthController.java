package com.app.auth.controller;

import com.app.auth.dto.AuthRequest;
import com.app.auth.dto.AuthResponse;
import com.app.auth.dto.ForgotPasswordRequest;
import com.app.auth.dto.RegisterRequest;
import com.app.auth.dto.ResendOtpRequest;
import com.app.auth.dto.ResetPasswordRequest;
import com.app.auth.dto.VerifyEmailRequest;
import com.app.auth.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;

    @PostMapping("/register")
    public ResponseEntity<String> register(@Valid @RequestBody RegisterRequest request) {
        userService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body("Registration started. Enter the OTP sent to your email to complete onboarding.");
    }

    @PostMapping("/verify-email")
    public ResponseEntity<String> verifyEmail(@Valid @RequestBody VerifyEmailRequest request) {
        userService.verifyEmail(request);
        return ResponseEntity.ok("Email verified. Customer profile created. You can sign in now.");
    }

    @PostMapping("/resend-otp")
    public ResponseEntity<String> resendOtp(@Valid @RequestBody ResendOtpRequest request) {
        userService.resendOtp(request);
        return ResponseEntity.ok("If the account can receive a code, a new OTP has been sent.");
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<String> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        userService.forgotPassword(request);
        return ResponseEntity.ok("If that email is registered, an OTP has been sent.");
    }

    @PostMapping("/reset-password")
    public ResponseEntity<String> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        userService.resetPassword(request);
        return ResponseEntity.ok("Password updated. You can sign in with the new password.");
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody AuthRequest request) {
        return ResponseEntity.ok(userService.login(request));
    }
}
