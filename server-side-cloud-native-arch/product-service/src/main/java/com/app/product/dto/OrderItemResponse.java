package com.app.product.dto;

import java.math.BigDecimal;

public record OrderItemResponse(
        Long productId,
        String productName,
        int quantity,
        BigDecimal unitPriceAtPurchase,
        BigDecimal subtotal
) {}
