package com.app.product.controller;

import com.app.product.dto.OrderRequest;
import com.app.product.dto.OrderResponse;
import com.app.product.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    public ResponseEntity<OrderResponse> placeOrder(@Valid @RequestBody OrderRequest request,
                                                      @RequestHeader("Authorization") String authorization) {
        return ResponseEntity.status(HttpStatus.CREATED).body(orderService.placeOrder(request, authorization));
    }

    @GetMapping("/{orderNumber}")
    public OrderResponse getByOrderNumber(@PathVariable String orderNumber) {
        return orderService.getByOrderNumber(orderNumber);
    }

    @GetMapping("/customer/{customerId}")
    public List<OrderResponse> getByCustomer(@PathVariable Long customerId) {
        return orderService.getByCustomerId(customerId);
    }
}
