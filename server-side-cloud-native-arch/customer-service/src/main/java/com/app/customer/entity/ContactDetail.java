package com.app.customer.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

/**
 * One consolidated contact record per customer — modeled as its own table
 * (not just extra columns on Customer) because it's a cohesive, independently
 * meaningful sub-resource (phones + contact preference) that a client might
 * want to fetch/update on its own (e.g. a "contact preferences" settings
 * screen), matching Single Responsibility at the data-model level.
 */
@Entity
@Table(name = "contact_details")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ContactDetail {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false, unique = true)
    @JsonIgnore
    private Customer customer;

    @Column(nullable = false, length = 20)
    private String primaryPhone;

    @Column(length = 20)
    private String secondaryPhone;

    @Column(length = 150)
    private String alternateEmail;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    @Builder.Default
    private ContactMethod preferredContactMethod = ContactMethod.EMAIL;
}
