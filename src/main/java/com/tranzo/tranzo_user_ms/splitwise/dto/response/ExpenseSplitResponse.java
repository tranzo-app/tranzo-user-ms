package com.tranzo.tranzo_user_ms.splitwise.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DTO for expense split response data.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExpenseSplitResponse {

    private UUID id;
    private UserResponse user;
    private BigDecimal amount;
    private BigDecimal percentage;
    private LocalDateTime createdAt;
}
