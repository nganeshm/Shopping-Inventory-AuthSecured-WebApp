package com.app.product.controller;

import com.app.product.dto.InventoryResponse;
import com.app.product.service.InventoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/inventory")
@RequiredArgsConstructor
public class InventoryController {

    private final InventoryService inventoryService;

    @GetMapping("/product/{productId}")
    public InventoryResponse getByProduct(@PathVariable Long productId) {
        return inventoryService.getByProductId(productId);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/product/{productId}/restock")
    public InventoryResponse restock(@PathVariable Long productId, @RequestParam int quantity) {
        return inventoryService.restock(productId, quantity);
    }
}
