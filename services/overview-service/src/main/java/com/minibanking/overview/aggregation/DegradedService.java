package com.minibanking.overview.aggregation;

public record DegradedService(
        String service,
        String reason
) {
}
