package com.app.product.service.impl;

import com.app.product.config.CacheNames;
import com.app.product.dto.ExtractedProductRow;
import com.app.product.entity.Category;
import com.app.product.entity.Inventory;
import com.app.product.entity.Product;
import com.app.product.repository.CategoryRepository;
import com.app.product.repository.ProductRepository;
import com.app.product.service.ProductImportWriter;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductImportWriterImpl implements ProductImportWriter {

    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;

    @Override
    @Transactional(readOnly = true)
    public List<String> findConflicts(List<ExtractedProductRow> rows) {
        if (rows.isEmpty()) {
            return List.of();
        }
        Map<String, Category> categories = categoriesByName(rows);
        Set<String> existingSkus = new HashSet<>(productRepository.findSkusIn(skus(rows)));

        List<String> errors = new ArrayList<>();
        for (ExtractedProductRow row : rows) {
            if (!categories.containsKey(row.categoryName())) {
                errors.add("row " + row.rowNumber() + ": category not found: " + row.categoryName());
            }
            if (existingSkus.contains(row.sku())) {
                errors.add("row " + row.rowNumber() + ": sku already exists");
            }
        }
        return errors;
    }

    @Override
    @Transactional
    @Caching(evict = {
            @CacheEvict(cacheNames = CacheNames.PRODUCT_BY_ID, allEntries = true),
            @CacheEvict(cacheNames = CacheNames.PRODUCT_CATALOG, allEntries = true),
            @CacheEvict(cacheNames = CacheNames.INVENTORY_BY_PRODUCT, allEntries = true)
    })
    public int save(List<ExtractedProductRow> rows) {
        Map<String, Category> categories = categoriesByName(rows);
        List<Product> products = new ArrayList<>(rows.size());
        for (ExtractedProductRow row : rows) {
            Product product = Product.builder()
                    .sku(row.sku())
                    .name(row.name())
                    .description(row.description())
                    .price(new BigDecimal(row.price()))
                    .category(categories.get(row.categoryName()))
                    .active(true)
                    .build();
            Inventory inventory = Inventory.builder()
                    .product(product)
                    .quantityAvailable(quantity(row.initialQuantity()))
                    .warehouseLocation(row.warehouseLocation())
                    .lastRestockedAt(Instant.now())
                    .build();
            product.setInventory(inventory);
            products.add(product);
        }
        productRepository.saveAll(products);
        return products.size();
    }

    private Map<String, Category> categoriesByName(List<ExtractedProductRow> rows) {
        Set<String> names = rows.stream().map(ExtractedProductRow::categoryName).collect(Collectors.toSet());
        return categoryRepository.findByNameIn(names).stream()
                .collect(Collectors.toMap(Category::getName, Function.identity()));
    }

    private static Set<String> skus(List<ExtractedProductRow> rows) {
        return rows.stream().map(ExtractedProductRow::sku).collect(Collectors.toSet());
    }

    private static int quantity(String raw) {
        if (raw == null || raw.isBlank()) {
            return 0;
        }
        return Integer.parseInt(raw);
    }
}
