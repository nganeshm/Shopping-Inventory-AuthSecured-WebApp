package com.app.product.service;

import com.app.product.client.CustomerServiceClient;
import com.app.product.dto.CustomerSnapshotDto;
import com.app.product.exception.CustomerNotFoundException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Wraps the cross-service Feign call to customer-service with resilience.
 *
 * @CircuitBreaker(name=..., fallbackMethod=...) — Resilience4j tracks call
 * failures against "customerServiceCB" (thresholds configured in
 * application.yml). Once the failure rate crosses the threshold, the breaker
 * OPENS and short-circuits straight to the fallback — no network call at all
 * — until the wait duration elapses and it moves to HALF_OPEN to probe
 * recovery.
 *
 * @Retry adds a few automatic retries (with backoff) for transient blips
 * BEFORE those failures count against the circuit breaker.
 *
 * Why this matters here specifically: placing an order needs the customer's
 * shipping address. If customer-service is briefly down, we should not hard
 * -fail order creation — the fallback below lets OrderServiceImpl decide to
 * proceed with a "pending address" placeholder rather than cascade the
 * outage to every checkout.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CustomerLookupService {

    private final CustomerServiceClient customerServiceClient;

    @CircuitBreaker(name = "customerServiceCB", fallbackMethod = "getCustomerFallback")
    @Retry(name = "customerServiceCB")
    public CustomerSnapshotDto getCustomer(Long customerId, String authorizationHeader) {
        CustomerSnapshotDto dto = customerServiceClient.getCustomerSnapshot(customerId, authorizationHeader);
        if (dto == null) {
            throw new CustomerNotFoundException("Customer not found: " + customerId);
        }
        return dto;
    }

    // Signature must be: same params as the original + a Throwable, same return type.
    private CustomerSnapshotDto getCustomerFallback(Long customerId, String authorizationHeader, Throwable throwable) {
        log.warn("customer-service unavailable while placing order for customer {}. Reason: {}",
                customerId, throwable.getMessage());
        return new CustomerSnapshotDto(customerId, "Unknown", "Customer",
                "Address unavailable", "Unknown", "Unknown", "00000", "Unknown");
    }
}
