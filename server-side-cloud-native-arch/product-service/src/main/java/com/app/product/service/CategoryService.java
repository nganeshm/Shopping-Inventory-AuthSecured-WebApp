package com.app.product.service;

import com.app.product.dto.CategoryRequest;
import com.app.product.dto.CategoryResponse;

import java.util.List;

/**
 * Interface Segregation + Dependency Inversion: the controller depends on
 * this narrow contract, never on CategoryServiceImpl directly. Spring
 * injects whichever bean implements it — today there's one implementation,
 * but the controller's code would not need to change if a second
 * (e.g. a caching decorator) were introduced.
 */
public interface CategoryService {
    CategoryResponse create(CategoryRequest request);
    CategoryResponse getById(Long id);
    List<CategoryResponse> getAll();
}
