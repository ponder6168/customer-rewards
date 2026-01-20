package com.retail.customer.rewards.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PointsServiceTest {

    private final PointsService pointsService = new PointsService();

    @Test
    @DisplayName("Null Amount yields 0 points")
    void testNullAmount() {
        assertEquals(0, pointsService.calculatePoints(null));
    }

    @Test
    @DisplayName("Negative Amount yields 0 points")
    void testNegativeAmount() {
        assertEquals(0, pointsService.calculatePoints(new BigDecimal("-1")));
    }

    @Test
    @DisplayName("Amounts below or equal to $50 yield 0 points")
    void testBelowOrEqual50() {
        assertEquals(0, pointsService.calculatePoints(new BigDecimal("49.99")));
        assertEquals(0, pointsService.calculatePoints(new BigDecimal("50.00")));
    }

    @Test
    @DisplayName("$51 yields 1 point (1 dollar over 50)")
    void testFiftyOne() {
        assertEquals(1, pointsService.calculatePoints(new BigDecimal("51.00")));
    }

    @Test
    @DisplayName("$99.99 floors to 99 -> 49 points")
    void testNinetyNinePointNinetyNine() {
        assertEquals(49, pointsService.calculatePoints(new BigDecimal("99.99")));
    }

    @Test
    @DisplayName("$100 yields 50 points (50 dollars between 50 and 100)")
    void testOneHundred() {
        assertEquals(50, pointsService.calculatePoints(new BigDecimal("100.00")));
    }

    @Test
    @DisplayName("$101 yields 52 points (50 for 50..100 + 2 for the dollar over 100)")
    void testOneHundredOne() {
        assertEquals(52, pointsService.calculatePoints(new BigDecimal("101.00")));
    }

    @Test
    @DisplayName("$120 yields 90 points (50 + 2*(20))")
    void testOneTwenty() {
        assertEquals(90, pointsService.calculatePoints(new BigDecimal("120.00")));
    }

    @Test
    @DisplayName("Fractional values are floored before calculation")
    void testFractionalFloored() {
        assertEquals(90, pointsService.calculatePoints(new BigDecimal("120.50"))); // floors to 120
        assertEquals(49, pointsService.calculatePoints(new BigDecimal("99.99")));   // floors to 99
    }
}