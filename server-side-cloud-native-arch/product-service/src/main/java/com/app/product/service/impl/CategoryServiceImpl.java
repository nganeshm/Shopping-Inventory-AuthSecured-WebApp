package com.app.product.service.impl;

import com.app.product.config.CacheNames;
import com.app.product.dto.CategoryRequest;
import com.app.product.dto.CategoryResponse;
import com.app.product.entity.Category;
import com.app.product.exception.CategoryNotFoundException;
import com.app.product.repository.CategoryRepository;
import com.app.product.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;

    @Override
    @Transactional
    @Caching(evict = {
            @CacheEvict(cacheNames = CacheNames.CATEGORY_BY_ID, allEntries = true),
            @CacheEvict(cacheNames = CacheNames.CATEGORY_CATALOG, allEntries = true)
    })
    public CategoryResponse create(CategoryRequest request) {
        Category parent = null;
        if (request.parentCategoryId() != null) {
            parent = categoryRepository.findById(request.parentCategoryId())
                    .orElseThrow(() -> new CategoryNotFoundException(
                            "Parent category not found: " + request.parentCategoryId()));
        }

        Category category = Category.builder()
                .name(request.name())
                .description(request.description())
                .parentCategory(parent)
                .build();

        return toResponse(categoryRepository.save(category));
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(cacheNames = CacheNames.CATEGORY_BY_ID, key = "#id")
    public CategoryResponse getById(Long id) {
        return categoryRepository.findById(id)
                .map(this::toResponse)
                .orElseThrow(() -> new CategoryNotFoundException("Category not found: " + id));
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(cacheNames = CacheNames.CATEGORY_CATALOG, key = "'all'")
    public List<CategoryResponse> getAll() {
        // Cache a stable ArrayList rather than a JDK internal immutable-list type.
        return categoryRepository.findAll().stream()
                .map(this::toResponse)
                .collect(Collectors.toCollection(ArrayList::new));
    }

    private CategoryResponse toResponse(Category c) {
        return new CategoryResponse(
                c.getId(),
                c.getName(),
                c.getDescription(),
                c.getParentCategory() != null ? c.getParentCategory().getId() : null,
                c.getParentCategory() != null ? c.getParentCategory().getName() : null,
                c.getSubCategories().stream().map(Category::getName).toList()
        );
    }
}
