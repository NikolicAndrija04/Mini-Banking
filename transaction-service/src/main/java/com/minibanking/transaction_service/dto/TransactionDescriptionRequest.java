package com.minibanking.transaction_service.dto;

import jakarta.validation.constraints.NotBlank;

public class TransactionDescriptionRequest {

    @NotBlank(message = "Description is required")
    private String description;

    public TransactionDescriptionRequest() {
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}