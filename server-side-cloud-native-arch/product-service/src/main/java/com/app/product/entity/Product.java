package com.app.product.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * The sellable item. Sits at the center of three relationships:
 *  - Product (N) --> (1) Category            [many-to-one]
 *  - Product (1) <-> (1) Inventory            [one-to-one, Inventory owns the FK]
 *  - Product (1) --> (N) OrderItem            [one-to-many, read-mostly history]
 */
@Entity
@Table(name = "products", indexes = {
        @Index(name = "idx_products_sku", columnList = "sku", unique = true),
        @Index(name = "idx_products_category", columnList = "category_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 40)
    private String sku;

    @Column(nullable = false, length = 150)
    private String name;

    @Column(length = 1000)
    private String description;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal price;

    @Column(nullable = false)
    @Builder.Default
    private boolean active = true;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    /**
     * Inventory is a separate table/entity (not just columns on Product)
     * because it has its own lifecycle and write pattern — stock levels
     * change constantly (every order), while product name/price/description
     * change rarely. Splitting them avoids row-lock contention on the
     * product's descriptive data every time stock is adjusted, and keeps
     * each entity's responsibility singular (SRP applied to data modeling).
     *
     * mappedBy="product": Inventory owns the foreign key (product_id) since
     * it is the "optional/dependent" side; cascade=ALL + orphanRemoval means
     * saving/deleting a Product automatically saves/deletes its Inventory row.
     */
    @OneToOne(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private Inventory inventory;

    @OneToMany(mappedBy = "product")
    @JsonIgnore
    @Builder.Default
    private List<OrderItem> orderItems = new ArrayList<>();

    @CreationTimestamp
    @Column(updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    private Instant updatedAt;
}
