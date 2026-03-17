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
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Entity representing a settlement between users.
 * Settlements are used to clear outstanding balances.
 */
@Entity
@Table(name = "splitwise_settlements")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Settlement {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_id", nullable = false)
    private SplitwiseGroup group;

    @Column(name = "paid_by", nullable = false)
    private UUID paidBy;

    @Column(name = "paid_to", nullable = false)
    private UUID paidTo;

    @NotNull(message = "Settlement amount is required")
    @DecimalMin(value = "0.01", message = "Settlement amount must be greater than 0")
    @Column(name = "amount", nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;

    @Column(name = "payment_method", length = 50)
    private String paymentMethod;

    @Column(name = "transaction_id", length = 100)
    private UUID transactionId;

    @Column(name = "notes", length = 500)
    private String notes;

    @Column(name = "status", length = 20)
    @Builder.Default
    private String status = "PENDING";

    @CreationTimestamp
    @Column(name = "settled_at", nullable = false)
    private LocalDateTime settledAt;

    @OneToMany(mappedBy = "settlement", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<SettlementExpense> settledExpenses = new ArrayList<>();

    /**
     * Adds an expense to this settlement.
     */
    public void addSettledExpense(SettlementExpense settlementExpense) {
        settledExpenses.add(settlementExpense);
        settlementExpense.setSettlement(this);
    }

    /**
     * Removes an expense from this settlement.
     */
    public void removeSettledExpense(SettlementExpense settlementExpense) {
        settledExpenses.remove(settlementExpense);
        settlementExpense.setSettlement(null);
    }

    /**
     * Gets the total amount of settled expenses.
     */
    public BigDecimal getTotalSettledAmount() {
        return settledExpenses.stream()
                .map(SettlementExpense::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * Checks if this settlement is fully allocated to expenses.
     */
    public boolean isFullyAllocated() {
        return getTotalSettledAmount().compareTo(amount) >= 0;
    }

    /**
     * Gets the remaining unallocated amount.
     */
    public BigDecimal getRemainingAmount() {
        return amount.subtract(getTotalSettledAmount());
    }

    /**
     * Validates that this settlement is valid.
     */
    public boolean isValid() {
        return group != null && 
               paidBy != null && 
               paidTo != null && 
               !paidBy.equals(paidTo) && // Users should be different
               amount != null && 
               amount.compareTo(BigDecimal.ZERO) > 0;
    }

    /**
     * Gets a human-readable description of this settlement.
     */
    public String getDescription() {
        StringBuilder description = new StringBuilder();
        description.append("User ").append(paidBy)
                   .append(" paid User ").append(paidTo)
                   .append(": $").append(amount);
        
        if (paymentMethod != null) {
            description.append(" via ").append(paymentMethod);
        }
        
        return description.toString();
    }

    /**
     * Checks if this settlement involves a specific user.
     */
    public boolean involvesUser(UUID userId) {
        return paidBy.equals(userId) || paidTo.equals(userId);
    }

    /**
     * Gets the other user in this settlement (not the specified user).
     */
    public UUID getOtherUser(UUID userId) {
        if (paidBy.equals(userId)) {
            return paidTo;
        } else if (paidTo.equals(userId)) {
            return paidBy;
        }
        return null;
    }

    /**
     * Gets the amount for a specific user.
     * Positive if they should receive, negative if they paid.
     */
    public BigDecimal getAmountForUser(UUID userId) {
        if (paidBy.equals(userId)) {
            return amount.negate(); // They paid, so negative
        } else if (paidTo.equals(userId)) {
            return amount; // They received, so positive
        }
        return BigDecimal.ZERO;
    }

    /**
     * Creates a reverse settlement (paidTo pays paidBy).
     */
    public Settlement createReverse() {
        return Settlement.builder()
                .group(this.group)
                .paidBy(this.paidTo)
                .paidTo(this.paidBy)
                .amount(this.amount)
                .build();
    }

    /**
     * Gets the number of expenses settled by this settlement.
     */
    public int getSettledExpenseCount() {
        return settledExpenses.size();
    }

    /**
     * Checks if this settlement settles a specific expense.
     */
    public boolean settlesExpense(Long expenseId) {
        return settledExpenses.stream()
                .anyMatch(se -> se.getExpense().getId().equals(expenseId));
    }

    /**
     * Gets the amount settled for a specific expense.
     */
    public BigDecimal getSettledAmountForExpense(Long expenseId) {
        return settledExpenses.stream()
                .filter(se -> se.getExpense().getId().equals(expenseId))
                .map(SettlementExpense::getAmount)
                .findFirst()
                .orElse(BigDecimal.ZERO);
    }
}
