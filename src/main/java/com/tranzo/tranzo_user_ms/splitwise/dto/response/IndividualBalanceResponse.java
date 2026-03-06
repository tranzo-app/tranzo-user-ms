package com.tranzo.tranzo_user_ms.splitwise.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * DTO for individual balance between two users.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IndividualBalanceResponse {

    private UserResponse otherUser;
    private BigDecimal amount;
    private String type; // "OWED" or "OWING"
}
