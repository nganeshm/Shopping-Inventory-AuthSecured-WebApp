package com.app.product.service.impl;

import com.app.product.dto.CustomerSnapshotDto;
import com.app.product.dto.OrderItemRequest;
import com.app.product.dto.OrderItemResponse;
import com.app.product.dto.OrderRequest;
import com.app.product.dto.OrderResponse;
import com.app.product.entity.Order;
import com.app.product.entity.OrderItem;
import com.app.product.entity.Product;
import com.app.product.exception.OrderNotFoundException;
import com.app.product.exception.ProductNotFoundException;
import com.app.product.repository.OrderRepository;
import com.app.product.repository.ProductRepository;
import com.app.product.service.CustomerLookupService;
import com.app.product.service.InventoryService;
import com.app.product.service.OrderService;
import com.app.product.strategy.PricingStrategy;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

/**
 * The most composite use case in the service: placing an order touches
 * Product (read), Inventory (write, via InventoryService), the cross-service
 * Customer lookup (via CustomerLookupService, resilient), and a pluggable
 * PricingStrategy — yet this class only depends on INTERFACES for every one
 * of those collaborators (ProductRepository aside, which IS the data-access
 * abstraction for this service's own aggregate). That is Dependency
 * Inversion applied consistently: swap InventoryServiceImpl,
 * BulkOrderDiscountStrategy, or even the Feign-backed CustomerLookupService
 * for test doubles, and this class does not change.
 */
@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final InventoryService inventoryService;
    private final CustomerLookupService customerLookupService;
    private final PricingStrategy pricingStrategy;

    @Override
    @Transactional
    public OrderResponse placeOrder(OrderRequest request, String authorizationHeader) {
        // 1. Validate the customer exists and fetch their shipping address —
        //    resilient call across the service boundary (circuit breaker +
        //    retry live inside CustomerLookupService).
        CustomerSnapshotDto customer = customerLookupService.getCustomer(request.customerId(), authorizationHeader);

        // 2. Build order items: for each line, load the Product, reserve
        //    stock (throws OutOfStockException if insufficient), and snapshot
        //    the current price.
        Order order = Order.builder()
                .orderNumber(generateOrderNumber())
                .customerId(request.customerId())
                .shippingLine1(customer.defaultAddressLine1())
                .shippingCity(customer.defaultAddressCity())
                .shippingState(customer.defaultAddressState())
                .shippingPostalCode(customer.defaultAddressPostalCode())
                .shippingCountry(customer.defaultAddressCountry())
                .build();

        BigDecimal subtotal = BigDecimal.ZERO;
        int totalItemCount = 0;

        for (OrderItemRequest itemRequest : request.items()) {
            Product product = productRepository.findById(itemRequest.productId())
                    .orElseThrow(() -> new ProductNotFoundException("Product not found: " + itemRequest.productId()));

            inventoryService.reserve(product.getId(), itemRequest.quantity());

            BigDecimal lineSubtotal = product.getPrice().multiply(BigDecimal.valueOf(itemRequest.quantity()));
            OrderItem orderItem = OrderItem.builder()
                    .product(product)
                    .quantity(itemRequest.quantity())
                    .unitPriceAtPurchase(product.getPrice())
                    .subtotal(lineSubtotal)
                    .build();

            order.addItem(orderItem); // keeps both sides of the bidirectional join in sync
            subtotal = subtotal.add(lineSubtotal);
            totalItemCount += itemRequest.quantity();
        }

        // 3. Pricing strategy decides the final total (OCP: see strategy package).
        order.setTotalAmount(pricingStrategy.calculateFinalTotal(subtotal, totalItemCount));

        return toResponse(orderRepository.save(order));
    }

    @Override
    public OrderResponse getByOrderNumber(String orderNumber) {
        return orderRepository.findByOrderNumber(orderNumber)
                .map(this::toResponse)
                .orElseThrow(() -> new OrderNotFoundException("Order not found: " + orderNumber));
    }

    @Override
    public List<OrderResponse> getByCustomerId(Long customerId) {
        return orderRepository.findDetailedOrdersByCustomerId(customerId).stream().map(this::toResponse).toList();
    }

    private String generateOrderNumber() {
        return "ORD-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    private OrderResponse toResponse(Order o) {
        List<OrderItemResponse> items = o.getOrderItems().stream()
                .map(oi -> new OrderItemResponse(
                        oi.getProduct().getId(), oi.getProduct().getName(),
                        oi.getQuantity(), oi.getUnitPriceAtPurchase(), oi.getSubtotal()))
                .toList();

        return new OrderResponse(
                o.getId(), o.getOrderNumber(), o.getCustomerId(), o.getStatus().name(), o.getTotalAmount(),
                o.getShippingLine1(), o.getShippingCity(), o.getShippingState(),
                o.getShippingPostalCode(), o.getShippingCountry(), o.getOrderDate(), items);
    }
}
