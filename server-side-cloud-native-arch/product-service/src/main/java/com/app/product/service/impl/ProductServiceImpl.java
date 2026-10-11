package com.app.product.service.impl;

import com.app.product.config.CacheNames;
import com.app.product.dto.InventoryResponse;
import com.app.product.dto.ProductPageResponse;
import com.app.product.dto.ProductRequest;
import com.app.product.dto.ProductResponse;
import com.app.product.entity.Category;
import com.app.product.entity.Inventory;
import com.app.product.entity.Product;
import com.app.product.exception.CategoryNotFoundException;
import com.app.product.exception.ProductNotFoundException;
import com.app.product.repository.CategoryRepository;
import com.app.product.repository.ProductRepository;
import com.app.product.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Orchestrates Product + its Category link + its (required) Inventory row —
 * a good example of a service layer composing TWO repositories to keep a
 * single cohesive use case ("create a sellable product") transactional and
 * atomic, while each repository itself stays narrowly focused on one
 * aggregate (SRP).
 */
@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;

    @Override
    @Transactional
    @Caching(evict = {
            @CacheEvict(cacheNames = CacheNames.PRODUCT_BY_ID, allEntries = true),
            @CacheEvict(cacheNames = CacheNames.PRODUCT_CATALOG, allEntries = true)
    })
    public ProductResponse create(ProductRequest request) {
        Category category = categoryRepository.findById(request.categoryId())
                .orElseThrow(() -> new CategoryNotFoundException("Category not found: " + request.categoryId()));

        Product product = Product.builder()
                .sku(request.sku())
                .name(request.name())
                .description(request.description())
                .price(request.price())
                .category(category)
                .active(true)
                .build();

        Inventory inventory = Inventory.builder()
                .product(product)
                .quantityAvailable(request.initialQuantity() != null ? request.initialQuantity() : 0)
                .warehouseLocation(request.warehouseLocation())
                .lastRestockedAt(Instant.now())
                .build();
        product.setInventory(inventory); // cascade=ALL on Product.inventory persists this automatically

        return toResponse(productRepository.save(product));
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(cacheNames = CacheNames.PRODUCT_BY_ID, key = "#id")
    public ProductResponse getById(Long id) {
        return productRepository.findByIdWithCategoryAndInventory(id)
                .map(this::toResponse)
                .orElseThrow(() -> new ProductNotFoundException("Product not found: " + id));
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(cacheNames = CacheNames.PRODUCT_CATALOG, key = "#page + '-' + #size")
    public ProductPageResponse getAll(int page, int size) {
        Page<Long> ids = productRepository.findActiveIds(
                PageRequest.of(page, size, Sort.by("id")));
        List<ProductResponse> content = new ArrayList<>();
        if (!ids.isEmpty()) {
            Map<Long, Product> byId = productRepository
                    .findByIdInWithCategoryAndInventory(ids.getContent()).stream()
                    .collect(Collectors.toMap(Product::getId, Function.identity()));
            content = ids.getContent().stream()
                    .map(byId::get)
                    .filter(product -> product != null)
                    .sorted(Comparator.comparing(Product::getId))
                    .map(this::toResponse)
                    .collect(Collectors.toCollection(ArrayList::new));
        }
        return new ProductPageResponse(
                content,
                ids.getNumber(),
                ids.getSize(),
                ids.getTotalElements(),
                ids.getTotalPages(),
                ids.isLast());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductResponse> search(String keyword) {
        return productRepository.searchByName(keyword).stream().map(this::toResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductResponse> getLowStock() {
        return productRepository.findLowStockProducts().stream().map(this::toResponse).toList();
    }

    private ProductResponse toResponse(Product p) {
        InventoryResponse inv = null;
        if (p.getInventory() != null) {
            Inventory i = p.getInventory();
            inv = new InventoryResponse(i.getId(), p.getId(), i.getQuantityAvailable(),
                    i.getQuantityReserved(), i.getReorderLevel(), i.getWarehouseLocation(), i.isLowStock());
        }
        return new ProductResponse(
                p.getId(), p.getSku(), p.getName(), p.getDescription(), p.getPrice(),
                p.isActive(), p.getCategory().getName(), inv);
    }
}
