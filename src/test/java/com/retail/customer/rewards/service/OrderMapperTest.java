// language: java
package com.retail.customer.rewards.service;

import com.retail.customer.rewards.dto.OrderRequest;
import com.retail.customer.rewards.dto.OrderResponse;
import com.retail.customer.rewards.entities.Order;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class OrderMapperTest {

    private final OrderMapper mapper = new OrderMapper();

    @Test
    void toEntity_mapsAllFields_and_handlesNull() {
        assertNull(mapper.toEntity(null));

        OrderRequest dto = OrderRequest.builder()
                .orderId("ORD-1")
                .customerId("CUST-1")
                .orderDate(LocalDate.of(2025, 1, 1))
                .amount(BigDecimal.valueOf(120))
                .build();

        Order entity = mapper.toEntity(dto);

        assertNotNull(entity);
        assertEquals("ORD-1", entity.getOrderId());
        assertEquals("CUST-1", entity.getCustomerId());
        assertEquals(LocalDate.of(2025, 1, 1), entity.getOrderDate());
        assertEquals(BigDecimal.valueOf(120), entity.getAmount());
    }

    @Test
    void toDto_mapsAllFields_setsPointsZero_and_handlesNull() {
        assertNull(mapper.toDto(null));

        Order order = Order.builder()
                .id(42L)
                .orderId("ORD-2")
                .customerId("CUST-2")
                .orderDate(LocalDate.of(2025, 2, 2))
                .amount(BigDecimal.valueOf(75))
                .build();

        OrderResponse dto = mapper.toDto(order);

        assertNotNull(dto);
        assertEquals(42L, dto.getId());
        assertEquals("ORD-2", dto.getOrderId());
        assertEquals("CUST-2", dto.getCustomerId());
        assertEquals(LocalDate.of(2025, 2, 2), dto.getOrderDate());
        assertEquals(BigDecimal.valueOf(75), dto.getAmount());
        assertEquals(0, dto.getPoints());
    }
}