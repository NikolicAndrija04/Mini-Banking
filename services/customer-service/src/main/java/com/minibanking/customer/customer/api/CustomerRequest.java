package com.minibanking.customer.customer.api;

import com.minibanking.customer.customer.domain.CustomerStatus;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record CustomerRequest(
        @NotBlank @Size(max = 80) String firstName,
        @NotBlank @Size(max = 80) String lastName,
        @NotBlank @Email @Size(max = 160) String email,
        @NotBlank
        @Pattern(regexp = "^[+]?[0-9 ()-]{7,32}$", message = "must be a valid phone number")
        String phone,
        @NotBlank @Size(max = 240) String address,
        CustomerStatus status
) {
}
