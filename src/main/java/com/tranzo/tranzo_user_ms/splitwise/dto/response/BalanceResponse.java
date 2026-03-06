package com.tranzo.tranzo_user_ms.splitwise.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * DTO for balance response data.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BalanceResponse {

    private UserResponse user;
    private BigDecimal totalOwed;
    private BigDecimal totalOwing;
    private BigDecimal netBalance;
    private Map<Long, BigDecimal> individualBalances; // userId -> amount
    private List<IndividualBalanceResponse> balanceDetails;
}
