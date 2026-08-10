package com.minibanking.customer.customer.api;

import java.time.Instant;
import java.util.UUID;

import com.minibanking.customer.customer.domain.CustomerStatus;

public record CustomerResponse(
        UUID id,
        String firstName,
        String lastName,
        String email,
        String phone,
        String address,
        CustomerStatus status,
        Instant createdAt,
        Instant updatedAt,
        long version
) {
}
