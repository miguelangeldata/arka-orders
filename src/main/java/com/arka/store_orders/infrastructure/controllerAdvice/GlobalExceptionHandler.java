package com.arka.store_orders.infrastructure.controllerAdvice;

import com.arka.store_orders.infrastructure.controllerAdvice.exceptions.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.Objects;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(InsufficientStockException.class)
    public ResponseEntity<String>handleInsufficientStockException(InsufficientStockException exception){
        return new ResponseEntity<>("Error"+exception.getMessage(), HttpStatus.BAD_REQUEST);
    }
    @ExceptionHandler(InvalidOrderStateException.class)
    public ResponseEntity<String>handleInvalidOrderStateException(InvalidOrderStateException exception){
        return new ResponseEntity<>("Error"+exception.getMessage(), HttpStatus.BAD_REQUEST);
    }
    @ExceptionHandler(ItemUpdateFailedException.class)
    public ResponseEntity<String>handleIteUpdateFailedException(ItemUpdateFailedException exception){
        return new ResponseEntity<>("Error"+exception.getMessage(), HttpStatus.BAD_REQUEST);
    }
    @ExceptionHandler(OrderCreationFailedException.class)
    public ResponseEntity<String>handleOrderCreationFailedException(OrderCreationFailedException exception){
        return new ResponseEntity<>("Error"+exception.getMessage(), HttpStatus.BAD_REQUEST);
    }
    @ExceptionHandler(OrderItemNotFoundException.class)
    public ResponseEntity<String>handleOrderItemNotFoundException(OrderItemNotFoundException exception){
        return new ResponseEntity<>("Error"+exception.getMessage(), HttpStatus.BAD_REQUEST);
    }
    @ExceptionHandler(OrderNotFoundException.class)
    public ResponseEntity<String>handleOrderNotFoundException(OrderNotFoundException exception){
        return new ResponseEntity<>("Error"+exception.getMessage(), HttpStatus.BAD_REQUEST);
    }
    @ExceptionHandler(OrderPaymentCommunicationException.class)
    public ResponseEntity<String>handleOrderPaymentCommunicationException(OrderPaymentCommunicationException exception){
        return new ResponseEntity<>("Error"+exception.getMessage(), HttpStatus.BAD_REQUEST);
    }
    @ExceptionHandler(PaymentFailedException.class)
    public ResponseEntity<String>handlePaymentFailedException(PaymentFailedException exception){
        return new ResponseEntity<>("Error"+exception.getMessage(), HttpStatus.BAD_REQUEST);
    }
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<String> handleValidationExceptions(MethodArgumentNotValidException ex) {
        String errorMessage = "Validation Fail: " + Objects.requireNonNull(ex.getBindingResult().getFieldError()).getDefaultMessage();
        return new ResponseEntity<>(errorMessage, HttpStatus.BAD_REQUEST);
    }
    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> handleAllUncaughtException(Exception ex) {
        String message = ex.getMessage();
        if (message != null && (
                message.contains("favicon.ico") ||
                        message.contains("swagger") ||
                        message.contains("webjars") ||
                        message.contains("api-docs")
        )) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>("Internal Error. Try again later.", HttpStatus.INTERNAL_SERVER_ERROR);
    }

}
