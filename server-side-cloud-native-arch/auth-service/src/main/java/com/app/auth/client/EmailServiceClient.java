package com.app.auth.client;

import com.app.auth.dto.SendOtpEmailRequest;
import com.app.auth.dto.SendWelcomeEmailRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * auth-service never talks to SMTP. email-service is resolved via Eureka.
 */
@FeignClient(name = "email-service", configuration = EmailServiceFeignConfig.class)
public interface EmailServiceClient {

    @PostMapping("/api/emails/otp")
    void sendOtp(@RequestBody SendOtpEmailRequest request);

    @PostMapping("/api/emails/welcome")
    void sendWelcome(@RequestBody SendWelcomeEmailRequest request);
}
