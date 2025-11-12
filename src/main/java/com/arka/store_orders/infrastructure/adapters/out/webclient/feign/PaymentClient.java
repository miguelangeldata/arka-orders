package com.arka.store_orders.infrastructure.adapters.out.webclient.feign;

import com.arka.store_orders.infrastructure.resources.Request.PaymentRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@FeignClient(name = "PAYMENTS-SERVICE",path = "/payments")
public interface PaymentClient {
    @PostMapping("/process")
    public String processPayment(
            @RequestBody PaymentRequest paymentRequest);

    @GetMapping("/{transactionId}")
    public boolean validPayment(@PathVariable("transactionId")String id);
}
