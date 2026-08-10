package com.minibanking.account.common.error;

public class ConflictException extends RuntimeException {

    public ConflictException(String message) {
        super(message);
    }
}
