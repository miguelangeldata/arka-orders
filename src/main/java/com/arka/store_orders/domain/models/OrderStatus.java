package com.arka.store_orders.domain.models;

import io.swagger.v3.oas.annotations.media.Schema;

public enum OrderStatus {
    @Schema(description = "Order created, can be modified")
    PENDING,

    @Schema(description = "Waiting for user/admin confirmation")
    WAITINGCONFIRMATION,

    @Schema(description = "Order accepted and stock reserved")
    ACCEPTED,

    @Schema(description = "Waiting for payment initiation")
    WAITING_PAYMENT_CONFIRMATION,

    @Schema(description = "Order canceled, stock released")
    CANCELED
}
