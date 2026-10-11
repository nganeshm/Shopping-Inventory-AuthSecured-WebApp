package com.app.customer.service.impl;

import com.app.customer.dto.AddressRequest;
import com.app.customer.dto.AddressResponse;
import com.app.customer.dto.ContactDetailResponse;
import com.app.customer.dto.CustomerRequest;
import com.app.customer.dto.CustomerResponse;
import com.app.customer.dto.CustomerSnapshotResponse;
import com.app.customer.entity.ContactDetail;
import com.app.customer.entity.ContactMethod;
import com.app.customer.entity.Address;
import com.app.customer.entity.Customer;
import com.app.customer.exception.CustomerAlreadyExistsException;
import com.app.customer.exception.CustomerNotFoundException;
import com.app.customer.repository.CustomerRepository;
import com.app.customer.service.CustomerService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Creating a Customer is a single transactional unit of work that touches
 * three tables (customers, addresses, contact_details) through JPA cascades
 * — the service layer's job is exactly this kind of orchestration, while
 * each entity itself stays a plain, focused data model (SRP both at the
 * class level and at the data-model level).
 */
@Service
@RequiredArgsConstructor
public class CustomerServiceImpl implements CustomerService {

    private final CustomerRepository customerRepository;

    @Override
    @Transactional
    public CustomerResponse register(CustomerRequest request) {
        if (customerRepository.existsByEmail(request.email())) {
            throw new CustomerAlreadyExistsException("Customer already exists with email: " + request.email());
        }

        Customer customer = Customer.builder()
                .firstName(request.firstName())
                .lastName(request.lastName())
                .email(request.email())
                .active(true)
                .build();

        Address address = toAddressEntity(request.primaryAddress());
        address.setDefaultAddress(true); // the first address registered is always the default
        customer.addAddress(address); // keeps both sides of the join consistent

        var cd = request.contactDetail();
        ContactDetail contactDetail = ContactDetail.builder()
                .customer(customer)
                .primaryPhone(cd.primaryPhone())
                .secondaryPhone(cd.secondaryPhone())
                .alternateEmail(cd.alternateEmail())
                .preferredContactMethod(cd.preferredContactMethod() != null
                        ? cd.preferredContactMethod() : ContactMethod.EMAIL)
                .build();
        customer.setContactDetail(contactDetail); // cascade=ALL on Customer.contactDetail persists this too

        return toResponse(customerRepository.save(customer));
    }

    @Override
    @Transactional(readOnly = true)
    public CustomerResponse getById(Long id) {
        return customerRepository.findDetailedById(id)
                .map(this::toResponse)
                .orElseThrow(() -> new CustomerNotFoundException("Customer not found: " + id));
    }

    @Override
    @Transactional(readOnly = true)
    public CustomerResponse getByEmail(String email) {
        return customerRepository.findDetailedByEmail(email)
                .map(this::toResponse)
                .orElseThrow(() -> new CustomerNotFoundException("Customer not found: " + email));
    }

    @Override
    @Transactional(readOnly = true)
    public List<CustomerResponse> getAll() {
        return customerRepository.findAllDetailed().stream().map(this::toResponse).toList();
    }

    @Override
    @Transactional
    public CustomerResponse addAddress(Long customerId, AddressRequest request) {
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new CustomerNotFoundException("Customer not found: " + customerId));

        Address address = toAddressEntity(request);
        if (request.defaultAddress()) {
            // only one address may be default at a time
            customer.getAddresses().forEach(a -> a.setDefaultAddress(false));
        }
        customer.addAddress(address);

        return toResponse(customerRepository.save(customer));
    }

    @Override
    public CustomerSnapshotResponse getSnapshot(Long id) {
        Customer customer = customerRepository.findDetailedById(id)
                .orElseThrow(() -> new CustomerNotFoundException("Customer not found: " + id));

        Address defaultAddr = customer.getDefaultAddress();

        return new CustomerSnapshotResponse(
                customer.getId(), customer.getFirstName(), customer.getLastName(),
                defaultAddr != null ? defaultAddr.getLine1() : null,
                defaultAddr != null ? defaultAddr.getCity() : null,
                defaultAddr != null ? defaultAddr.getState() : null,
                defaultAddr != null ? defaultAddr.getPostalCode() : null,
                defaultAddr != null ? defaultAddr.getCountry() : null
        );
    }

    private Address toAddressEntity(AddressRequest r) {
        return Address.builder()
                .addressType(r.addressType())
                .line1(r.line1())
                .line2(r.line2())
                .city(r.city())
                .state(r.state())
                .postalCode(r.postalCode())
                .country(r.country())
                .defaultAddress(r.defaultAddress())
                .build();
    }

    private CustomerResponse toResponse(Customer c) {
        List<AddressResponse> addresses = c.getAddresses().stream()
                .map(a -> new AddressResponse(a.getId(), a.getAddressType().name(), a.getLine1(), a.getLine2(),
                        a.getCity(), a.getState(), a.getPostalCode(), a.getCountry(), a.isDefaultAddress()))
                .toList();

        ContactDetailResponse contact = c.getContactDetail() != null
                ? new ContactDetailResponse(
                        c.getContactDetail().getId(),
                        c.getContactDetail().getPrimaryPhone(),
                        c.getContactDetail().getSecondaryPhone(),
                        c.getContactDetail().getAlternateEmail(),
                        c.getContactDetail().getPreferredContactMethod().name())
                : null;

        return new CustomerResponse(c.getId(), c.getFirstName(), c.getLastName(), c.getEmail(),
                c.isActive(), addresses, contact, c.getRegisteredAt());
    }
}
