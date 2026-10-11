package com.app.product.service.impl;

import com.app.product.config.CacheNames;
import com.app.product.dto.InventoryResponse;
import com.app.product.entity.Inventory;
import com.app.product.exception.OutOfStockException;
import com.app.product.exception.ProductNotFoundException;
import com.app.product.repository.InventoryRepository;
import com.app.product.service.InventoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class InventoryServiceImpl implements InventoryService {

    private final InventoryRepository inventoryRepository;

    @Override
    @Transactional(readOnly = true)
    @Cacheable(cacheNames = CacheNames.INVENTORY_BY_PRODUCT, key = "#productId")
    public InventoryResponse getByProductId(Long productId) {
        return toResponse(findByProductIdOrThrow(productId));
    }

    @Override
    @Transactional
    @Caching(evict = {
            @CacheEvict(cacheNames = CacheNames.INVENTORY_BY_PRODUCT, key = "#productId"),
            @CacheEvict(cacheNames = CacheNames.PRODUCT_BY_ID, key = "#productId"),
            @CacheEvict(cacheNames = CacheNames.PRODUCT_CATALOG, allEntries = true)
    })
    public InventoryResponse restock(Long productId, int quantity) {
        Inventory inventory = findByProductIdOrThrow(productId);
        inventory.setQuantityAvailable(inventory.getQuantityAvailable() + quantity);
        inventory.setLastRestockedAt(Instant.now());
        return toResponse(inventoryRepository.save(inventory));
    }

    @Override
    @Transactional
    @Caching(evict = {
            @CacheEvict(cacheNames = CacheNames.INVENTORY_BY_PRODUCT, key = "#productId"),
            @CacheEvict(cacheNames = CacheNames.PRODUCT_BY_ID, key = "#productId"),
            @CacheEvict(cacheNames = CacheNames.PRODUCT_CATALOG, allEntries = true)
    })
    public void reserve(Long productId, int quantity) {
        Inventory inventory = findByProductIdOrThrow(productId);
        if (inventory.getQuantityAvailable() < quantity) {
            throw new OutOfStockException(
                    "Insufficient stock for product " + productId +
                    ": requested " + quantity + ", available " + inventory.getQuantityAvailable());
        }
        inventory.setQuantityAvailable(inventory.getQuantityAvailable() - quantity);
        inventory.setQuantityReserved(inventory.getQuantityReserved() + quantity);
        inventoryRepository.save(inventory);
    }

    private Inventory findByProductIdOrThrow(Long productId) {
        return inventoryRepository.findByProductId(productId)
                .orElseThrow(() -> new ProductNotFoundException("No inventory record for product: " + productId));
    }

    private InventoryResponse toResponse(Inventory i) {
        return new InventoryResponse(
                i.getId(), i.getProduct().getId(), i.getQuantityAvailable(),
                i.getQuantityReserved(), i.getReorderLevel(), i.getWarehouseLocation(), i.isLowStock());
    }
}
