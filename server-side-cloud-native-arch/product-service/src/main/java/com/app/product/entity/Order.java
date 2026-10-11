package com.app.product.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * A customer's order. Deliberately does NOT have a JPA @ManyToOne to a
 * Customer entity — Customer lives in a different microservice with its own
 * database, and JPA relationships cannot span two separate databases/JVMs.
 *
 * Instead:
 *  1. customerId is stored as a plain Long — a "soft" reference, validated
 *     at order-creation time via a Feign call to customer-service
 *     (see CustomerLookupService), not enforced by a DB foreign key.
 *  2. The shipping address is SNAPSHOTTED onto the order (shippingLine1,
 *     shippingCity, ...) at the moment of purchase, copied from whatever
 *     customer-service returned. This is deliberate: if the customer edits
 *     their address next week, this already-placed order must still show
 *     the address it actually shipped to. This is a standard SOA/event-driven
 *     pattern — "copy what you need, don't reach across the boundary later."
 *
 * Within THIS database, Order does have real, enforced joins to OrderItem
 * (1:N) and (transitively) Product.
 */
@Entity
@Table(name = "orders", indexes = {
        @Index(name = "idx_orders_customer_id", columnList = "customer_id"),
        @Index(name = "idx_orders_order_number", columnList = "order_number", unique = true)
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "order_number", nullable = false, unique = true, length = 40)
    private String orderNumber;

    /** Cross-service reference by ID only — see class javadoc. */
    @Column(name = "customer_id", nullable = false)
    private Long customerId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private OrderStatus status = OrderStatus.PENDING;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal totalAmount;

    // --- shipping address snapshot (copied from customer-service at order time) ---
    @Column(length = 150)
    private String shippingLine1;
    @Column(length = 150)
    private String shippingCity;
    @Column(length = 100)
    private String shippingState;
    @Column(length = 20)
    private String shippingPostalCode;
    @Column(length = 100)
    private String shippingCountry;

    /**
     * The real, same-database join: an order owns its line items.
     * cascade=ALL + orphanRemoval=true means: save the Order -> its
     * OrderItems are saved too; remove an item from this list -> it's
     * deleted from the DB, not just unlinked.
     */
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<OrderItem> orderItems = new ArrayList<>();

    @CreationTimestamp
    @Column(updatable = false)
    private Instant orderDate;

    /** Keeps the bidirectional OrderItem<->Order link consistent on both sides. */
    public void addItem(OrderItem item) {
        orderItems.add(item);
        item.setOrder(this);
    }
}
