package com.arka.store_orders.application.services;

import com.arka.store_orders.application.factory.OrderFactory;
import com.arka.store_orders.domain.models.*;
import com.arka.store_orders.domain.ports.in.OrderUseCases;
import com.arka.store_orders.domain.ports.out.feignclient.PaymentPort;
import com.arka.store_orders.domain.ports.out.feignclient.ProductPort;
import com.arka.store_orders.domain.ports.out.feignclient.ShippingPort;
import com.arka.store_orders.domain.ports.out.persistence.OrderPersistencePort;

import com.arka.store_orders.infrastructure.controllerAdvice.exceptions.*;
import com.arka.store_orders.infrastructure.mapper.OrderItemMapper;
import com.arka.store_orders.infrastructure.resources.Request.ItemQuantityUpdate;
import com.arka.store_orders.infrastructure.resources.Request.OrderRequest;
import com.arka.store_orders.infrastructure.resources.Request.PaymentRequest;
import com.arka.store_orders.infrastructure.resources.Request.ShippingRequest;
import com.arka.store_orders.infrastructure.resources.Response.AvailableStockResponse;
import com.arka.store_orders.infrastructure.resources.Response.OrderItemShipping;
import feign.FeignException;
import io.github.resilience4j.retry.annotation.Retry;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.logging.Logger;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderService implements OrderUseCases {
    private final static Logger log=Logger.getLogger(OrderService.class.getName());


    private final OrderFactory orderFactory;
    private final OrderPersistencePort persistence;
    private final ProductPort productPort;
    private final PaymentPort paymentPort;
    private final ShippingPort shippingPort;
    private final OrderItemMapper mapper;

    @Override
    @Transactional
    public Order createOrder(OrderRequest request) {
        Order newOrder=orderFactory.createOrder(request);
        List<OrderItem> items=newOrder.getItems();
        try {
            for (OrderItem item : items) {
                reserveStockWithRetry(item);
                item.calculateAmount();
                item.setOrderId(newOrder.getId());
            }
            newOrder.calculateTotal();
            log.info("Order created and stock reserved for orderId: {}"+newOrder.getId());
            return persistence.save(newOrder);
        } catch (InsufficientStockException e) {
            rollbackStockReservations(items);
            log.warning("Failed to create order due to: {}"+e.getMessage()+ e);
            throw new OrderCreationFailedException("Failed to create order: " + e.getMessage(), e);
        }
    }

    @Override
    @Transactional
    public Order processOrder(UUID orderId) {
        Order processingOrder = getOrderById(orderId)
                .orElseThrow(() -> new OrderNotFoundException("Order not found by ID: " + orderId));
        processingOrder.calculateTotal();
        try {

            String transactionId = paymentProcess(orderId, processingOrder.getTotal());
            processingOrder.setTransactionId(transactionId);

        } catch (PaymentFailedException e) {
            processingOrder.switchToWaitingPaymentInitiation();
            processingOrder.setTransactionId(null);
        }
        processingOrder.switchToWaitingConfirmation();
        return persistence.save(processingOrder);

    }
    @Override
    @Transactional
    public Order updateOrder(UUID id, Long itemId, ItemQuantityUpdate quantityUpdate) {
        Order processingOrder = getOrderById(id)
                .orElseThrow(() -> new OrderNotFoundException("Order not found by ID: " + id));
        processingOrder.ensureCanModifyItems();
        OrderItem existingItem=getOrderItem(processingOrder,itemId);
        handleSockUpdate(existingItem.getProductId(),existingItem.getQuantity(), quantityUpdate.quantity());
        existingItem.setQuantity(quantityUpdate.quantity());
        existingItem.calculateAmount();
        processingOrder.calculateTotal();
        return persistence.save(processingOrder);

    }

    @Override
    public Order acceptOrder(UUID orderId,String userId) {
        Order acceptedOrder = getOrderById(orderId)
                .orElseThrow(() -> new OrderNotFoundException("Order not found by ID: " + orderId));
        acceptedOrder.ensureAcceptOrder();
        acceptedOrder.setUserId(userId);
        Order savedOrder = persistence.save(acceptedOrder);
        try {
            for (OrderItem item : savedOrder.getItems()) {
                productPort.decrementStock(item.getProductId(), item.getQuantity());
            }
            acceptedOrder.switchToAccepted();
            List<OrderItemShipping> items = getShippingItem(savedOrder);
            ShippingRequest request = new ShippingRequest(
                    savedOrder.getId().toString(),
                    savedOrder.getUserId(),
                    items
            );
            shippingPort.sendOrder(request);
            log.info("Order accepted, stock decremented, and shipping request sent for orderId: "+ orderId);

        } catch (Exception e) {
            log.warning("Fail: "+ orderId+ e);
        }
        log.info("Order accepted and stock decremented for orderId and shipping send: {}"+ orderId);
        return savedOrder;
    }

    @Override
    public void cancelOrder(UUID id) {
        Order existingOrder = getOrderById(id)
                .orElseThrow(() -> new OrderNotFoundException("Order not found by ID: " + id));
        existingOrder.ensureCanRemoveOrder();

        for (OrderItem item : existingOrder.getItems()) {
            productPort.recoveryStock(item.getProductId(), item.getQuantity());
        }
        existingOrder.switchToCanceled();
        persistence.save(existingOrder);
        log.info("Order canceled and stock recovered for orderId: {}"+ id);
    }

    @Override
    public Order deleteItem(UUID orderId, Long itemId) {
        Order existingOrder = getOrderById(orderId)
                .orElseThrow(() -> new OrderNotFoundException("Order not found by ID: " + orderId));
        existingOrder.ensureCanModifyItems();
        OrderItem itemToRemove=existingOrder.getItems().stream().filter(
                item -> item.getId().equals(itemId))
                .findFirst().orElseThrow(()->new IllegalArgumentException("Item Not Found"));
        productPort.recoveryStock(itemToRemove.getProductId(),itemToRemove.getQuantity());
        existingOrder.getItems().remove(itemToRemove);
        existingOrder.calculateTotal();
        return  persistence.save(existingOrder);
    }

    @Override
    public Order addItem(UUID orderId, OrderItem item) {
        Order existingOrder = getOrderById(orderId)
                .orElseThrow(() -> new OrderNotFoundException("Order not found by ID: " + orderId));

        if (!existingOrder.getStatus().equals(OrderStatus.PENDING)) {
            throw new InvalidOrderStateException("Cannot add items: order is not in PENDING status.");
        }
        validateAndReserveStock(item);
        item.setOrderId(orderId);
        item.calculateAmount();
        existingOrder.addItem(item);
        existingOrder.calculateTotal();
        log.info("Item added to orderId: {}" + orderId);
        return persistence.save(existingOrder);
    }

    @Override
    public Optional<Order> getOrderById(UUID id) {
        return Optional.ofNullable(persistence.findByIdWithItems(id)
                .orElseThrow(() -> new OrderNotFoundException(" Order not Found By Id : "+id)));
    }

    @Override
    public List<Order> getOrders() {

        return persistence.findAll();
    }



    @Retry(name = "productReserveRetry", fallbackMethod = "handleReserveFailure")
    private void reserveStockWithRetry(OrderItem item) {
        validateAndReserveStock(item);
    }
    private void validateAndReserveStock(OrderItem item){
        Long id=item.getProductId();
        AvailableStockResponse availableStock=productPort.getAvailableStock(id);
        if (availableStock.availableStock() == null || availableStock.availableStock() < item.getQuantity()) {
            throw new InsufficientStockException("Insufficient stock for SKU: " + id + ". Available: " + availableStock);
        }
        productPort.reserveStock(id, item.getQuantity());
    }
    private void rollbackStockReservations(List<OrderItem> items) {
        for (OrderItem item : items) {
            try {
                productPort.recoveryStock(item.getProductId(), item.getQuantity());
            } catch (Exception ignored) {
                log.warning("Failed to rollback stock for SKU: {}");
            }
        }
    }
    private List<OrderItemShipping> getShippingItem(Order order){
        return order.getItems().stream()
                .map(mapper::orderItemsToShipping).collect(Collectors.toList());
    }
    private void handleSockUpdate(Long productId,Integer oldQuantity,Integer newQuantity){
        int quantityDifference = newQuantity - oldQuantity;

        if (quantityDifference > 0) {
            Integer stockToReserve = quantityDifference;
            AvailableStockResponse stockResponse = productPort.getAvailableStock(productId);
            if (stockResponse.availableStock() < stockToReserve) {
                throw new InsufficientStockException("Insufficient Stock available to update the item. Required: " + stockToReserve);
            }
            productPort.reserveStock(productId, stockToReserve);

        } else if (quantityDifference < 0) {
            Integer stockToRelease = Math.abs(quantityDifference);
            productPort.recoveryStock(productId, stockToRelease);
        }
    }
    private OrderItem getOrderItem(Order order, Long itemId){
        return order.getItems().stream().filter(
                        item -> item.getId().equals(itemId)).findFirst()
                .orElseThrow(() -> new OrderNotFoundException("Item not found by ID: " + itemId));

    }
    private String paymentProcess(UUID orderId,Double total){
        String transactionId;

        try {
            transactionId = callPaymentService(orderId, total);
            log.info("Payment processed for orderId: " + orderId + " with txId: " + transactionId);

        } catch (PaymentFailedException e) {
            log.warning("Payment initiation failed for orderId: " + orderId + " after all retries. Reason: " + e.getMessage());
            throw e;

        } catch (Exception e) {
            log.severe("Unexpected error during payment processing for orderId: " + orderId + e);
            throw new PaymentFailedException("Unexpected error during payment initiation for order " + orderId + ". Reason: " + e.getMessage(), e);
        }

        return transactionId;
    }
    @Retry(name = "paymentInitBackoffRetry", fallbackMethod = "handlePaymentServiceFailure")
    private String callPaymentService(UUID orderId, Double total) {
        PaymentRequest request = new PaymentRequest(orderId, total);
        return paymentPort.processPayment(request);
    }

    private String handlePaymentServiceFailure(UUID orderId, Double total, FeignException e) {
        log.severe("Payment retries exhausted for order " + orderId + ". Error: " + e.getMessage());
        throw new PaymentFailedException("Max retries exceeded contacting payment service", e);
    }
    public ComprehensiveOrderMetrics getComprehensiveMetrics() {
        List<Order> allOrders = getOrders();

        Long totalOrders = totalOrders(allOrders);
        Long acceptedOrders = totalAcceptedOrders(allOrders);
        Long pendingOrders = totalPendingOrders(allOrders);
        Double totalSalesAmount = totalSalesAmount(allOrders);
        Double averageOrderValue = calculateAverageOrderValue(totalSalesAmount, acceptedOrders);

        BestSellerMetrics bestsellers = calculateBestsellerMetrics(allOrders);
        return new ComprehensiveOrderMetrics(
                totalOrders,
                acceptedOrders,
                pendingOrders,
                totalSalesAmount,
                averageOrderValue,
                bestsellers
        );
    }

    private BestSellerMetrics calculateBestsellerMetrics(List<Order> allOrders) {
        Map<Long, Long> productCounts = getProductSalesCounts(allOrders);

        if (productCounts.isEmpty()) {
            return new BestSellerMetrics(null, null, 0L, 0L);
        }
        Map.Entry<Long, Long> mostSold = findMostSold(productCounts);
        Map.Entry<Long, Long> leastSold = findLeastSold(productCounts);

        return new BestSellerMetrics(
                mostSold.getKey(),
                leastSold.getKey(),
                mostSold.getValue(),
                leastSold.getValue()
        );
    }

    private Long totalOrders(List<Order> allOrders) {
        return (long) allOrders.size();
    }

    private Long totalAcceptedOrders(List<Order> allOrders) {
        return allOrders.stream()
                .filter(order -> order.getStatus().equals(OrderStatus.ACCEPTED))
                .count();
    }

    private Long totalPendingOrders(List<Order> allOrders) {
        return allOrders.stream()
                .filter(order -> order.getStatus().equals(OrderStatus.PENDING))
                .count();
    }

    private Double totalSalesAmount(List<Order> allOrders) {
        return allOrders.stream()

                .filter(order -> order.getStatus().equals(OrderStatus.ACCEPTED))
                .mapToDouble(Order::getTotal)
                .sum();
    }

    private Double calculateAverageOrderValue(Double totalSalesAmount, Long acceptedOrders) {
        return (acceptedOrders != null && acceptedOrders > 0) ?
                (totalSalesAmount / acceptedOrders) : 0.0;
    }


    private Map<Long, Long> getProductSalesCounts(List<Order> allOrders) {
        return allOrders.stream()
                .flatMap(order -> order.getItems().stream())
                .collect(Collectors.groupingBy(
                        OrderItem::getProductId,
                        Collectors.summingLong(OrderItem::getQuantity)
                ));
    }
    private Map.Entry<Long, Long> findMostSold(Map<Long, Long> productCounts) {
        return productCounts.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .get();
    }
    private Map.Entry<Long, Long> findLeastSold(Map<Long, Long> productCounts) {
        return productCounts.entrySet().stream()
                .min(Map.Entry.comparingByValue())
                .get();
    }

}
