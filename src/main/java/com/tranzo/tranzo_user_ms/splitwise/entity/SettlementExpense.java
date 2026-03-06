package com.tranzo.tranzo_user_ms.splitwise.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Entity representing the relationship between a settlement and an expense.
 * Tracks how much of a settlement is allocated to which expense.
 */
@Entity
@Table(name = "splitwise_settlement_expenses",
       uniqueConstraints = @UniqueConstraint(columnNames = {"settlement_id", "expense_id"}))
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SettlementExpense {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "settlement_id", nullable = false)
    private Settlement settlement;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "expense_id", nullable = false)
    private Expense expense;

    @NotNull(message = "Settlement amount is required")
    @DecimalMin(value = "0.01", message = "Settlement amount must be greater than 0")
    @Column(name = "amount", nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * Gets the percentage of the expense that this settlement covers.
     */
    public BigDecimal getSettlementPercentage() {
        if (expense != null && expense.getAmount().compareTo(BigDecimal.ZERO) > 0) {
            return amount.divide(expense.getAmount(), 4, BigDecimal.ROUND_HALF_UP)
                       .multiply(new BigDecimal("100"))
                       .setScale(2, BigDecimal.ROUND_HALF_UP);
        }
        return BigDecimal.ZERO;
    }

    /**
     * Gets the percentage of the settlement that goes to this expense.
     */
    public BigDecimal getSettlementPortionPercentage() {
        if (settlement != null && settlement.getAmount().compareTo(BigDecimal.ZERO) > 0) {
            return amount.divide(settlement.getAmount(), 4, BigDecimal.ROUND_HALF_UP)
                       .multiply(new BigDecimal("100"))
                       .setScale(2, BigDecimal.ROUND_HALF_UP);
        }
        return BigDecimal.ZERO;
    }

    /**
     * Validates that this settlement expense is valid.
     */
    public boolean isValid() {
        return settlement != null && 
               expense != null && 
               amount != null && 
               amount.compareTo(BigDecimal.ZERO) > 0 &&
               amount.compareTo(expense.getAmount()) <= 0; // Cannot settle more than expense amount
    }

    /**
     * Gets a human-readable description of this settlement expense.
     */
    public String getDescription() {
        return String.format("Settlement %d covers $%.2f of expense %d ($%.2f total - %.2f%%)", 
                           settlement.getId(), amount, expense.getId(), 
                           expense.getAmount(), getSettlementPercentage());
    }

    /**
     * Checks if this fully settles the expense.
     */
    public boolean fullySettlesExpense() {
        return amount.compareTo(expense.getAmount()) >= 0;
    }

    /**
     * Gets the remaining amount needed to fully settle the expense.
     */
    public BigDecimal getRemainingAmount() {
        return expense.getAmount().subtract(amount);
    }

    /**
     * Updates the settlement amount, ensuring it doesn't exceed the expense amount.
     */
    public void updateAmount(BigDecimal newAmount) {
        if (newAmount != null) {
            // Cap at expense amount
            BigDecimal maxAmount = expense.getAmount();
            this.amount = newAmount.min(maxAmount);
        }
    }

    /**
     * Adds to the current settlement amount.
     */
    public void addAmount(BigDecimal amountToAdd) {
        BigDecimal newAmount = this.amount.add(amountToAdd);
        updateAmount(newAmount);
    }

    /**
     * Checks if this settlement expense involves a specific user.
     */
    public boolean involvesUser(UUID userId) {
        return settlement.involvesUser(userId) && expense.isUserInvolved(userId);
    }

    /**
     * Gets the effective amount for a specific user from this settlement expense.
     */
    public BigDecimal getAmountForUser(UUID userId) {
        if (!involvesUser(userId)) {
            return BigDecimal.ZERO;
        }
        
        // Get the user's share of expense
        BigDecimal userExpenseShare = expense.getSplitAmountForUser(userId);
        
        // Calculate proportion of this settlement that applies to this user
        BigDecimal settlementProportion = amount.divide(expense.getAmount(), 4, RoundingMode.HALF_UP);
        
        return userExpenseShare.multiply(settlementProportion).setScale(2, RoundingMode.HALF_UP);
    }
}
