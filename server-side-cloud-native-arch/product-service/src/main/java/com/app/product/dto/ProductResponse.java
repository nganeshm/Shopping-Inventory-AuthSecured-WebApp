package com.app.product.dto;

import java.math.BigDecimal;

public record ProductResponse(
        Long id,
        String sku,
        String name,
        String description,
        BigDecimal price,
        boolean active,
        String categoryName,
        InventoryResponse inventory
) {}
