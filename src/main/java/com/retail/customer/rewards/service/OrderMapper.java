package com.retail.customer.rewards.service;

import com.retail.customer.rewards.dto.OrderRequest;
import com.retail.customer.rewards.dto.OrderResponse;
import com.retail.customer.rewards.entities.Order;
import org.springframework.stereotype.Component;

@Component
public class OrderMapper {

public Order toEntity(OrderRequest dto) {
                    if (dto == null) return null;
                    return Order.builder()
                            .orderId(dto.getOrderId())
                            .customerId(dto.getCustomerId())
                            .orderDate(dto.getOrderDate())
                            .amount(dto.getAmount())
                            .build();
                }
    public OrderResponse toDto(Order order) {
        if (order == null) return null;
        return OrderResponse.builder()
                .id(order.getId())
                .orderId(order.getOrderId())
                .customerId(order.getCustomerId())
                .orderDate(order.getOrderDate())
                .amount(order.getAmount())
                .points(0) // points computed elsewhere; set appropriately if you have a PointsService
                .build();
    }
}