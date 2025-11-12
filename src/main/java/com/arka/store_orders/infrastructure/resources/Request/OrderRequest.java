package com.arka.store_orders.infrastructure.resources.Request;

import com.arka.store_orders.domain.models.OrderItem;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;
@Schema(
        description = "Request to create a new order. Contains a list of items to purchase."
)
public record OrderRequest(
        @Schema(
                description = "List of items to include in the order. Cannot be empty.",
                example = "[{\"productId\": 1, \"quantity\": 2}, {\"productId\": 2, \"quantity\": 1}]"
        )
        @NotEmpty(message = "Order must contain at least one item")
        List<OrderItemRequest> items
) {
}
