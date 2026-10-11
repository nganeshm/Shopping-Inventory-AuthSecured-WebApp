package com.app.auth.service;

import com.app.auth.client.CustomerServiceClient;
import com.app.auth.dto.AddressDetailsRequest;
import com.app.auth.dto.ContactDetailsRequest;
import com.app.auth.dto.CustomerOnboardRequest;
import com.app.auth.dto.CustomerOnboardRequest.CustomerAddressPayload;
import com.app.auth.dto.CustomerOnboardRequest.CustomerContactPayload;
import com.app.auth.dto.RegisterRequest;
import com.app.auth.entity.PendingCustomerProfile;
import com.app.auth.repository.PendingCustomerProfileRepository;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Holds profile/address in authdb until OTP succeeds, then writes them
 * into customer-service (customerdb). auth-service never touches customerdb.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CustomerOnboardingService {

    private final PendingCustomerProfileRepository pendingRepository;
    private final CustomerServiceClient customerServiceClient;

    @Transactional
    public void storePending(RegisterRequest request) {
        AddressDetailsRequest address = request.address();
        ContactDetailsRequest contact = request.contact();
        pendingRepository.save(PendingCustomerProfile.builder()
                .email(request.email().toLowerCase())
                .firstName(blankTo(request.firstName(), request.username()))
                .lastName(blankTo(request.lastName(), "Member"))
                .addressType(address == null ? "HOME" : blankTo(address.addressType(), "HOME"))
                .line1(address == null ? "Address pending" : address.line1())
                .line2(address == null ? null : address.line2())
                .city(address == null ? "Pending" : address.city())
                .state(address == null ? "Pending" : address.state())
                .postalCode(address == null ? "000000" : address.postalCode())
                .country(address == null ? "IN" : address.country())
                .primaryPhone(contact == null ? "+0000000000" : contact.primaryPhone())
                .secondaryPhone(contact == null ? null : contact.secondaryPhone())
                .alternateEmail(contact == null ? null : contact.alternateEmail())
                .preferredContactMethod(contact == null
                        ? "EMAIL"
                        : blankTo(contact.preferredContactMethod(), "EMAIL"))
                .build());
    }

    public void publishAfterVerification(String email) {
        pendingRepository.findById(email.toLowerCase()).ifPresent(pending -> {
            try {
                customerServiceClient.register(toCustomerRequest(pending));
                pendingRepository.delete(pending);
                log.info("Customer profile created in customer-service for {}", email);
            } catch (FeignException ex) {
                if (ex.status() == 409) {
                    pendingRepository.delete(pending);
                    log.info("Customer already existed in customer-service for {}", email);
                    return;
                }
                log.warn("customer-service did not accept profile for {}: {}", email, ex.getMessage());
            } catch (Exception ex) {
                log.warn("customer-service did not accept profile for {}: {}", email, ex.getMessage());
            }
        });
    }

    private static CustomerOnboardRequest toCustomerRequest(PendingCustomerProfile pending) {
        return new CustomerOnboardRequest(
                pending.getFirstName(),
                pending.getLastName(),
                pending.getEmail(),
                new CustomerAddressPayload(
                        pending.getAddressType(),
                        pending.getLine1(),
                        pending.getLine2(),
                        pending.getCity(),
                        pending.getState(),
                        pending.getPostalCode(),
                        pending.getCountry(),
                        true),
                new CustomerContactPayload(
                        pending.getPrimaryPhone(),
                        pending.getSecondaryPhone(),
                        pending.getAlternateEmail(),
                        pending.getPreferredContactMethod()));
    }

    private static String blankTo(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value.trim();
    }
}
