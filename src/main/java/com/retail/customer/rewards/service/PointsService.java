package com.retail.customer.rewards.service;

import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Calculates reward points for a single order amount.
 *
 * Rules:
 * - 0 points for amount <= 50
 * - 1 point per dollar for dollars between 50 and 100 (100 inclusive)
 * - 2 points per dollar for dollars over 100
 *
 * Points are calculated per whole dollar (floor).
 */
@Service
public class PointsService {

    /**
     * Calculate points for the given monetary amount.
     *
     * @param amount money amount (must be non-null; can be fractional)
     * @return computed points (>= 0)
     */
    public int calculatePoints(BigDecimal amount) {
        if (amount == null) {
            return 0;
        }
        int dollars = amount.setScale(0, RoundingMode.FLOOR).intValue();
        if(dollars < 0) {
            return 0;
        } else if (dollars <= 50) {
            return 0;
        } else if (dollars <= 100) {
            return dollars - 50;

        } else {
            return 50 + (dollars - 100) * 2;
        }
    }
}