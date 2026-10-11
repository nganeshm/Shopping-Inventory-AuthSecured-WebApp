package com.app.product.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

/**
 * One line of an Order: "N units of this Product at the price it had when
 * the order was placed."
 *
 * unitPriceAtPurchase is a deliberate SNAPSHOT of Product.price, copied in
 * at creation time rather than always joining live to Product.price. If the
 * catalog price changes next month, historical orders must keep showing
 * what the customer actually paid — never silently recalculate the past.
 */
@Entity
@Table(name = "order_items")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    @JsonIgnore
    private Order order;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(nullable = false)
    private int quantity;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal unitPriceAtPurchase;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal subtotal;
}
