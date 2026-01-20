package com.retail.customer.rewards.validation;

import com.retail.customer.rewards.dto.RewardsRequest;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.time.LocalDate;

public class DateRangeValidator implements ConstraintValidator<ValidDateRange, RewardsRequest> {
    @Override
    public boolean isValid(RewardsRequest rewardsRequest, ConstraintValidatorContext ctx) {
        if (rewardsRequest == null) return true;
        LocalDate startDate = rewardsRequest.getStartDate();
        LocalDate endDate = rewardsRequest.getEndDate();
        return startDate == null || endDate == null || !startDate.isAfter(endDate);
    }
}
