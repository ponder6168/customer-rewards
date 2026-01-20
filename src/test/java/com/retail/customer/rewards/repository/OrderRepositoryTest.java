package com.retail.customer.rewards.repository;

import com.retail.customer.rewards.entities.Order;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class OrderRepositoryTest {

    @Autowired
    private OrderRepository orderRepository;

    @Test
    void findsOrdersByCustomerAndDateRange() {
        Order o1 = Order.builder()
                .customerId("cust1")
                .orderDate(LocalDate.of(2025, 1, 10))
                .amount(BigDecimal.valueOf(100L))
                .build();
        orderRepository.save(o1);

        Order o2 = Order.builder()
                .customerId("cust1")
                .orderDate(LocalDate.of(2025, 2, 10))
                .amount(BigDecimal.valueOf(50L))
                .build();
        orderRepository.save(o2);

        List<Order> results = orderRepository.findByCustomerIdAndOrderDateBetween(
                "cust1", LocalDate.of(2025, 1, 1), LocalDate.of(2025, 1, 31));

        assertThat(results).hasSize(1);
        assertThat(results.get(0).getOrderDate()).isEqualTo(LocalDate.of(2025, 1, 10));
    }
}