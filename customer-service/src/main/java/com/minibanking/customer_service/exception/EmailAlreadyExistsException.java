package com.minibanking.customer_service.exception;

public class EmailAlreadyExistsException extends RuntimeException {

    public EmailAlreadyExistsException(String email) {
        super("Customer with email " + email + " already exists");
    }
}