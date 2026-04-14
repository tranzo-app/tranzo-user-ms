package com.tranzo.tranzo_user_ms.splitwise.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Entity representing an expense in the Splitwise system.
 * Expenses can be split among group members in various ways.
 */
@Entity
@Table(name = "splitwise_expenses")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Expense {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @NotBlank(message = "Expense name is required")
    @Size(max = 200, message = "Expense name must not exceed 200 characters")
    @Column(name = "name", nullable = false, length = 200)
    private String name;

    @Column(name = "description", length = 1000)
    private String description;

    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.01", message = "Amount must be greater than 0")
    @Column(name = "amount", nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;

    @Column(name = "paid_by", nullable = false)
    private UUID paidBy;

    @Column(name = "group_id", nullable = false)
    private UUID groupId;

    @Enumerated(EnumType.STRING)
    @Column(name = "split_type", nullable = false)
    private SplitType splitType;

    @Column(name = "expense_date")
    private LocalDateTime expenseDate;

    @Column(name = "category", length = 50)
    private String category;

    @Column(name = "receipt_url", length = 500)
    private String receiptUrl;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "expense", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<ExpenseSplit> splits = new ArrayList<>();

    @OneToMany(mappedBy = "expense", cascade = CascadeType.ALL)
    @Builder.Default
    private List<SettlementExpense> settlementExpenses = new ArrayList<>();

    /**
     * Enum defining how expenses can be split among users.
     */
    public enum SplitType {
        EQUAL("Equal Split"),
        UNEQUAL("Unequal Split"),
        PERCENTAGE("Percentage Split");

        private final String displayName;

        SplitType(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    public UUID getPaidBy() {
        return paidBy;
    }

    public UUID getGroupId() {
        return groupId;
    }

    /**
     * Adds a split to this expense.
     */
    public void addSplit(ExpenseSplit split) {
        splits.add(split);
        split.setExpense(this);
    }

    /**
     * Removes a split from this expense.
     */
    public void removeSplit(ExpenseSplit split) {
        splits.remove(split);
        split.setExpense(null);
    }

    /**
     * Gets the total amount split among all users.
     */
    public BigDecimal getTotalSplitAmount() {
        return splits.stream()
                .map(ExpenseSplit::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * Checks if the expense splits are valid (sum equals total amount).
     */
    public boolean hasValidSplits() {
        if (splits.isEmpty()) {
            return false;
        }

        BigDecimal totalSplit = getTotalSplitAmount();
        return totalSplit.compareTo(amount) == 0;
    }

    /**
     * Gets the split amount for a specific user.
     */
    public BigDecimal getSplitAmountForUser(UUID userId) {
        return splits.stream()
                .filter(split -> split.getUserId().equals(userId))
                .map(ExpenseSplit::getAmount)
                .findFirst()
                .orElse(BigDecimal.ZERO);
    }

    /**
     * Checks if a user is involved in this expense.
     */
    public boolean isUserInvolved(UUID userId) {
        return paidBy.equals(userId) || 
               splits.stream().anyMatch(split -> split.getUserId().equals(userId));
    }

    /**
     * Gets all users involved in this expense (payer + split users).
     */
    public List<UUID> getInvolvedUsers() {
        List<UUID> involvedUsers = new ArrayList<>();
        involvedUsers.add(paidBy);
        
        splits.stream()
                .map(ExpenseSplit::getUserId)
                .filter(userId -> !involvedUsers.contains(userId))
                .forEach(involvedUsers::add);
        
        return involvedUsers;
    }

    /**
     * Gets the number of users involved in this expense.
     */
    public int getInvolvedUserCount() {
        return getInvolvedUsers().size();
    }

    /**
     * Checks if this expense is fully settled.
     */
    public boolean isFullySettled() {
        return settlementExpenses.stream()
                .map(SettlementExpense::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .compareTo(amount) >= 0;
    }

    /**
     * Gets the remaining unsettled amount.
     */
    public BigDecimal getRemainingAmount() {
        BigDecimal settledAmount = settlementExpenses.stream()
                .map(SettlementExpense::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        return amount.subtract(settledAmount);
    }
}
