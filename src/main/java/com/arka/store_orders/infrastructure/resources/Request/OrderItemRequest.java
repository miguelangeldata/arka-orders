package com.arka.store_orders.infrastructure.resources.Request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "Item to add to the order")
public class OrderItemRequest {
    @Schema(
            description = "Product ID from catalog",
            example = "1",
            required = true
    )
    @NotNull(message = "Product ID is required")
    private Long productId;

    @Schema(
            description = "Quantity of the product to order",
            example = "3",
            minimum = "1"
    )
    @NotNull(message = "Quantity is required")
    @Min(value = 1, message = "Quantity must be at least 1")
    private Integer quantity;

    @Schema(
            description = "Optional unit price. If provided, overrides the current product price. Used for promotions or manual adjustments.",
            example = "49.99",
            minimum = "0"
    )
    @PositiveOrZero(message = "Price cannot be negative")
    private Double price;
}
