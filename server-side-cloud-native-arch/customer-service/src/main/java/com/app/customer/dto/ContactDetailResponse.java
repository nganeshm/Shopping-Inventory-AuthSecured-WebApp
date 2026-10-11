package com.app.customer.dto;

public record ContactDetailResponse(
        Long id,
        String primaryPhone,
        String secondaryPhone,
        String alternateEmail,
        String preferredContactMethod
) {}
