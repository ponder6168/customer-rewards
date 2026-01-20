package com.retail.customer.rewards.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.retail.customer.rewards.dto.OrderRequest;
import com.retail.customer.rewards.repository.OrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class OrdersAndRewardsIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private OrderRepository orderRepository;

    @BeforeEach
    void beforeEach() {
        orderRepository.deleteAll();
    }

    @Test
    @DisplayName("POST /orders returns persisted order with computed points")
    void createOrderAndReturnPoints() throws Exception {
        LocalDate orderDate = LocalDate.of(2023, 1, 1);
        OrderRequest request = OrderRequest.builder()
                .orderId("t-integ-1")
                .customerId("C-test")
                .orderDate(orderDate)
                .amount(new BigDecimal("120.00"))
                .build();


        mockMvc.perform(post("/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(jsonPath("$.orderId", is(request.getOrderId())))
                .andExpect(jsonPath("$.customerId", is(request.getCustomerId())))
                .andExpect(jsonPath("$.orderDate", is(orderDate.toString())))
                .andExpect(jsonPath("$.amount", is(120.00)))
                .andExpect(jsonPath("$.points", is(90)));
    }

    @Test
    @DisplayName("POST /orders/batch and then GET /rewards/{customerId} returns aggregated totals")
    void batchCreateAndGetRewardsForCustomer() throws Exception {
        LocalDate earliestOrderDate = LocalDate.of(2023, 1, 1);
        LocalDate latestOrderDate = LocalDate.of(2023, 2, 1);
        OrderRequest request1 = OrderRequest.builder()
                .orderId("t-batch-1")
                .customerId("C-batch")
                .orderDate(earliestOrderDate)
                .amount(new BigDecimal("120.00")) // 90 pts
                .build();
        OrderRequest request2 = OrderRequest.builder()
                .orderId("t-batch-2")
                .customerId("C-batch")
                .orderDate(latestOrderDate)
                .amount(new BigDecimal("75.00")) // 25 pts
                .build();

        // send batch
        mockMvc.perform(post("/orders/batch")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new OrderRequest[]{request1, request2})))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[*].orderId", containsInAnyOrder(request1.getOrderId(), request2.getOrderId())))
                .andExpect(jsonPath("$[*].customerId", containsInAnyOrder(request1.getCustomerId(), request2.getCustomerId())))
                .andExpect(jsonPath("$[*].orderDate", containsInAnyOrder(earliestOrderDate.toString(), latestOrderDate.toString())))
                .andExpect(jsonPath("$[*].amount", containsInAnyOrder(120.00, 75.00)))
                .andExpect(jsonPath("$[*].points", containsInAnyOrder(90, 25)));

        // get rewards for the customer
        mockMvc.perform(get("/rewards/{customerId}", request1.getCustomerId())
                        .param("startDate", earliestOrderDate.toString())
                        .param("endDate", latestOrderDate.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.customerId", is(request1.getCustomerId())))
                .andExpect(jsonPath("$.totalPoints", is(115)))
                .andExpect(jsonPath("$.monthlyPoints", aMapWithSize(greaterThanOrEqualTo(1))))
                .andExpect(jsonPath("$.monthlyPoints.*", hasItems(90, 25)));
    }
    @Test
    @DisplayName("POST /orders/batch for multiple customers then GET /rewards returns aggregated totals for all customers")
    void batchCreateMultipleCustomersAndGetRewardsForAllCustomers() throws Exception {
        LocalDate earliestOrderDate = LocalDate.of(2023, 1, 5);
        LocalDate middleOrderDate1 = LocalDate.of(2023, 2, 10);
        LocalDate middleOrderDate2 = LocalDate.of(2023, 3, 3);
        LocalDate latestOrderDate = LocalDate.of(2023, 4, 4);
        String customer1Id = "C-1";
        String customer2Id = "C-2";
        OrderRequest customer1Order1 = OrderRequest.builder()
                .orderId("mc-1-1")
                .customerId(customer1Id)
                .orderDate(earliestOrderDate)
                .amount(new BigDecimal("120.00")) // 90 pts
                .build();

        OrderRequest customer1Order2 = OrderRequest.builder()
                .orderId("mc-1-2")
                .customerId(customer1Id)
                .orderDate(middleOrderDate1)
                .amount(new BigDecimal("80.00")) // 30 pts
                .build();

        OrderRequest customer2Order1 = OrderRequest.builder()
                .orderId("mc-2-1")
                .customerId(customer2Id)
                .orderDate(middleOrderDate2)
                .amount(new BigDecimal("75.00")) // 25 pts
                .build();

        OrderRequest Customer2Order2 = OrderRequest.builder()
                .orderId("mc-2-2")
                .customerId(customer2Id)
                .orderDate(latestOrderDate)
                .amount(new BigDecimal("200.00")) // 250 pts
                .build();

        // send batch of orders for multiple customers
        mockMvc.perform(post("/orders/batch")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new OrderRequest[]{customer1Order1, customer1Order2, customer2Order1, Customer2Order2})))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(4)));

        // request rewards for all customers within date range
        mockMvc.perform(get("/rewards")
                        .param("startDate", earliestOrderDate.toString())
                        .param("endDate", latestOrderDate.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[*].customerId", containsInAnyOrder(customer1Id, customer2Id)))
                .andExpect(jsonPath("$[*].totalPoints", containsInAnyOrder(120, 275)))
                .andExpect(jsonPath("$[*].monthlyPoints", everyItem(aMapWithSize(greaterThanOrEqualTo(1)))))
                .andExpect(jsonPath("$[*].monthlyPoints.*", hasItems(90, 30, 25, 250)));
    }

}