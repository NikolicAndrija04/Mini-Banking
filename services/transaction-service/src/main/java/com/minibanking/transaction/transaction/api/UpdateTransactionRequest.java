package com.minibanking.transaction.transaction.api;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateTransactionRequest(
        @NotBlank @Size(max = 280) String description
) {
}
