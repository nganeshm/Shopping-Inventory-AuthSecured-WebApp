package com.app.product.service;

import com.app.product.dto.InventoryResponse;

public interface InventoryService {
    InventoryResponse getByProductId(Long productId);
    InventoryResponse restock(Long productId, int quantity);
    /** Reserves stock for a pending order; throws OutOfStockException if insufficient. */
    void reserve(Long productId, int quantity);
}
