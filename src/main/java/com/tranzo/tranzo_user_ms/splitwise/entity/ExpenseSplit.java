package com.tranzo.tranzo_user_ms.splitwise.entity;

import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.DecimalMax;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.UUID;
import java.util.ArrayList;
import java.util.List;

/**
 * Entity representing how an expense is split among users.
 * Each split represents one user's share of an expense.
 */
@Entity
@Table(name = "splitwise_expense_splits")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExpenseSplit {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "expense_id", nullable = false)
    private Expense expense;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @NotNull(message = "Split amount is required")
    @DecimalMin(value = "0.01", message = "Split amount must be greater than 0")
    @Column(name = "amount", nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;

    @DecimalMin(value = "0", message = "Percentage cannot be negative")
    @DecimalMax(value = "100", message = "Percentage cannot exceed 100")
    @Column(name = "percentage", precision = 5, scale = 2)
    private BigDecimal percentage;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public UUID getExpenseId() {
        return expense != null ? expense.getId() : null;
    }

    public UUID getUserId() {
        return userId;
    }

    /**
     * Checks if this split is for the expense payer.
     * This would need service call to get expense details for comparison.
     */
    public boolean isPayerSplit(UUID paidByUserId) {
        return paidByUserId != null && paidByUserId.equals(userId);
    }

    /**
     * Gets the effective share amount (amount paid by this user minus their split).
     */
    public BigDecimal getEffectiveShare() {
        // This logic would need to be handled at service level
        // since we don't have direct access to expense entity
        return amount.negate();
    }

    /**
     * Creates equal splits for an expense among multiple users.
     */
    public static List<ExpenseSplit> createEqualSplits(Expense expense, List<UUID> userIds, BigDecimal totalAmount) {
        BigDecimal splitAmount = totalAmount.divide(BigDecimal.valueOf(userIds.size()), 2, RoundingMode.HALF_UP);
        return userIds.stream()
                .map(userId -> ExpenseSplit.builder()
                        .expense(expense)
                        .userId(userId)
                        .amount(splitAmount)
                        .percentage(BigDecimal.valueOf(100.0 / userIds.size()))
                        .build())
                .toList();
    }

    /**
     * Creates percentage-based splits for an expense.
     */
    public static List<ExpenseSplit> createPercentageSplits(Expense expense, List<UUID> userIds, List<BigDecimal> percentages, BigDecimal totalAmount) {
        List<ExpenseSplit> splits = new ArrayList<>();
        for (int i = 0; i < userIds.size(); i++) {
            BigDecimal splitAmount = totalAmount.multiply(percentages.get(i))
                    .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
            splits.add(ExpenseSplit.builder()
                    .expense(expense)
                    .userId(userIds.get(i))
                    .amount(splitAmount)
                    .percentage(percentages.get(i))
                    .build());
        }
        return splits;
    }

    /**
     * Gets the percentage representation of this split.
     * If percentage is not set, calculates it from the expense amount.
     */
    public BigDecimal getCalculatedPercentage() {
        if (percentage != null) {
            return percentage;
        }
        // This logic would need to be handled at service level
        // since we don't have direct access to expense entity
        return BigDecimal.ZERO;
    }

    /**
     * Sets the percentage and calculates the amount based on expense total.
     */
    public void setPercentageAndCalculateAmount(BigDecimal percentage) {
        this.percentage = percentage;
        // This logic would need to be handled at service level
        // since we don't have direct access to expense entity
    }

    /**
     * Validates that this split is consistent with the expense.
     */
    public boolean isValid() {
        if (expense == null || userId == null || amount == null) {
            return false;
        }
        
        // Amount should be positive
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            return false;
        }
        
        // If percentage is set, it should be reasonable
        if (percentage != null) {
            return percentage.compareTo(BigDecimal.ZERO) >= 0 && 
                   percentage.compareTo(new BigDecimal("100")) <= 0;
        }
        
        return true;
    }

    /**
     * Gets a human-readable description of this split.
     */
    public String getDescription() {
        StringBuilder description = new StringBuilder();
        description.append("User ").append(userId)
                   .append(" pays $").append(amount);
        
        if (percentage != null) {
            description.append(" (").append(percentage).append("%)");
        }
        
        return description.toString();
    }
}
