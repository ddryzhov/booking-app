package com.daniil.bookingapp.exception;

public class PendingPaymentException extends RuntimeException {
    public PendingPaymentException(String message) {
        super(message);
    }
}
