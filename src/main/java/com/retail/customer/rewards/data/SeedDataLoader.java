package com.retail.customer.rewards.data;

import com.retail.customer.rewards.entities.Order;
import com.retail.customer.rewards.repository.OrderRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Loads sample orders on startup (only when the DB is empty).
 */
@Component
@Profile("!test")
public class SeedDataLoader implements CommandLineRunner {

    private final OrderRepository orderRepository;

    public SeedDataLoader(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    @Override
    public void run(String... args) {
        if (orderRepository.count() > 0) {
            return; // already seeded
        }

List<Order> orders = List.of(
        Order.builder().orderId("t1").customerId("C1").orderDate(LocalDate.of(2025, 9, 1)).amount(new BigDecimal("120.00")).build(),
        Order.builder().orderId("t2").customerId("C1").orderDate(LocalDate.of(2025, 9, 15)).amount(new BigDecimal("75.00")).build(),
        Order.builder().orderId("t3").customerId("C1").orderDate(LocalDate.of(2025, 10, 2)).amount(new BigDecimal("110.00")).build(),
        Order.builder().orderId("t4").customerId("C1").orderDate(LocalDate.of(2025, 11, 20)).amount(new BigDecimal("45.00")).build(),
        Order.builder().orderId("t5").customerId("C2").orderDate(LocalDate.of(2025, 9, 5)).amount(new BigDecimal("50.00")).build(),
        Order.builder().orderId("t6").customerId("C2").orderDate(LocalDate.of(2025, 9, 7)).amount(new BigDecimal("51.00")).build(),
        Order.builder().orderId("t7").customerId("C2").orderDate(LocalDate.of(2025, 10, 10)).amount(new BigDecimal("200.00")).build(),
        Order.builder().orderId("t8").customerId("C2").orderDate(LocalDate.of(2025, 11, 11)).amount(new BigDecimal("99.99")).build(),
        Order.builder().orderId("t9").customerId("C3").orderDate(LocalDate.of(2025, 9, 30)).amount(new BigDecimal("120.50")).build(),
        Order.builder().orderId("t10").customerId("C3").orderDate(LocalDate.of(2025, 10, 21)).amount(new BigDecimal("60.00")).build(),
        Order.builder().orderId("t11").customerId("C3").orderDate(LocalDate.of(2025, 11, 2)).amount(new BigDecimal("100.00")).build(),
        Order.builder().orderId("t12").customerId("C3").orderDate(LocalDate.of(2025, 11, 25)).amount(new BigDecimal("101.00")).build(),
        Order.builder().orderId("t13").customerId("C4").orderDate(LocalDate.of(2025, 10, 15)).amount(new BigDecimal("49.99")).build(),
        Order.builder().orderId("t14").customerId("C4").orderDate(LocalDate.of(2025, 10, 16)).amount(new BigDecimal("150.00")).build()
);

        orderRepository.saveAll(orders);
    }
}