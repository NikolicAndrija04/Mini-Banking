package com.minibanking.overview.aggregation;

import java.time.Instant;

public record InstanceResponse(
        String service,
        String servedBy,
        Instant timestamp
) {
}
