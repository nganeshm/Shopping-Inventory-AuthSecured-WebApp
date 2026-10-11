package com.app.product.controller;

import com.app.product.dto.ProductPageResponse;
import com.app.product.dto.ProductRequest;
import com.app.product.dto.ProductResponse;
import com.app.product.service.ProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<ProductResponse> create(@Valid @RequestBody ProductRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(productService.create(request));
    }

    @GetMapping("/{id}")
    public ProductResponse getById(@PathVariable Long id) {
        return productService.getById(id);
    }

    @GetMapping
    public ProductPageResponse getAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        int safePage = Math.max(page, 0);
        int safeSize = size > 0 ? Math.min(size, 20) : 20;
        return productService.getAll(safePage, safeSize);
    }

    @GetMapping("/search")
    public List<ProductResponse> search(@RequestParam String keyword) {
        return productService.search(keyword);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/low-stock")
    public List<ProductResponse> lowStock() {
        return productService.getLowStock();
    }
}
