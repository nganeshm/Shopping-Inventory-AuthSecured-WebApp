package com.app.product.entity;

/** Simple, linear order lifecycle for this demo (no partial shipments etc.). */
public enum OrderStatus {
    PENDING,
    CONFIRMED,
    SHIPPED,
    DELIVERED,
    CANCELLED
}
