package com.arka.store_orders.infrastructure.adapters.out.webclient.feign;

import com.arka.store_orders.infrastructure.resources.Request.ShippingRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "SHIPPING-SERVICE")
public interface ShippingClient {
    @PostMapping("/shipping")
    public void sendShipping(@RequestBody ShippingRequest request);
}
