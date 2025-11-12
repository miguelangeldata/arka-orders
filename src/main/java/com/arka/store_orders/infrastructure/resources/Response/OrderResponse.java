package com.arka.store_orders.infrastructure.resources.Response;

import com.arka.store_orders.domain.models.OrderItem;
import com.arka.store_orders.domain.models.OrderStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;


@AllArgsConstructor
@NoArgsConstructor
@Data
public class OrderResponse {
    @Schema(
            description = "Unique identifier of the order",
            example = "a1b2c3d4-e5f6-7890-g1h2-i3j4k5l6m7n8"
    )
    private UUID id;

    @Schema(
            description = "Timestamp when the order was created",
            example = "2025-04-05T10:30:00"
    )
    private LocalDateTime createAt;

    @Schema(description = "List of items included in the order")
    private List<OrderItem> items;

    @Schema(
            description = "Current status of the order",
            example = "PENDING"
    )
    private OrderStatus status;

    @Schema(
            description = "Total amount of the order (sum of item amounts)",
            example = "149.99"
    )
    private Double total;
}
