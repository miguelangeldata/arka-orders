package com.arka.store_orders.domain.ports.in;

import com.arka.store_orders.domain.models.ComprehensiveOrderMetrics;
import com.arka.store_orders.domain.models.Order;
import com.arka.store_orders.domain.models.OrderItem;
import com.arka.store_orders.infrastructure.resources.Request.ItemQuantityUpdate;
import com.arka.store_orders.infrastructure.resources.Request.OrderRequest;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface OrderUseCases {
    Order createOrder(OrderRequest request);
    Order processOrder(UUID orderId);
    Order acceptOrder(UUID orderId,String userId);
    void cancelOrder(UUID id);
    Order updateOrder(UUID id, Long itemId, ItemQuantityUpdate quantityUpdate);
    Order deleteItem(UUID orderId,Long itemId);
    Order addItem(UUID orderId,OrderItem item);

    Optional<Order> getOrderById(UUID id);
    List<Order> getOrders();
    ComprehensiveOrderMetrics getComprehensiveMetrics();

}
