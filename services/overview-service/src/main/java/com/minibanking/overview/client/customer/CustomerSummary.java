package com.minibanking.overview.client.customer;

import java.time.Instant;
import java.util.UUID;

public record CustomerSummary(
        UUID id,
        String firstName,
        String lastName,
        String email,
        String phone,
        String address,
        String status,
        Instant createdAt,
        Instant updatedAt
) {
}
