package com.app.auth.repository;

import com.app.auth.entity.OtpPurpose;
import com.app.auth.entity.OtpToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface OtpTokenRepository extends JpaRepository<OtpToken, Long> {

    List<OtpToken> findByEmailAndPurposeAndUsedFalseOrderByCreatedAtDesc(String email, OtpPurpose purpose);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("update OtpToken t set t.used = true where t.email = :email and t.purpose = :purpose and t.used = false")
    void markUnusedAsUsed(@Param("email") String email, @Param("purpose") OtpPurpose purpose);
}
