package com.arka.store_orders.infrastructure.adapters.in.web;

import com.arka.store_orders.domain.models.ComprehensiveOrderMetrics;
import com.arka.store_orders.domain.ports.in.OrderUseCases;
import com.arka.store_orders.infrastructure.mapper.OrderItemMapper;
import com.arka.store_orders.infrastructure.mapper.OrderMapper;
import com.arka.store_orders.infrastructure.resources.Request.ItemQuantityUpdate;
import com.arka.store_orders.infrastructure.resources.Request.OrderItemRequest;
import com.arka.store_orders.infrastructure.resources.Request.OrderRequest;
import com.arka.store_orders.infrastructure.resources.Response.OrderResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/orders")
@RequiredArgsConstructor
@Tag(name = "Orders", description = "Full order lifecycle management: creation, modification, acceptance, cancellation, and retrieval.")
public class OrderController {
    private final OrderUseCases useCases;
    private final OrderMapper mapper;
    private final OrderItemMapper itemMapper;

    @PostMapping
    @Operation(
            summary = "Create a new order",
            description = "Registers a new order with items and initial status PENDING. Total is calculated automatically.")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "Order created successfully",
                    content = @Content(schema = @Schema(implementation = OrderResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid input (e.g., empty items, invalid quantity)"),
            @ApiResponse(responseCode = "404", description = "Product not found"),
            @ApiResponse(responseCode = "409", description = "Stock reservation failed")
    })
    public ResponseEntity<OrderResponse> createOrder(@RequestBody OrderRequest request){
        OrderResponse orderResponse=mapper.domainToResponse(useCases.createOrder(request));
        return ResponseEntity.ok(orderResponse);
    }

    @PostMapping("/process/{orderId}")
    @Operation(
            summary = "Process an order",
            description = "Triggers order processing: reserves stock, validates payment, updates status.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Order processed successfully"),
            @ApiResponse(responseCode = "404", description = "Order not found"),
            @ApiResponse(responseCode = "409", description = "Insufficient stock or invalid state transition")
    })
    public ResponseEntity<OrderResponse> processOrder(@PathVariable("orderId")UUID orderId){
        OrderResponse orderResponse=mapper.domainToResponse(useCases.processOrder(orderId));
        return ResponseEntity.ok(orderResponse);
    }
    @PutMapping("/update/{orderId}/{itemId}")
    @Operation(
            summary = "Update item quantity",
            description = "Modifies the quantity of a specific item in the order. Only allowed in PENDING state.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Item quantity updated"),
            @ApiResponse(responseCode = "400", description = "Invalid quantity (e.g., negative or zero)"),
            @ApiResponse(responseCode = "404", description = "Order or item not found"),
            @ApiResponse(responseCode = "409", description = "Order is not in PENDING state")
    })
    public ResponseEntity<OrderResponse> updateOrder(
            @PathVariable("orderId")UUID orderId,
            @PathVariable("itemId")Long itemId,
            @RequestBody ItemQuantityUpdate quantityUpdate){
        OrderResponse orderResponse=mapper.domainToResponse(useCases.updateOrder(orderId,itemId,quantityUpdate));
        return ResponseEntity.ok(orderResponse);
    }

    @PutMapping("/add-item/{orderId}")
    @Operation(
            summary = "Add item to existing order",
            description = "Adds a new product to an existing order. Only allowed in PENDING state.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Item added successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid product or quantity"),
            @ApiResponse(responseCode = "404", description = "Order or product not found"),
            @ApiResponse(responseCode = "409", description = "Cannot modify non-PENDING order")
    })
    public ResponseEntity<OrderResponse> addItem(@PathVariable("orderId")UUID orderId,@RequestBody OrderItemRequest item){
        OrderResponse orderResponse=mapper.domainToResponse(useCases.addItem(orderId, itemMapper.requestToDomain(item)));
        return ResponseEntity.ok(orderResponse);
    }
    @PutMapping("/remove-item/{orderId}/{itemId}")
    @Operation(
            summary = "Remove item from order",
            description = "Removes a specific item from the order. Only allowed in PENDING state.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Item removed successfully"),
            @ApiResponse(responseCode = "404", description = "Order or item not found"),
            @ApiResponse(responseCode = "409", description = "Cannot modify non-PENDING order")
    })
    public ResponseEntity<OrderResponse> removeItem(@PathVariable("orderId")UUID orderId,@PathVariable("itemId")Long itemId){
        OrderResponse orderResponse=mapper.domainToResponse(useCases.deleteItem(orderId, itemId));
        return ResponseEntity.ok(orderResponse);
    }

    @PostMapping("/accept/{orderId}/{userId}")
    @Operation(
            summary = "Accept order",
            description = "Changes order status to ACCEPTED. Triggers stock reservation and payment flow.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Order accepted"),
            @ApiResponse(responseCode = "404", description = "Order or user not found"),
            @ApiResponse(responseCode = "409", description = "Invalid state transition (must be WAITING_CONFIRMATION)")
    })
    public ResponseEntity<OrderResponse> acceptOrder(
            @PathVariable("orderId")UUID orderId,
            @PathVariable("userId")String userId){
        OrderResponse orderResponse=mapper.domainToResponse(useCases.acceptOrder(orderId,userId));
        return ResponseEntity.ok(orderResponse);
    }
    @PutMapping("/cancel/{orderId}")
    @Operation(
            summary = "Cancel order",
            description = "Cancels the order and releases reserved stock. Only allowed before shipping.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Order canceled successfully"),
            @ApiResponse(responseCode = "404", description = "Order not found"),
            @ApiResponse(responseCode = "409", description = "Cannot cancel order in current state")
    })
    public ResponseEntity<String> cancelOrder(@PathVariable("orderId")UUID orderId){
        useCases.cancelOrder(orderId);
        String message="Order was canceled Successfully";
        return ResponseEntity.ok(message);
    }

    @GetMapping
    @Operation(
            summary = "Get all orders",
            description = "Retrieves a list of all orders with current status and details."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "List of orders",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = OrderResponse.class)))
            )
    })
    public ResponseEntity<List<OrderResponse>> getAllOrder(){
        List<OrderResponse> orders=useCases.getOrders().stream()
                .map(mapper::domainToResponse).toList();
        return ResponseEntity.ok(orders);
    }

    @GetMapping("/{orderId}")
    @Operation(
            summary = "Get order by ID",
            description = "Retrieves detailed information about a specific order.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Order found"),
            @ApiResponse(responseCode = "404", description = "Order not found")
    })
    public ResponseEntity<OrderResponse>getById(@PathVariable("orderId")UUID orderId){
        return useCases.getOrderById(orderId).map(mapper::domainToResponse)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    @GetMapping("/metrics")
    public ResponseEntity<ComprehensiveOrderMetrics>getMetrics(){
        ComprehensiveOrderMetrics comprehensiveOrderMetrics=useCases.getComprehensiveMetrics();
        return ResponseEntity.ok(comprehensiveOrderMetrics);
    }

}
