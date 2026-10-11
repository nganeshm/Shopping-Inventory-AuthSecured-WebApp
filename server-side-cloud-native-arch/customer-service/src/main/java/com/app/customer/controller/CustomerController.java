package com.app.customer.controller;

import com.app.customer.dto.AddressRequest;
import com.app.customer.dto.CustomerRequest;
import com.app.customer.dto.CustomerResponse;
import com.app.customer.dto.CustomerSnapshotResponse;
import com.app.customer.service.CustomerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/customers")
@RequiredArgsConstructor
public class CustomerController {

    private final CustomerService customerService;

    @PostMapping("/register")
    public ResponseEntity<CustomerResponse> register(@Valid @RequestBody CustomerRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(customerService.register(request));
    }

    @GetMapping("/me")
    public CustomerResponse getMyProfile(Authentication authentication) {
        Object details = authentication == null ? null : authentication.getDetails();
        if (!(details instanceof String email) || email.isBlank()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED,
                    "Sign in again so the token includes your email");
        }
        return customerService.getByEmail(email);
    }

    @GetMapping("/details/by-email")
    public CustomerResponse getDetailsByEmail(@RequestParam("email") String email) {
        if (email == null || email.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "email is required");
        }
        return customerService.getByEmail(email.trim());
    }

    @GetMapping("/{id}")
    public CustomerResponse getById(@PathVariable("id") Long id) {
        return customerService.getById(id);
    }

    @GetMapping
    public List<CustomerResponse> getAll() {
        return customerService.getAll();
    }

    @PostMapping("/{id}/addresses")
    public CustomerResponse addAddress(@PathVariable("id") Long id, @Valid @RequestBody AddressRequest request) {
        return customerService.addAddress(id, request);
    }

    @GetMapping("/{id}/snapshot")
    public CustomerSnapshotResponse getSnapshot(@PathVariable("id") Long id) {
        return customerService.getSnapshot(id);
    }
}
