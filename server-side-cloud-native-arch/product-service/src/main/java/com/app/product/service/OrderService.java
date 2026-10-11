package com.app.product.service;

import com.app.product.dto.OrderRequest;
import com.app.product.dto.OrderResponse;

import java.util.List;

public interface OrderService {
    OrderResponse placeOrder(OrderRequest request, String authorizationHeader);
    OrderResponse getByOrderNumber(String orderNumber);
    List<OrderResponse> getByCustomerId(Long customerId);
}
