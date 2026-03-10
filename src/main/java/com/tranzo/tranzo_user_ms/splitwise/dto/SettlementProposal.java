package com.tranzo.tranzo_user_ms.splitwise.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;
import java.util.UUID;

/**
 * Data Transfer Object representing a settlement proposal between two users.
 * This represents a single transaction that should occur to settle group expenses.
 */
@Data
@NoArgsConstructor
public class SettlementProposal {
    
    /**
     * ID of the user who should pay (debtor).
     */
    private UUID from;
    
    /**
     * ID of the user who should receive payment (creditor).
     */
    private UUID to;
    
    /**
     * Amount to be settled, always positive.
     */
    private BigDecimal amount;
    
    /**
     * Constructor with automatic amount scaling.
     */
    public SettlementProposal(UUID from, UUID to, BigDecimal amount) {
        this.from = from;
        this.to = to;
        this.amount = amount != null ? 
            amount.setScale(2, RoundingMode.HALF_UP) : 
            BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
    }
    
    /**
     * Validates that this settlement proposal is valid.
     * 
     * @return true if the proposal is valid
     */
    public boolean isValid() {
        return from != null && 
               to != null && 
               !Objects.equals(from, to) && 
               amount != null && 
               amount.compareTo(BigDecimal.ZERO) > 0;
    }
    
    /**
     * Returns a human-readable description of the settlement.
     */
    public String getDescription() {
        return String.format("User %s pays User %s: $%.2f", from, to, amount);
    }
    
    @Override
    public String toString() {
        return getDescription();
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SettlementProposal that = (SettlementProposal) o;
        return Objects.equals(from, that.from) && 
               Objects.equals(to, that.to) && 
               Objects.equals(amount, that.amount);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(from, to, amount);
    }
}
