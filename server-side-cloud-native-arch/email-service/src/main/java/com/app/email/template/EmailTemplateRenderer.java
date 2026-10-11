package com.app.email.template;

import com.app.email.dto.SendOtpEmailRequest;
import com.app.email.dto.SendWelcomeEmailRequest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Loads the HTML chosen for the purpose and substitutes runtime fields.
 * Inline placeholders keep the templates free of a template engine.
 */
@Component
public class EmailTemplateRenderer {

    public String render(SendOtpEmailRequest request, String subject) {
        Map<String, String> values = new LinkedHashMap<>();
        values.put("{{username}}", escape(blankTo(request.username(), "there")));
        values.put("{{email}}", escape(request.to()));
        values.put("{{otp}}", escape(request.otp()));
        values.put("{{ttlMinutes}}", String.valueOf(request.ttlMinutes()));
        values.put("{{subject}}", escape(subject));
        return apply(EmailTemplateType.fromPurpose(request.purpose()), values);
    }

    public String renderWelcome(SendWelcomeEmailRequest request, String subject) {
        Map<String, String> values = new LinkedHashMap<>();
        values.put("{{username}}", escape(blankTo(request.username(), "there")));
        values.put("{{email}}", escape(request.to()));
        values.put("{{subject}}", escape(subject));
        return apply(EmailTemplateType.fromPurpose(request.purpose()), values);
    }

    public String resolveSubject(SendOtpEmailRequest request) {
        return resolveSubject(request.purpose(), request.username(), request.subject());
    }

    public String resolveWelcomeSubject(SendWelcomeEmailRequest request) {
        return resolveSubject(request.purpose(), request.username(), request.subject());
    }

    private String resolveSubject(String purpose, String username, String subject) {
        if (subject != null && !subject.isBlank()) {
            return subject.trim();
        }
        return EmailTemplateType.fromPurpose(purpose).subjectFor(blankTo(username, "there"));
    }

    private String apply(EmailTemplateType type, Map<String, String> values) {
        String rendered = load(type.resourceName());
        for (Map.Entry<String, String> entry : values.entrySet()) {
            rendered = rendered.replace(entry.getKey(), entry.getValue());
        }
        return rendered;
    }

    private static String load(String resourceName) {
        try {
            ClassPathResource resource = new ClassPathResource("templates/" + resourceName);
            return resource.getContentAsString(StandardCharsets.UTF_8);
        } catch (IOException ex) {
            throw new IllegalStateException("Email template missing: " + resourceName, ex);
        }
    }

    private static String blankTo(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value.trim();
    }

    private static String escape(String value) {
        if (value == null) {
            return "";
        }
        return value
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;");
    }
}
