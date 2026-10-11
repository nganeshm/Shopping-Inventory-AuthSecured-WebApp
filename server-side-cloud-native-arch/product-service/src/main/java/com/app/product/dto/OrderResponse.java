package com.app.product.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public record OrderResponse(
        Long id,
        String orderNumber,
        Long customerId,
        String status,
        BigDecimal totalAmount,
        String shippingLine1,
        String shippingCity,
        String shippingState,
        String shippingPostalCode,
        String shippingCountry,
        Instant orderDate,
        List<OrderItemResponse> items
) {}
