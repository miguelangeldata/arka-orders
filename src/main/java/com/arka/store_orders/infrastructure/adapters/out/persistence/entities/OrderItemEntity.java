package com.arka.store_orders.infrastructure.adapters.out.persistence.entities;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity(name = "items")
public class OrderItemEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Schema(description = "Internal ID of the order item", example = "101")
    private Long id;

    @Schema(description = "Product ID from catalog", example = "1")
    private Long productId;

    @Schema(description = "Quantity ordered", example = "2")
    private Integer quantity;

    @Schema(description = "Unit price at the time of order", example = "49.99")
    private Double price;

    @Schema(description = "Total for this item (quantity × price)", example = "99.98")
    private Double amount;
    @Column(name = "order_id")

    private UUID orderId;
}
