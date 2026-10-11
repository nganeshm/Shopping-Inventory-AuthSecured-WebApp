package com.app.product.dto;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;

public record ProductRequest(
        @NotBlank(message = "sku is required")
        @Size(max = 40)
        String sku,

        @NotBlank(message = "name is required")
        @Size(max = 150)
        String name,

        @Size(max = 1000)
        String description,

        @NotNull(message = "price is required")
        @DecimalMin(value = "0.0", inclusive = false, message = "price must be greater than 0")
        BigDecimal price,

        @NotNull(message = "categoryId is required")
        Long categoryId,

        /** Optional initial stock — if omitted, inventory starts at 0. */
        @Min(0)
        Integer initialQuantity,

        String warehouseLocation
) {}
