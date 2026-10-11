package com.app.product.controller;

import com.app.product.dto.ProductImportResponse;
import com.app.product.service.ProductImportService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/**
 * Bulk product load. Spark reads the CSV; this service writes products and inventory.
 */
@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductImportController {

    private final ProductImportService productImportService;

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping(value = "/import", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ProductImportResponse> importProducts(@RequestParam("file") MultipartFile file) {
        return ResponseEntity.status(HttpStatus.CREATED).body(productImportService.importCsv(file));
    }
}
