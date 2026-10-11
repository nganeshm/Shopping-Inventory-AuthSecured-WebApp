package com.app.product.dto;

import java.util.List;

public record CategoryResponse(
        Long id,
        String name,
        String description,
        Long parentCategoryId,
        String parentCategoryName,
        List<String> subCategoryNames
) {}
