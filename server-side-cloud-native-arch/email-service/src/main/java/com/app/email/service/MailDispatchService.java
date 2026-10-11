package com.app.email.service;

import com.app.email.dto.SendOtpEmailRequest;
import com.app.email.dto.SendWelcomeEmailRequest;
import com.app.email.template.EmailTemplateRenderer;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

/**
 * Builds the HTML body from the purpose-selected template, then hands it to SMTP.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class MailDispatchService {

    private final JavaMailSender mailSender;
    private final EmailTemplateRenderer templateRenderer;

    @Value("${email.from}")
    private String from;

    public void sendOtp(SendOtpEmailRequest request) {
        String subject = templateRenderer.resolveSubject(request);
        String html = templateRenderer.render(request, subject);
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(from);
            helper.setTo(request.to());
            helper.setSubject(subject);
            helper.setText(html, true);
            mailSender.send(message);
            log.info("OTP email sent to {} purpose={} subject={}",
                    request.to(), request.purpose(), subject);
        } catch (Exception ex) {
            log.warn("SMTP unavailable; OTP for {} purpose={} is {}",
                    request.to(), request.purpose(), request.otp());
        }
    }

    public void sendWelcome(SendWelcomeEmailRequest request) {
        String subject = templateRenderer.resolveWelcomeSubject(request);
        String html = templateRenderer.renderWelcome(request, subject);
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(from);
            helper.setTo(request.to());
            helper.setSubject(subject);
            helper.setText(html, true);
            mailSender.send(message);
            log.info("Welcome email sent to {} purpose={} subject={}",
                    request.to(), request.purpose(), subject);
        } catch (Exception ex) {
            log.warn("SMTP unavailable; welcome email for {} skipped", request.to());
        }
    }
}
