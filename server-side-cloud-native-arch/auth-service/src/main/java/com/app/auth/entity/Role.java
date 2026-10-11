package com.app.auth.entity;

/**
 * Coarse-grained roles used by Spring Security's authority model
 * (ROLE_ prefix convention) and checked via @PreAuthorize/@Secured in
 * downstream services.
 */
public enum Role {
    ROLE_USER,
    ROLE_ADMIN
}
