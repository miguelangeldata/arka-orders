package com.arka.store_orders.infrastructure.controllerAdvice.exceptions;

public class InvalidOrderStateException extends RuntimeException {
    public InvalidOrderStateException(String message) {
        super(message);
    }
    public InvalidOrderStateException(String message, Throwable cause) {
        super(message, cause);
    }
}
