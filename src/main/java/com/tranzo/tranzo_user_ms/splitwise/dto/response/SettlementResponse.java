package com.tranzo.tranzo_user_ms.splitwise.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO for settlement response data.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SettlementResponse {

    private Long id;
    private GroupResponse group;
    private UserResponse paidBy;
    private UserResponse paidTo;
    private BigDecimal amount;
    private String paymentMethod;
    private String transactionId;
    private String notes;
    private LocalDateTime settledAt;
    private List<ExpenseResponse> settledExpenses;
    private Boolean isFullyAllocated;
    private BigDecimal remainingAmount;
    private String status;
}
