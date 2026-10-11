package com.app.customer.repository;

import com.app.customer.entity.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface CustomerRepository extends JpaRepository<Customer, Long> {

    Optional<Customer> findByEmail(String email);
    boolean existsByEmail(String email);

    /**
     * Join-fetch Customer + addresses + contactDetail in one query — used by
     * the "full profile" endpoint so the response doesn't trigger N lazy
     * loads while serializing nested collections.
     */
    @Query("""
           SELECT DISTINCT c FROM Customer c
           LEFT JOIN FETCH c.addresses a
           LEFT JOIN FETCH c.contactDetail cd
           WHERE c.id = :id
           """)
    Optional<Customer> findDetailedById(@Param("id") Long id);

    @Query("""
           SELECT DISTINCT c FROM Customer c
           LEFT JOIN FETCH c.addresses a
           LEFT JOIN FETCH c.contactDetail cd
           """)
    java.util.List<Customer> findAllDetailed();

    @Query("""
           SELECT DISTINCT c FROM Customer c
           LEFT JOIN FETCH c.addresses a
           LEFT JOIN FETCH c.contactDetail cd
           WHERE lower(c.email) = lower(:email)
           """)
    Optional<Customer> findDetailedByEmail(@Param("email") String email);
}
