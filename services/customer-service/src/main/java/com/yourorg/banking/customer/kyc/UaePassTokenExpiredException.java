package com.yourorg.banking.customer.kyc;

public class UaePassTokenExpiredException extends RuntimeException {

    public UaePassTokenExpiredException(String message) {
        super(message);
    }

    public UaePassTokenExpiredException(String message, Throwable cause) {
        super(message, cause);
    }
}
