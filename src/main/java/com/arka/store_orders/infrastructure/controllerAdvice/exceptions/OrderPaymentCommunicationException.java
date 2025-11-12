package com.arka.store_orders.infrastructure.controllerAdvice.exceptions;

public class OrderPaymentCommunicationException extends RuntimeException {
    public OrderPaymentCommunicationException(String message) {
        super(message);
    }
}
