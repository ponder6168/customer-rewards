package com.retail.customer.rewards.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.retail.customer.rewards.dto.OrderRequest;
import com.retail.customer.rewards.dto.OrderResponse;
import com.retail.customer.rewards.service.OrderService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@WebMvcTest(OrdersController.class)
class OrdersControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private OrderService orderService;

    @Test
    void createOrder_ReturnsOrderResponse_With_Optional_OrderId() throws Exception {
        OrderRequest request = new OrderRequest(
                "order1",
                "customer1",
                LocalDate.of(2023, 1, 1),
                new BigDecimal("100.00")
        );

        OrderResponse expectedResponse = OrderResponse.builder()
                .id(1L)
                .orderId("order1")
                .customerId("customer1")
                .orderDate(LocalDate.of(2023, 1, 1))
                .amount(new BigDecimal("100.00"))
                .points(50)
                .build();

        when(orderService.createOrder(ArgumentMatchers.eq(request))).thenReturn(expectedResponse);

        var mvcResult = mockMvc.perform(post("/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andReturn();

        OrderResponse actual = objectMapper.readValue(mvcResult.getResponse().getContentAsString(), OrderResponse.class);
        assertThat(actual, is(expectedResponse));
    }

    @Test
    void createOrder_ReturnsOrderResponse_Without_Optional_OrderId() throws Exception {
        OrderRequest request = OrderRequest.builder()
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

        when(orderService.createOrder(ArgumentMatchers.eq(request))).thenReturn(expectedResponse);

        var mvcResult = mockMvc.perform(post("/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andReturn();

        OrderResponse actual = objectMapper.readValue(mvcResult.getResponse().getContentAsString(), OrderResponse.class);
        assertThat(actual, is(expectedResponse));
    }

    @Test
    void createOrdersBatch_ReturnsOrderResponses_WhenRequestsAreValid() throws Exception {
        List<OrderRequest> requests = List.of(
                new OrderRequest(
                        "order1",
                        "customer1",
                        LocalDate.of(2023, 1, 1),
                        new BigDecimal("100.00")
                ), new OrderRequest(
                        "order2",
                        "customer2",
                        LocalDate.of(2023, 1, 1),
                        new BigDecimal("200.00")
                )
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

        when(orderService.createOrdersBatch(ArgumentMatchers.eq(requests))).thenReturn(expectedResponses);

        var mvcResult = mockMvc.perform(post("/orders/batch")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requests)))
                .andExpect(status().isOk())
                .andReturn();

        List<OrderResponse> actual = objectMapper.readValue(mvcResult.getResponse().getContentAsString(),
                new TypeReference<>() {
                });
        assertThat(actual, is(expectedResponses));
    }

    @Test
    void createOrder_ReturnsBadRequest_WhenCustomerIdIsInvalid() throws Exception {
        OrderRequest invalidRequest = OrderRequest.builder()
                .customerId("")
                .orderDate(LocalDate.of(2023, 1, 1))
                .amount(new BigDecimal("100.00"))
                .build();

        mockMvc.perform(post("/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createOrdersBatch_ReturnsBadRequest_WhenAnyRequestIsInvalid() throws Exception {
        // Construct one valid and one invalid request
        OrderRequest valid = OrderRequest.builder()
                .customerId("customer1")
                .orderDate(LocalDate.of(2023, 1, 1))
                .amount(new BigDecimal("100.00"))
                .build();
        OrderRequest invalid = OrderRequest.builder()
                .customerId("")
                .orderDate(LocalDate.of(2023, 1, 1))
                .amount(new BigDecimal("200.00"))
                .build();

        List<OrderRequest> requests = List.of(valid, invalid);

        mockMvc.perform(post("/orders/batch")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requests)))
                .andExpect(status().isBadRequest());
    }
}