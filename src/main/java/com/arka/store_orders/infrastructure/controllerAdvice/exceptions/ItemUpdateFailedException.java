package com.arka.store_orders.infrastructure.controllerAdvice.exceptions;

public class ItemUpdateFailedException extends RuntimeException {
    public ItemUpdateFailedException(String message) {
        super(message);
    }
    public ItemUpdateFailedException(String message, Throwable cause) {
        super(message, cause);
    }
}
