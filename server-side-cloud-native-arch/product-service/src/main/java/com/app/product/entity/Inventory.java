package com.app.product.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

/**
 * Stock-keeping record for exactly one Product (strict 1:1).
 * quantityAvailable is what can be freshly reserved; quantityReserved is
 * stock already allocated to PENDING/CONFIRMED orders but not yet shipped —
 * splitting the two avoids overselling during the window between "order
 * placed" and "order shipped".
 */
@Entity
@Table(name = "inventory")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Inventory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Owning side of the Product <-> Inventory one-to-one: this table holds
     * the foreign key (product_id), with a unique constraint enforcing the
     * 1:1 cardinality at the database level, not just in Java.
     */
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false, unique = true)
    private Product product;

    @Column(nullable = false)
    @Builder.Default
    private int quantityAvailable = 0;

    @Column(nullable = false)
    @Builder.Default
    private int quantityReserved = 0;

    @Column(nullable = false)
    @Builder.Default
    private int reorderLevel = 10;

    @Column(length = 100)
    private String warehouseLocation;

    private Instant lastRestockedAt;

    @Transient // derived, never persisted as its own column
    public boolean isLowStock() {
        return quantityAvailable <= reorderLevel;
    }
}
