package com.retail.customer.rewards.controller;

import com.retail.customer.rewards.dto.OrderRequest;
import com.retail.customer.rewards.dto.OrderResponse;
import com.retail.customer.rewards.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/orders")
@Tag(name = "Orders", description = "Create and manage orders")
@RequiredArgsConstructor
public class OrdersController {

    private final OrderService orderService;


    @Operation(summary = "Create a single order", description = "Create a new order and return the persisted order with computed reward points")
    @PostMapping
    public ResponseEntity<OrderResponse> createOrder(@Valid @RequestBody OrderRequest request) {
        OrderResponse response = orderService.createOrder(request);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Create orders in batch", description = "Create multiple orders in a single request")
    @PostMapping("/batch")
    public ResponseEntity<List<OrderResponse>> createOrdersBatch(@Valid @RequestBody List<OrderRequest> requests) {
        List<OrderResponse> responses = orderService.createOrdersBatch(requests);
        return ResponseEntity.ok(responses);
    }
}