package com.app.product.repository;

import com.app.product.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, Long> {

    Optional<Product> findBySku(String sku);

    boolean existsBySku(String sku);

    @Query("select p.sku from Product p where p.sku in :skus")
    List<String> findSkusIn(@Param("skus") Collection<String> skus);

    List<Product> findByCategoryId(Long categoryId);

    @Query("""
           SELECT p FROM Product p
           JOIN FETCH p.category
           LEFT JOIN FETCH p.inventory
           WHERE p.id = :id
           """)
    Optional<Product> findByIdWithCategoryAndInventory(@Param("id") Long id);

    @Query("SELECT p.id FROM Product p WHERE p.active = true")
    Page<Long> findActiveIds(Pageable pageable);

    @Query("""
           SELECT DISTINCT p FROM Product p
           LEFT JOIN FETCH p.category
           LEFT JOIN FETCH p.inventory
           WHERE p.id IN :ids
           """)
    List<Product> findByIdInWithCategoryAndInventory(@Param("ids") Collection<Long> ids);

    @Query("""
           SELECT p FROM Product p
           JOIN p.inventory i
           WHERE i.quantityAvailable <= i.reorderLevel
           """)
    List<Product> findLowStockProducts();

    @Query("SELECT p FROM Product p WHERE LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<Product> searchByName(@Param("keyword") String keyword);
}
