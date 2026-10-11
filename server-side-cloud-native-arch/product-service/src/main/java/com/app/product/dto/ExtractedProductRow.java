package com.app.product.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/** One JSON line written by {@code ProductCsvJob}. */
@JsonIgnoreProperties(ignoreUnknown = true)
public record ExtractedProductRow(
        int rowNumber,
        String sku,
        String name,
        String description,
        String price,
        String categoryName,
        String initialQuantity,
        String warehouseLocation,
        String reason
) {}
