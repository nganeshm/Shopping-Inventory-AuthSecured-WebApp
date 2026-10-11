package com.app.product.service;

import com.app.product.dto.ProductPageResponse;
import com.app.product.dto.ProductRequest;
import com.app.product.dto.ProductResponse;

import java.util.List;

public interface ProductService {
    ProductResponse create(ProductRequest request);
    ProductResponse getById(Long id);
    ProductPageResponse getAll(int page, int size);
    List<ProductResponse> search(String keyword);
    List<ProductResponse> getLowStock();
}
