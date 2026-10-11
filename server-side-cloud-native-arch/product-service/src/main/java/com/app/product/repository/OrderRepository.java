package com.app.product.repository;

import com.app.product.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Long> {

    Optional<Order> findByOrderNumber(String orderNumber);

    List<Order> findByCustomerId(Long customerId);

    /**
     * JOIN FETCH across the Order -> OrderItem -> Product chain so the full
     * order detail (including each line's product) comes back in a single
     * round trip — the kind of query you reach for on an "order detail page".
     */
    @Query("""
           SELECT DISTINCT o FROM Order o
           LEFT JOIN FETCH o.orderItems oi
           LEFT JOIN FETCH oi.product p
           WHERE o.customerId = :customerId
           """)
    List<Order> findDetailedOrdersByCustomerId(@Param("customerId") Long customerId);
}
