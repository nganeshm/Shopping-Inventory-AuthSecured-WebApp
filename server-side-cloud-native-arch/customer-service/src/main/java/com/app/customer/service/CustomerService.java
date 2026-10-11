package com.app.customer.service;

import com.app.customer.dto.AddressRequest;
import com.app.customer.dto.CustomerRequest;
import com.app.customer.dto.CustomerResponse;
import com.app.customer.dto.CustomerSnapshotResponse;
import java.util.List;

public interface CustomerService {
    CustomerResponse register(CustomerRequest request);
    CustomerResponse getById(Long id);
    CustomerResponse getByEmail(String email);
    List<CustomerResponse> getAll();
    CustomerResponse addAddress(Long customerId, AddressRequest request);
    CustomerSnapshotResponse getSnapshot(Long id);
}
