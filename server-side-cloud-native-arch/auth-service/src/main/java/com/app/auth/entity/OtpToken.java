package com.app.auth.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

/**
 * Short-lived one-time code. The raw OTP is never stored — only a BCrypt
 * hash — so a leaked authdb dump cannot be used to take over an account.
 */
@Entity
@Table(name = "otp_tokens", indexes = {
        @Index(name = "idx_otp_email_purpose", columnList = "email, purpose")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OtpToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String email;

    @Column(nullable = false)
    private String codeHash;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private OtpPurpose purpose;

    @Column(nullable = false)
    private Instant expiresAt;

    @Column(nullable = false)
    @Builder.Default
    private boolean used = false;

    @Column(updatable = false)
    private Instant createdAt;

    public static OtpToken of(String email, String codeHash, OtpPurpose purpose, Instant expiresAt) {
        return OtpToken.builder()
                .email(email)
                .codeHash(codeHash)
                .purpose(purpose)
                .expiresAt(expiresAt)
                .used(false)
                .build();
    }

    @PrePersist
    void onCreate() {
        this.createdAt = Instant.now();
    }

    public boolean isExpired() {
        return Instant.now().isAfter(expiresAt);
    }
}
