package com.app.email.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * email-service is not on the public gateway. Callers must present the
 * shared internal key so a random client on :8084 cannot send mail.
 */
@Component
public class InternalKeyFilter extends OncePerRequestFilter {

    @Value("${email.internal-key}")
    private String expectedKey;

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return request.getRequestURI().startsWith("/actuator");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String provided = request.getHeader("X-Internal-Key");
        if (expectedKey.equals(provided)) {
            filterChain.doFilter(request, response);
            return;
        }
        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.setContentType("application/json");
        response.getOutputStream().write("{\"error\":\"Missing or invalid internal key\"}".getBytes(StandardCharsets.UTF_8));
    }
}
