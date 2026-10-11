package com.app.email.controller;

import com.app.email.dto.SendOtpEmailRequest;
import com.app.email.dto.SendWelcomeEmailRequest;
import com.app.email.service.MailDispatchService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/emails")
@RequiredArgsConstructor
public class EmailController {

    private final MailDispatchService mailDispatchService;

    @PostMapping("/otp")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public void sendOtp(@Valid @RequestBody SendOtpEmailRequest request) {
        mailDispatchService.sendOtp(request);
    }

    @PostMapping("/welcome")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public void sendWelcome(@Valid @RequestBody SendWelcomeEmailRequest request) {
        mailDispatchService.sendWelcome(request);
    }
}
