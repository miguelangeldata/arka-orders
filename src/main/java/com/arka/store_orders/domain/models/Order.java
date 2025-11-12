package com.arka.store_orders.domain.models;

import com.arka.store_orders.infrastructure.controllerAdvice.exceptions.InvalidOrderStateException;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
public class Order {
    private UUID id=UUID.randomUUID();
    private LocalDateTime createAt;
    private List<OrderItem> items;
    private OrderStatus status;
    private Double total;
    private LocalDateTime updateAt;
    private String transactionId;
    private String userId;


    public Order( List<OrderItem> items) {
        this.createAt=LocalDateTime.now();
        this.items = items;
        this.status=OrderStatus.PENDING;
    }


    public void ensureCanModifyItems() {
        if (this.status != OrderStatus.PENDING) {
            throw new InvalidOrderStateException("Order cannot be modified. Status is: " + this.status);
        }
    }
    public void ensureCanRemoveOrder() {
        if (this.status != OrderStatus.PENDING) {
            throw new InvalidOrderStateException("Order cannot be modified. Status is: " + this.status);
        }
    }
    public void ensureAcceptOrder() {
        if (this.status != OrderStatus.WAITINGCONFIRMATION) {
            throw new InvalidOrderStateException("Order cannot be modified. Status is: " + this.status);
        }
    }
    public void ensureShippingOrder() {
        if (this.status != OrderStatus.ACCEPTED) {
            throw new InvalidOrderStateException("Order cannot be modified. Status is: " + this.status);
        }
    }
    public void addItem(OrderItem item){
        this.items.add(item);
    }
    public void calculateTotal(){
        this.total=items.stream().mapToDouble(OrderItem::calculateAmount).sum();
    }

    public void switchToAccepted(){
        this.status=OrderStatus.ACCEPTED;
    }
    public void switchToCanceled(){
        this.status=OrderStatus.CANCELED;
    }
    public void switchToWaitingConfirmation(){
        this.status=OrderStatus.WAITINGCONFIRMATION;
    }
    public void switchToWaitingPaymentInitiation(){
        this.status=OrderStatus.WAITING_PAYMENT_CONFIRMATION;
    }

}
