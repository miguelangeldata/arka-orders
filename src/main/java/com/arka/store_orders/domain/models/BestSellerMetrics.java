package com.arka.store_orders.domain.models;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class BestSellerMetrics {
    private Long mostSoldProductId;
    private Long leastSoldProductId;

    private Long mostSoldCount;
    private Long leastSoldCount;
}
