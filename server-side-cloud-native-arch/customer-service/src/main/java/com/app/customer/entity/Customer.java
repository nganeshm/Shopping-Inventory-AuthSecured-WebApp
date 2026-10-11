package com.app.customer.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * The customer profile aggregate root. Linked to auth-service's User only
 * loosely by email (NOT a foreign key — different service, different
 * database) — auth-service answers "who can log in", this answers "who are
 * they and where do we ship to."
 *
 * Relationships (both real, enforced joins WITHIN this database):
 *  - Customer (1) --> (N) Address        [one-to-many, owned by Address]
 *  - Customer (1) <-> (1) ContactDetail  [one-to-one, owned by ContactDetail]
 */
@Entity
@Table(name = "customers", indexes = {
        @Index(name = "idx_customers_email", columnList = "email", unique = true)
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Customer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 60)
    private String firstName;

    @Column(nullable = false, length = 60)
    private String lastName;

    /** Loose correlation key back to auth-service's User.email — never a DB FK across services. */
    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    @Builder.Default
    private boolean active = true;

    /**
     * cascade=ALL + orphanRemoval=true: saving a Customer saves its
     * addresses; removing an Address from this list deletes that row.
     * mappedBy="customer" -> Address is the owning side (holds customer_id).
     */
    @OneToMany(mappedBy = "customer", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Address> addresses = new ArrayList<>();

    @OneToOne(mappedBy = "customer", cascade = CascadeType.ALL, orphanRemoval = true)
    private ContactDetail contactDetail;

    @CreationTimestamp
    @Column(updatable = false)
    private Instant registeredAt;

    public void addAddress(Address address) {
        addresses.add(address);
        address.setCustomer(this);
    }

    @JsonIgnore
    @Transient
    public Address getDefaultAddress() {
        return addresses.stream()
                .filter(Address::isDefaultAddress)
                .findFirst()
                .orElse(addresses.isEmpty() ? null : addresses.get(0));
    }
}
