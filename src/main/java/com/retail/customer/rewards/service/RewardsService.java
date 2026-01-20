package com.retail.customer.rewards.service;

import com.retail.customer.rewards.dto.CustomerMonthlyRewardsDto;
import com.retail.customer.rewards.entities.Order;
import com.retail.customer.rewards.repository.OrderRepository;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class RewardsService {

    private final OrderRepository orderRepository;
    private final PointsService pointsService;
    private final DateTimeFormatter monthKeyFormatter = DateTimeFormatter.ofPattern("yyyy-MM");
    private final Clock clock;


    public RewardsService(OrderRepository orderRepository, PointsService pointsService, Clock clock) {
        this.clock = clock;
        this.orderRepository = orderRepository;
        this.pointsService = pointsService;
    }

    private LocalDate defaultStartDate(LocalDate endInclusive) {
        LocalDate dateToCountBackFrom;
        dateToCountBackFrom = (endInclusive != null) ? endInclusive : LocalDate.now(clock);
        return dateToCountBackFrom.minusMonths(2).withDayOfMonth(1);
    }

    private LocalDate defaultEndDate() {
        LocalDate now = LocalDate.now(clock);
        return now.withDayOfMonth(now.lengthOfMonth());
    }

    public List<CustomerMonthlyRewardsDto> getRewards(LocalDate startDate, LocalDate endDate) {
        SearchRangeInclusive searchRange = getSearchRange(startDate, endDate);
        List<Order> orders = orderRepository.findByOrderDateBetween(searchRange.modifiedStartDate(), searchRange.modifiedEndDate());
        Map<String, CustomerMonthlyRewardsDto> aggregation = new LinkedHashMap<>();

        for (Order order : orders) {
            String customerId = order.getCustomerId();
            YearMonth ym = YearMonth.from(order.getOrderDate());
            String monthKey = ym.format(monthKeyFormatter);
            int points = pointsService.calculatePoints(order.getAmount());

            CustomerMonthlyRewardsDto dto = aggregation.computeIfAbsent(customerId, id -> new CustomerMonthlyRewardsDto(id, new LinkedHashMap<>(), 0));
            dto.addPointsForMonth(monthKey, points);
        }

        // Return sorted list by customerId for determinism
        return aggregation.values().stream()
                .sorted(Comparator.comparing(CustomerMonthlyRewardsDto::getCustomerId))
                .collect(Collectors.toList());
    }

    public CustomerMonthlyRewardsDto getRewardsForCustomer(String customerId, LocalDate startDate, LocalDate endDate) {
        SearchRangeInclusive searchRange = getSearchRange(startDate, endDate);

        List<Order> orders = orderRepository.findByCustomerIdAndOrderDateBetween(customerId, searchRange.modifiedStartDate, searchRange.modifiedEndDate);

        CustomerMonthlyRewardsDto dto = new CustomerMonthlyRewardsDto();
        dto.setCustomerId(customerId);

        for (Order order : orders) {
            YearMonth ym = YearMonth.from(order.getOrderDate());
            String monthKey = ym.format(monthKeyFormatter);
            int points = pointsService.calculatePoints(order.getAmount());
            dto.addPointsForMonth(monthKey, points);
        }

        return dto;
    }

    private SearchRangeInclusive getSearchRange(LocalDate startDate, LocalDate endDate) {
        LocalDate modifiedStartDate = (startDate != null) ? startDate : defaultStartDate(endDate);
        LocalDate modifiedEndDate = (endDate != null) ? endDate : defaultEndDate();

        return new SearchRangeInclusive(modifiedStartDate, modifiedEndDate);
    }

    private record SearchRangeInclusive(LocalDate modifiedStartDate, LocalDate modifiedEndDate) {
    }

}