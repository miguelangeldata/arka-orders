package com.arka.store_orders.infrastructure.controllerAdvice.exceptions;

public class OrderCreationFailedException extends RuntimeException {
    public OrderCreationFailedException(String message) {
        super(message);
    }
    public OrderCreationFailedException(String message, Throwable cause) {
        super(message, cause);
    }
}
