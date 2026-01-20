package com.retail.customer.rewards.dto;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class CustomerMonthlyRewardsDtoTest {

    @Test
    void defaultConstructorInitializesMap() {
        var dto = new CustomerMonthlyRewardsDto();
        assertNotNull(dto.getMonthlyPoints());
        assertTrue(dto.getMonthlyPoints().isEmpty());
        assertEquals(0L, dto.getTotalPoints());
    }

    @Test
    void addPointsCreatesAndSums() {
        var dto = new CustomerMonthlyRewardsDto();
        dto.addPointsForMonth("2025-09", 50L);
        assertEquals(50L, dto.getMonthlyPoints().get("2025-09").longValue());
        assertEquals(50L, dto.getTotalPoints());

        dto.addPointsForMonth("2025-09", 25L);
        assertEquals(75L, dto.getMonthlyPoints().get("2025-09").longValue());
        assertEquals(75L, dto.getTotalPoints());
    }

    @Test
    void multipleMonthsTotalCalculated() {
        var dto = CustomerMonthlyRewardsDto.builder().build();
        dto.addPointsForMonth("2025-08", 10L);
        dto.addPointsForMonth("2025-09", 20L);
        assertEquals(10L, dto.getMonthlyPoints().get("2025-08").longValue());
        assertEquals(20L, dto.getMonthlyPoints().get("2025-09").longValue());
        assertEquals(30L, dto.getTotalPoints());
    }
}
