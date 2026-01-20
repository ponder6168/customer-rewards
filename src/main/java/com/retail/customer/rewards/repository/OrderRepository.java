package com.retail.customer.rewards.repository;

import com.retail.customer.rewards.entities.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    // Find orders within an inclusive date range
    List<Order> findByOrderDateBetween(LocalDate startInclusive, LocalDate endInclusive);

    // Find orders for a specific customer within an inclusive date range
    List<Order> findByCustomerIdAndOrderDateBetween(String customerId, LocalDate startInclusive, LocalDate endInclusive);
}