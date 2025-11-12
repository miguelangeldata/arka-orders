package com.arka.store_orders.infrastructure.controllerAdvice.exceptions;

public class OrderNotFoundException extends RuntimeException {
    public OrderNotFoundException(String message) {
        super(message);
    }
    public OrderNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
