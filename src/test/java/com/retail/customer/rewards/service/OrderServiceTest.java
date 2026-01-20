package com.retail.customer.rewards.service;

import com.retail.customer.rewards.dto.OrderRequest;
import com.retail.customer.rewards.dto.OrderResponse;
import com.retail.customer.rewards.entities.Order;
import com.retail.customer.rewards.repository.OrderRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @InjectMocks
    private OrderService orderService;

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private PointsService pointsService;

    @Mock
    private OrderMapper orderMapper;

    @Test
    void createOrder_ReturnsOrderResponse_With_Optional_OrderId() {
        OrderRequest request = OrderRequest.builder()
                .orderId("order1")
                .customerId("customer1")
                .orderDate(LocalDate.of(2023, 1, 1))
                .amount(new BigDecimal("100.00"))
                .build();

        Order order = Order.builder()
                .orderId("order1")
                .customerId("customer1")
                .orderDate(LocalDate.of(2023, 1, 1))
                .amount(new BigDecimal("100.00"))
                .build();

        Order savedOrder = Order.builder()
                .id(1L)
                .orderId("order1")
                .customerId("customer1")
                .orderDate(LocalDate.of(2023, 1, 1))
                .amount(new BigDecimal("100.00"))
                .build();

        OrderResponse expectedResponse = OrderResponse.builder()
                .id(1L)
                .orderId("order1")
                .customerId("customer1")
                .orderDate(LocalDate.of(2023, 1, 1))
                .amount(new BigDecimal("100.00"))
                .points(50)
                .build();

        when(orderMapper.toEntity(ArgumentMatchers.any(OrderRequest.class))).thenReturn(order);
        when(orderRepository.save(order)).thenReturn(savedOrder);
        when(pointsService.calculatePoints(savedOrder.getAmount())).thenReturn(50);

        OrderResponse actual = orderService.createOrder(request);

        assertThat(actual, is(expectedResponse));
    }

    @Test
    void createOrder_ReturnsOrderResponse_Without_Optional_OrderId() {
        OrderRequest request = OrderRequest.builder()
                .customerId("customer1")
                .orderDate(LocalDate.of(2023, 1, 1))
                .amount(new BigDecimal("100.00"))
                .build();

        Order order = Order.builder()
                .customerId("customer1")
                .orderDate(LocalDate.of(2023, 1, 1))
                .amount(new BigDecimal("100.00"))
                .build();

        Order savedOrder = Order.builder()
                .id(1L)
                .customerId("customer1")
                .orderDate(LocalDate.of(2023, 1, 1))
                .amount(new BigDecimal("100.00"))
                .build();

        OrderResponse expectedResponse = OrderResponse.builder()
                .id(1L)
                .customerId("customer1")
                .orderDate(LocalDate.of(2023, 1, 1))
                .amount(new BigDecimal("100.00"))
                .points(50)
                .build();

        when(orderMapper.toEntity(ArgumentMatchers.any(OrderRequest.class))).thenReturn(order);
        when(orderRepository.save(order)).thenReturn(savedOrder);
        when(pointsService.calculatePoints(savedOrder.getAmount())).thenReturn(50);

        OrderResponse actual = orderService.createOrder(request);

        assertThat(actual, is(expectedResponse));
    }

    @Test
    void createOrdersBatch_ReturnsOrderResponses_WhenRequestsAreValid() {
        List<OrderRequest> requests = List.of(
                OrderRequest.builder()
                        .orderId("order1")
                        .customerId("customer1")
                        .orderDate(LocalDate.of(2023, 1, 1))
                        .amount(new BigDecimal("100.00"))
                        .build(),
                OrderRequest.builder()
                        .orderId("order2")
                        .customerId("customer2")
                        .orderDate(LocalDate.of(2023, 1, 1))
                        .amount(new BigDecimal("200.00"))
                        .build()
        );

        List<Order> orders = List.of(
                Order.builder()
                        .orderId("order1")
                        .customerId("customer1")
                        .orderDate(LocalDate.of(2023, 1, 1))
                        .amount(new BigDecimal("100.00"))
                        .build(),
                Order.builder()
                        .orderId("order2")
                        .customerId("customer2")
                        .orderDate(LocalDate.of(2023, 1, 1))
                        .amount(new BigDecimal("200.00"))
                        .build()
        );

        List<Order> savedOrders = List.of(
                Order.builder()
                        .id(1L)
                        .orderId("order1")
                        .customerId("customer1")
                        .orderDate(LocalDate.of(2023, 1, 1))
                        .amount(new BigDecimal("100.00"))
                        .build(),
                Order.builder()
                        .id(2L)
                        .orderId("order2")
                        .customerId("customer2")
                        .orderDate(LocalDate.of(2023, 1, 1))
                        .amount(new BigDecimal("200.00"))
                        .build()
        );

        List<OrderResponse> expectedResponses = List.of(
                OrderResponse.builder()
                        .id(1L)
                        .orderId("order1")
                        .customerId("customer1")
                        .orderDate(LocalDate.of(2023, 1, 1))
                        .amount(new BigDecimal("100.00"))
                        .points(50)
                        .build(),
                OrderResponse.builder()
                        .id(2L)
                        .orderId("order2")
                        .customerId("customer2")
                        .orderDate(LocalDate.of(2023, 1, 1))
                        .amount(new BigDecimal("200.00"))
                        .points(100)
                        .build()
        );

        when(orderMapper.toEntity(ArgumentMatchers.any(OrderRequest.class)))
                .thenReturn(orders.get(0), orders.get(1));
        when(orderRepository.saveAll(orders)).thenReturn(savedOrders);
        when(pointsService.calculatePoints(ArgumentMatchers.any(BigDecimal.class))).thenReturn(50, 100);

        List<OrderResponse> actual = orderService.createOrdersBatch(requests);

        assertThat(actual, is(expectedResponses));
    }
}