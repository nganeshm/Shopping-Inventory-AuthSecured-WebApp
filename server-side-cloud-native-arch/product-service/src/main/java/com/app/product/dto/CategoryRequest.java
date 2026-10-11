package com.app.product.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CategoryRequest(
        @NotBlank(message = "name is required")
        @Size(max = 100)
        String name,

        @Size(max = 500)
        String description,

        /** Optional — null means this is a top-level category. */
        Long parentCategoryId
) {}
