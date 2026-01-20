package com.retail.customer.rewards.service;

import com.retail.customer.rewards.dto.OrderRequest;
import com.retail.customer.rewards.dto.OrderResponse;
import com.retail.customer.rewards.entities.Order;
import com.retail.customer.rewards.repository.OrderRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class OrderService {
    private final OrderRepository orderRepository;
    private final PointsService pointsService;
    private final OrderMapper orderMapper;

    public OrderService(OrderRepository orderRepository, PointsService pointsService, OrderMapper orderMapper) {
        this.orderRepository = orderRepository;
        this.pointsService = pointsService;
        this.orderMapper = orderMapper;
    }

    public OrderResponse createOrder(OrderRequest request) {
        Order order = orderMapper.toEntity(request);
        Order saved = orderRepository.save(order);
        return getOrderResponse(saved);
    }

    public List<OrderResponse> createOrdersBatch(List<OrderRequest> requests) {
        List<Order> entities = requests.stream().map(orderMapper::toEntity).collect(Collectors.toList());
        List<Order> saved = orderRepository.saveAll(entities);
        return saved.stream().map(order -> this.getOrderResponse(order)).collect(Collectors.toList());
   }

    private OrderResponse getOrderResponse(Order order) {
        int points = pointsService.calculatePoints(order.getAmount());
        return OrderResponse.builder()
                .id(order.getId())
                .orderId(order.getOrderId())
                .customerId(order.getCustomerId())
                .orderDate(order.getOrderDate())
                .amount(order.getAmount())
                .points(points)
                .build();
    }
}
