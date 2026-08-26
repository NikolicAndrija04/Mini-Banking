package com.minibanking.card_service.dto;

import com.minibanking.card_service.entity.CardType;
import jakarta.validation.constraints.NotNull;

public class CardRequest {

    @NotNull(message = "Account id is required")
    private Long accountId;

    @NotNull(message = "Card type is required")
    private CardType type;

    public CardRequest() {
    }

    public Long getAccountId() {
        return accountId;
    }

    public void setAccountId(Long accountId) {
        this.accountId = accountId;
    }

    public CardType getType() {
        return type;
    }

    public void setType(CardType type) {
        this.type = type;
    }
}