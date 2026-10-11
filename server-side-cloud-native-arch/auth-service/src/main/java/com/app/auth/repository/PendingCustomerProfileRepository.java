package com.app.auth.repository;

import com.app.auth.entity.PendingCustomerProfile;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PendingCustomerProfileRepository extends JpaRepository<PendingCustomerProfile, String> {
}
