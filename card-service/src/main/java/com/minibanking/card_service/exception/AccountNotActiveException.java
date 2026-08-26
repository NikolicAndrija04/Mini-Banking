package com.minibanking.card_service.exception;

public class AccountNotActiveException extends RuntimeException {

    public AccountNotActiveException(Long id) {
        super("Account with id " + id + " is not active");
    }
}