package com.app.product.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Product categories form a self-referencing tree (e.g. "Electronics" ->
 * "Laptops" -> "Gaming Laptops"), modeled with a single table and a nullable
 * FK back to itself (parent_category_id). This is the classic "adjacency
 * list" pattern for hierarchical data in a relational schema.
 *
 * Relationships:
 *  - parentCategory: ManyToOne (many subcategories share one parent)
 *  - subCategories:  OneToMany, the inverse side (mappedBy) of parentCategory
 *  - products:       OneToMany, the inverse side of Product.category
 */
@Entity
@Table(name = "categories")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    private String name;

    @Column(length = 500)
    private String description;

    /**
     * FetchType.LAZY is the deliberate default for *ToOne associations here:
     * loading a Category should not silently drag its entire parent chain
     * and product list into memory unless explicitly requested (via
     * @EntityGraph or an explicit join-fetch query), which keeps queries
     * predictable and avoids the classic N+1 trap.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_category_id")
    @JsonIgnore // avoid infinite recursion when serializing parent <-> children
    private Category parentCategory;

    @OneToMany(mappedBy = "parentCategory", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Category> subCategories = new ArrayList<>();

    @OneToMany(mappedBy = "category")
    @JsonIgnore
    @Builder.Default
    private List<Product> products = new ArrayList<>();
}
