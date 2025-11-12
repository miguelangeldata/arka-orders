package com.arka.store_orders.infrastructure.adapters.out.webclient.feign;

import com.arka.store_orders.infrastructure.resources.Response.AvailableStockResponse;
import com.arka.store_orders.infrastructure.resources.Response.ReservationResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

@FeignClient(name = "PRODUCTS-SERVICE",path = "/products")
public interface ProductClient {

    @PostMapping("/reserve/{productId}")
    public void reserveStock(
            @PathVariable("productId")Long productId,
            @RequestParam("quantity")Integer quantity);
    @PostMapping("/decrement/{productId}")
    public void decrementStock(
            @PathVariable("productId")Long productId,
            @RequestParam("quantity")Integer quantity);
    @PutMapping("/recover/{productId}")
    public AvailableStockResponse recoveryStock(
            @PathVariable("productId")Long productId,
            @RequestParam("quantity")Integer quantity);
    @GetMapping("/available/{productId}")
    public AvailableStockResponse getAvailableStock(@PathVariable("productId")Long productId);

}
