package com.app.product.dto;

public record InventoryResponse(
        Long id,
        Long productId,
        int quantityAvailable,
        int quantityReserved,
        int reorderLevel,
        String warehouseLocation,
        boolean lowStock
) {}
