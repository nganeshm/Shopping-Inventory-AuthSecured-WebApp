package com.app.product.config;

/**
 * Central cache names prevent string duplication across service classes and
 * make the invalidation graph explicit.
 */
public final class CacheNames {

    public static final String CATEGORY_BY_ID = "category-by-id";
    public static final String CATEGORY_CATALOG = "category-catalog";
    public static final String PRODUCT_BY_ID = "product-by-id";
    public static final String PRODUCT_CATALOG = "product-catalog";
    public static final String INVENTORY_BY_PRODUCT = "inventory-by-product";

    private CacheNames() {
    }
}
