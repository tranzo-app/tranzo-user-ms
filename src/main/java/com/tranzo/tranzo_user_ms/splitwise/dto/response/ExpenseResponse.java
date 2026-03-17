package com.tranzo.tranzo_user_ms.splitwise.dto.response;

import com.tranzo.tranzo_user_ms.splitwise.entity.Expense;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * DTO for expense response data.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExpenseResponse {

    private UUID id;
    private String name;
    private String description;
    private BigDecimal amount;
    private UserResponse paidBy;
    private GroupResponse group;
    private Expense.SplitType splitType;
    private String category;
    private LocalDateTime expenseDate;
    private String receiptUrl;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<ExpenseSplitResponse> splits;
    private Boolean isSettled;
    private BigDecimal remainingAmount;
}
