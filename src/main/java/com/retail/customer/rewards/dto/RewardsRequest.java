package com.retail.customer.rewards.dto;

import com.retail.customer.rewards.validation.ValidDateRange;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ValidDateRange(message = "Start date must be before end date")
public class RewardsRequest {

    private LocalDate startDate;

    private LocalDate endDate;

}
