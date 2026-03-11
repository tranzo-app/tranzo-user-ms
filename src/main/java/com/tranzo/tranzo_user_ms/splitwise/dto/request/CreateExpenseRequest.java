package com.tranzo.tranzo_user_ms.splitwise.dto.request;

import com.tranzo.tranzo_user_ms.splitwise.entity.Expense;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

/**
 * DTO for creating a new expense.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateExpenseRequest {

    @NotBlank(message = "Expense name is required")
    @Size(max = 200, message = "Expense name must not exceed 200 characters")
    private String name;

    @Size(max = 1000, message = "Description must not exceed 1000 characters")
    private String description;

    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.01", message = "Amount must be greater than 0")
    private BigDecimal amount;

    @NotNull(message = "Group ID is required")
    private Long groupId;

    @NotNull(message = "Paid by user ID is required")
    private UUID paidById;

    @NotNull(message = "Split type is required")
    private Expense.SplitType splitType;

    @Size(max = 50, message = "Category must not exceed 50 characters")
    private String category;

    private String expenseDate;

    @Size(max = 500, message = "Receipt URL must not exceed 500 characters")
    private String receiptUrl;

    @NotEmpty(message = "At least one split is required")
    private List<ExpenseSplitRequest> splits;
}
