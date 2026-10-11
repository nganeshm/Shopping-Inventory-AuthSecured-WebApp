package com.app.product.repository;

import com.app.product.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface CategoryRepository extends JpaRepository<Category, Long> {
    Optional<Category> findByName(String name);

    /** Top-level categories only (no parent) — e.g. for a navigation menu. */
    List<Category> findByParentCategoryIsNull();

    boolean existsByName(String name);

    List<Category> findByNameIn(Collection<String> names);
}
