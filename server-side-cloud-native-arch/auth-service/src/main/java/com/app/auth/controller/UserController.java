package com.app.auth.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/** Demonstrates protected + role-restricted endpoints once a JWT is attached. */
@RestController
@RequestMapping("/api/users")
public class UserController {

    /** Any authenticated user (any valid JWT) may call this. */
    @GetMapping("/me")
    public Map<String, Object> me(Authentication authentication) {
        return Map.of(
                "username", authentication.getName(),
                "authorities", authentication.getAuthorities()
        );
    }

    /**
     * @PreAuthorize evaluates a SpEL expression against the current
     * Authentication BEFORE the method body runs — method-level
     * authorization, enabled by @EnableMethodSecurity in SecurityConfig.
     */
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/admin-only")
    public String adminOnly() {
        return "Welcome, admin — this endpoint required ROLE_ADMIN.";
    }
}
