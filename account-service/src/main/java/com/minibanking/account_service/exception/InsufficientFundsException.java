package com.minibanking.account_service.exception;

public class InsufficientFundsException extends RuntimeException {

    public InsufficientFundsException(Long id) {
        super("Account with id " + id + " does not have sufficient funds");
    }
}