package com.retail.customer.rewards.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.LinkedHashMap;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CustomerMonthlyRewardsDto {

    private String customerId;

    /**
     * Map of "YYYY-MM" -> points for that month.
     * Initialized inline so the no-arg constructor yields a usable map.
     */
    @Builder.Default
    private Map<String, Long> monthlyPoints = new LinkedHashMap<>();

    private long totalPoints;

    /**
     * Convenience helper: add points to a month entry.
     */
    public void addPointsForMonth(String monthKey, long points) {
        this.monthlyPoints.merge(monthKey, points, Long::sum);
        this.totalPoints = this.monthlyPoints.values().stream().mapToLong(Long::longValue).sum();
    }
}