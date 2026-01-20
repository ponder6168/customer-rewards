package com.retail.customer.rewards.controller;

import com.retail.customer.rewards.dto.CustomerMonthlyRewardsDto;
import com.retail.customer.rewards.service.RewardsService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.time.*;
import java.util.List;
import java.util.Map;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.text.IsEmptyString.emptyString;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(RewardsController.class)
class RewardsControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private RewardsService rewardsService;

    @Test
    void getRewardsReturnsDataWhenAvailable() throws Exception {
        LocalDate startDate = LocalDate.of(2023, 1, 1);
        LocalDate endDate = LocalDate.of(2023, 1, 31);
        String monthKey = YearMonth.from(startDate).toString();
        String customerId = "123";

        CustomerMonthlyRewardsDto customerMonthlyRewardsDto = CustomerMonthlyRewardsDto.builder()
                .customerId(customerId)
                .monthlyPoints(Map.of(monthKey, 10L))
                .totalPoints(10L)
                .build();

        when(rewardsService.getRewards(startDate, endDate)).thenReturn(List.of(customerMonthlyRewardsDto));

        mockMvc.perform(get("/rewards")
                        .param("startDate", startDate.toString())
                        .param("endDate", endDate.toString()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].customerId").value(customerId))
                .andExpect(jsonPath("$[0].monthlyPoints['"+ monthKey +"']").value(10))
                .andExpect(jsonPath("$[0].totalPoints").value(10));
    }

    @Test
    void getRewardsReturnsEmptyListWhenNoDataAvailable() throws Exception {
        LocalDate startDate = LocalDate.of(2023, 1, 1);
        LocalDate endDate = LocalDate.of(2023, 1, 31);

        when(rewardsService.getRewards(startDate, endDate)).thenReturn(List.of());

        mockMvc.perform(get("/rewards")
                        .param("startDate", startDate.toString())
                        .param("endDate", endDate.toString()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json("[]"));
    }

    @Test
    void getRewardsForCustomerReturnsEmptyDataWhenNoDataAvailable() throws Exception {
        String customerId = "123";
        LocalDate startDate = LocalDate.of(2023, 1, 1);
        LocalDate endDate = LocalDate.of(2023, 1, 31);

        when(rewardsService.getRewardsForCustomer(customerId, startDate, endDate)).thenReturn(null);

        mockMvc.perform(get("/rewards/{customerId}", customerId)
                        .param("startDate", startDate.toString())
                        .param("endDate", endDate.toString()))
                .andExpect(status().isOk())
                .andExpect(content().string(emptyString()));
    }

    @Test
    void getRewardsThrowsBadRequestWhenStartDateAfterEndDate() throws Exception {
        mockMvc.perform(get("/rewards")
                        .param("startDate", "2023-02-01")
                        .param("endDate", "2023-01-31"))
                .andExpect(status().isBadRequest())
                .andExpect(result -> {
                    Throwable resolved = result.getResolvedException();
                    assertNotNull(resolved, "expected an exception to be resolved");
                    assertInstanceOf(MethodArgumentNotValidException.class, resolved, "expected a MethodArgumentNotValidException");
                    MethodArgumentNotValidException methodArgumentNotValidException = (MethodArgumentNotValidException) resolved;
                    String defaultMessage = methodArgumentNotValidException.getBindingResult().getAllErrors().get(0).getDefaultMessage();
                    assertEquals("Start date must be before end date", defaultMessage);
                });
    }

    @Test
    void getRewardsForCustomerThrowsBadRequestWhenStartDateAfterEndDate() throws Exception {
        String customerId = "123";
        mockMvc.perform(get("/rewards/{customerId}", customerId)
                        .param("startDate", "2023-02-01")
                        .param("endDate", "2023-01-31"))
                .andExpect(status().isBadRequest())
                .andExpect(result -> {
                    Throwable resolved = result.getResolvedException();
                    assertNotNull(resolved, "expected an exception to be resolved");
                    assertInstanceOf(MethodArgumentNotValidException.class, resolved, "expected a MethodArgumentNotValidException");
                    MethodArgumentNotValidException methodArgumentNotValidException = (MethodArgumentNotValidException) resolved;
                    String defaultMessage = methodArgumentNotValidException.getBindingResult().getAllErrors().get(0).getDefaultMessage();
                    assertEquals("Start date must be before end date", defaultMessage);
                });
    }

    // --- Tests that exercise default date behaviour using the fixed Clock provided by FixedClockConfig ---

    @Test
    void getRewardsUsesDefaultDatesWhenParamsOmitted() throws Exception {
        CustomerMonthlyRewardsDto dto = CustomerMonthlyRewardsDto.builder()
                .customerId("def")
                .monthlyPoints(Map.of(
                        "2025-10", 5L,
                        "2025-11", 10L,
                        "2025-12", 15L
                ))
                .totalPoints(30L)
                .build();

        when(rewardsService.getRewards(null, null)).thenReturn(List.of(dto));

        mockMvc.perform(get("/rewards"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].customerId").value("def"))
                .andExpect(jsonPath("$[0].monthlyPoints['2025-10']").value(5))
                .andExpect(jsonPath("$[0].monthlyPoints['2025-11']").value(10))
                .andExpect(jsonPath("$[0].monthlyPoints['2025-12']").value(15))
                .andExpect(jsonPath("$[0].totalPoints").value(30));
    }

    @Test
    void getRewardsForCustomerUsesDefaultDatesWhenParamsOmitted() throws Exception {
        String customerId = "cust-7";

        CustomerMonthlyRewardsDto dto = CustomerMonthlyRewardsDto.builder()
                .customerId(customerId)
                .monthlyPoints(Map.of(
                        "2025-10", 2L,
                        "2025-11", 3L,
                        "2025-12", 4L
                ))
                .totalPoints(9L)
                .build();

        when(rewardsService.getRewardsForCustomer(customerId, null, null)).thenReturn(dto);

        mockMvc.perform(get("/rewards/{customerId}", customerId))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.customerId").value(customerId))
                .andExpect(jsonPath("$.monthlyPoints['2025-10']").value(2))
                .andExpect(jsonPath("$.monthlyPoints['2025-11']").value(3))
                .andExpect(jsonPath("$.monthlyPoints['2025-12']").value(4))
                .andExpect(jsonPath("$.totalPoints").value(9));
    }
}