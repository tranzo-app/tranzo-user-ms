package com.tranzo.tranzo_user_ms.splitwise.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.util.UUID;

/**
 * DTO for creating a new settlement.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateSettlementRequest {

    @NotNull(message = "Group ID is required")
    private Long groupId;

    @NotNull(message = "Paid by user ID is required")
    private UUID paidById;

    @NotNull(message = "Paid to user ID is required")
    private UUID paidToId;

    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.01", message = "Amount must be greater than 0")
    private BigDecimal amount;

    @Size(max = 50, message = "Payment method must not exceed 50 characters")
    private String paymentMethod;

    @Size(max = 100, message = "Transaction ID must not exceed 100 characters")
    private String transactionId;

    @Size(max = 500, message = "Notes must not exceed 500 characters")
    private String notes;
}
