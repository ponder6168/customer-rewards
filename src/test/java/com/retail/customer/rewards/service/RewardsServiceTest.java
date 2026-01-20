package com.retail.customer.rewards.service;

import com.retail.customer.rewards.dto.CustomerMonthlyRewardsDto;
import com.retail.customer.rewards.entities.Order;
import com.retail.customer.rewards.repository.OrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.*;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class RewardsServiceTest {
    private OrderRepository orderRepository;
    private PointsService pointsService;
    private Clock clock;
    private RewardsService rewardsService;

    @BeforeEach
    void setUp() {
        orderRepository = mock(OrderRepository.class);
        pointsService = mock(PointsService.class);
        clock = Clock.fixed(Instant.parse("2025-12-15T00:00:00Z"), ZoneId.of("UTC"));
        rewardsService = new RewardsService(orderRepository, pointsService, clock);
    }

    @Test
    void getRewards_aggregatesPointsPerCustomerAndMonth_andSortsByCustomerId() {
        Order order1 = mock(Order.class);
        when(order1.getCustomerId()).thenReturn("custA");
        when(order1.getOrderDate()).thenReturn(LocalDate.of(2023, Month.JANUARY, 5));
        when(order1.getAmount()).thenReturn(BigDecimal.valueOf(120.0));

        Order order2 = mock(Order.class);
        when(order2.getCustomerId()).thenReturn("custA");
        when(order2.getOrderDate()).thenReturn(LocalDate.of(2023, Month.FEBRUARY, 1));
        when(order2.getAmount()).thenReturn(BigDecimal.valueOf(60.0));

        Order order3 = mock(Order.class);
        when(order3.getCustomerId()).thenReturn("custB");
        when(order3.getOrderDate()).thenReturn(LocalDate.of(2023, Month.JANUARY, 10));
        when(order3.getAmount()).thenReturn(BigDecimal.valueOf(75.0));

        when(orderRepository.findByOrderDateBetween(any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(Arrays.asList(order1, order2, order3));

        when(pointsService.calculatePoints(BigDecimal.valueOf(120.0))).thenReturn(90);
        when(pointsService.calculatePoints(BigDecimal.valueOf(60.0))).thenReturn(30);
        when(pointsService.calculatePoints(BigDecimal.valueOf(75.0))).thenReturn(25);

        List<CustomerMonthlyRewardsDto> results =
                rewardsService.getRewards(LocalDate.of(2023, 1, 1), LocalDate.of(2023, 3, 31));

        // two distinct customers expected, sorted by customerId
        assertEquals(2, results.size());
        assertEquals("custA", results.get(0).getCustomerId());
        assertEquals("custB", results.get(1).getCustomerId());

        // pointsService should have been invoked for each order amount
        verify(pointsService, times(1)).calculatePoints(BigDecimal.valueOf(120.0));
        verify(pointsService, times(1)).calculatePoints(BigDecimal.valueOf(60.0));
        verify(pointsService, times(1)).calculatePoints(BigDecimal.valueOf(75.0));
    }

    @Test
    void getRewardsForCustomer_returnsDtoWithCustomerId_andUsesRepositoryAndPointsService() {
        String customerId = "singleCust";
        Order order = mock(Order.class);
        when(order.getOrderDate()).thenReturn(LocalDate.of(2023, Month.MARCH, 3));
        when(order.getAmount()).thenReturn(BigDecimal.valueOf(200.0));

        when(orderRepository.findByCustomerIdAndOrderDateBetween(eq(customerId), any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(List.of(order));

        when(pointsService.calculatePoints(BigDecimal.valueOf(200.0))).thenReturn(150);

        CustomerMonthlyRewardsDto dto = rewardsService.getRewardsForCustomer(customerId, LocalDate.of(2023, 3, 1), LocalDate.of(2023, 3, 31));

        assertEquals(customerId, dto.getCustomerId());
        verify(pointsService, times(1)).calculatePoints(BigDecimal.valueOf(200.0));
        verify(orderRepository, times(1)).findByCustomerIdAndOrderDateBetween(eq(customerId), any(LocalDate.class), any(LocalDate.class));
    }

    @Test
    void getRewards_withNullStart_usesDefaultStartDate() {
        LocalDate providedEnd = LocalDate.of(2023, 3, 31);
        when(orderRepository.findByOrderDateBetween(any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(List.of());

        rewardsService.getRewards(null, providedEnd);

        LocalDate expectedStart = LocalDate.of(2023, 1, 1); // defaultStart based on fixed clock 2025-12-15
        verify(orderRepository, times(1)).findByOrderDateBetween(eq(expectedStart), eq(providedEnd));
    }

    @Test
    void getRewards_withNullEnd_usesDefaultEndDate() {
        LocalDate providedStart = LocalDate.of(2023, 1, 1);
        when(orderRepository.findByOrderDateBetween(any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(List.of());

        rewardsService.getRewards(providedStart, null);

        LocalDate expectedEnd = LocalDate.of(2025, 12, 31); // defaultEnd based on fixed clock 2025-12-15
        verify(orderRepository, times(1)).findByOrderDateBetween(eq(providedStart), eq(expectedEnd));
    }
    @Test
    void getRewards_withDatesNull_usesDefaultDates() {
        when(orderRepository.findByOrderDateBetween(any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(List.of());

        rewardsService.getRewards(null, null);

        LocalDate expectedStart = LocalDate.of(2025, 10, 1); // defaultEnd based on fixed clock 2025-12-15
        LocalDate expectedEnd = LocalDate.of(2025, 12, 31); // defaultEnd based on fixed clock 2025-12-15
        verify(orderRepository, times(1)).findByOrderDateBetween(eq(expectedStart), eq(expectedEnd));
    }
}