package com.app.customer.dto;

import java.time.Instant;
import java.util.List;

public record CustomerResponse(
        Long id,
        String firstName,
        String lastName,
        String email,
        boolean active,
        List<AddressResponse> addresses,
        ContactDetailResponse contactDetail,
        Instant registeredAt
) {}
