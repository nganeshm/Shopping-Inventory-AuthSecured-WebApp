package com.app.auth.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;

/**
 * The identity record. auth-service is the single source of truth for
 * "who can log in" — note it deliberately does NOT hold profile data like
 * address or phone; that belongs to customer-service (bounded context
 * separation, a core SOA idea: each service owns one cohesive slice of the
 * domain). The two are linked loosely by email, never by a foreign key,
 * because they live in different databases.
 */
@Entity
@Table(name = "users", indexes = {
        @Index(name = "idx_users_username", columnList = "username", unique = true),
        @Index(name = "idx_users_email", columnList = "email", unique = true)
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String username;

    @Column(nullable = false, unique = true)
    private String email;

    /** BCrypt hash only — the raw password is never persisted. */
    @Column(nullable = false)
    private String password;

    /**
     * Roles stored as a simple element collection in a join table
     * (user_roles) rather than a full entity, since a Role here is just an
     * enum label with no attributes of its own — using @ElementCollection
     * instead of a Role entity + ManyToMany keeps this simple without
     * over-engineering a lookup table that would never gain more columns.
     */
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "user_roles", joinColumns = @JoinColumn(name = "user_id"))
    @Enumerated(EnumType.STRING)
    @Column(name = "role")
    @Builder.Default
    private Set<Role> roles = new HashSet<>();

    @Column(nullable = false)
    @Builder.Default
    private boolean enabled = true;

    @Column(updatable = false)
    private Instant createdAt;

    @PrePersist
    void onCreate() {
        this.createdAt = Instant.now();
    }
}
