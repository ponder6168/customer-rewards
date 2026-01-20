package com.retail.customer.rewards.controller;

import com.retail.customer.rewards.dto.CustomerMonthlyRewardsDto;
import com.retail.customer.rewards.dto.RewardsRequest;
import com.retail.customer.rewards.service.RewardsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/rewards")
@Tag(name = "Rewards", description = "Retrieve monthly and total reward points summaries")
@RequiredArgsConstructor
public class RewardsController {

    private final RewardsService rewardsService;

    @Operation(summary = "Get rewards for all customers", description = "Returns a list of customers with monthly breakdown and totals for the date range")
    @GetMapping
    public ResponseEntity<List<CustomerMonthlyRewardsDto>> getRewards(@Valid @ModelAttribute RewardsRequest rewardsRequest) {
        List<CustomerMonthlyRewardsDto> result = rewardsService.getRewards(rewardsRequest.getStartDate(), rewardsRequest.getEndDate());
        return ResponseEntity.ok(result);
    }

    @Operation(summary = "Get rewards for a customer", description = "Returns monthly breakdown and total points for a single customer")
    @GetMapping("/{customerId}")
    public ResponseEntity<CustomerMonthlyRewardsDto> getRewardsForCustomer(
            @PathVariable String customerId, @Valid @ModelAttribute RewardsRequest rewardsRequest
    ) {

        CustomerMonthlyRewardsDto dto = rewardsService.getRewardsForCustomer(customerId, rewardsRequest.getStartDate(), rewardsRequest.getEndDate());
        return ResponseEntity.ok(dto);
    }
}