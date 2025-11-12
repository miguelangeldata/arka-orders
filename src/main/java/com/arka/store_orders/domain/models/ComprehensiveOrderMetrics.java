package com.arka.store_orders.domain.models;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ComprehensiveOrderMetrics {
    private Long totalOrders;
    private Long acceptedOrders;
    private Long pendingOrders;
    private Double totalSalesAmount;
    private Double averageOrderValue;
    private BestSellerMetrics bestsellers;
}