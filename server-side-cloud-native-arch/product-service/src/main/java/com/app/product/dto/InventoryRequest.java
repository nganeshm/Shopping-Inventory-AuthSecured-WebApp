package com.app.product.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public record InventoryRequest(
        @Min(value = 0, message = "quantityAvailable cannot be negative")
        int quantityAvailable,

        @Min(value = 0, message = "reorderLevel cannot be negative")
        int reorderLevel,

        @NotBlank(message = "warehouseLocation is required")
        String warehouseLocation
) {}
