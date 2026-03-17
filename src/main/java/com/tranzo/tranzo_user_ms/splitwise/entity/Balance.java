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
import java.util.UUID;

/**
 * Entity representing the balance between two users in a group.
 * Tracks how much one user owes another.
 */
@Entity
@Table(name = "splitwise_balances",
       uniqueConstraints = @UniqueConstraint(columnNames = {"group_id", "owed_by", "owed_to"}))
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Balance {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_id", nullable = false)
    private SplitwiseGroup group;

    @Column(name = "owed_by", nullable = false)
    private UUID owedBy;

    @Column(name = "owed_to", nullable = false)
    private UUID owedTo;

    @NotNull(message = "Balance amount is required")
    @DecimalMin(value = "0.01", message = "Balance amount must be greater than 0")
    @Column(name = "amount", nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;

    @CreationTimestamp
    @Column(name = "last_updated", nullable = false)
    private LocalDateTime lastUpdated;

    /**
     * Updates the balance amount.
     * If the new amount is zero or negative, this balance should be deleted.
     */
    public void updateAmount(BigDecimal newAmount) {
        if (newAmount.compareTo(BigDecimal.ZERO) <= 0) {
            this.amount = BigDecimal.ZERO;
        } else {
            this.amount = newAmount;
        }
        this.lastUpdated = LocalDateTime.now();
    }

    /**
     * Adds to the current balance amount.
     */
    public void addAmount(BigDecimal amountToAdd) {
        BigDecimal newAmount = this.amount.add(amountToAdd);
        updateAmount(newAmount);
    }

    /**
     * Subtracts from the current balance amount.
     */
    public void subtractAmount(BigDecimal amountToSubtract) {
        BigDecimal newAmount = this.amount.subtract(amountToSubtract);
        updateAmount(newAmount);
    }

    /**
     * Checks if this balance should be deleted (amount is zero or negative).
     */
    public boolean shouldDelete() {
        return amount.compareTo(BigDecimal.ZERO) <= 0;
    }

    /**
     * Gets a human-readable description of this balance.
     */
    public String getDescription() {
        return String.format("User %s owes User %s: $%.2f in group %d", 
                           owedBy, owedTo, amount, group.getId());
    }

    /**
     * Validates that this balance is valid.
     */
    public boolean isValid() {
        return group != null && 
               owedBy != null && 
               owedTo != null && 
               !owedBy.equals(owedTo) && // Users should be different
               amount != null && 
               amount.compareTo(BigDecimal.ZERO) > 0;
    }

    /**
     * Creates a reverse balance (owedTo owes owedBy).
     */
    public Balance createReverse() {
        return Balance.builder()
                .group(this.group)
                .owedBy(this.owedTo)
                .owedTo(this.owedBy)
                .amount(this.amount)
                .build();
    }

    /**
     * Merges this balance with another balance of the same direction.
     */
    public void mergeWith(Balance other) {
        if (canMergeWith(other)) {
            this.addAmount(other.getAmount());
        }
    }

    /**
     * Checks if this balance can be merged with another.
     */
    public boolean canMergeWith(Balance other) {
        return other != null &&
               this.group.getId().equals(other.getGroup().getId()) &&
               this.owedBy.equals(other.getOwedBy()) &&
               this.owedTo.equals(other.getOwedTo());
    }

    /**
     * Calculates the net balance when considering a reverse balance.
     * Positive means this direction owes more, negative means reverse owes more.
     */
    public BigDecimal calculateNetWith(Balance reverseBalance) {
        if (reverseBalance == null) {
            return this.amount;
        }
        
        if (isReverseOf(reverseBalance)) {
            return this.amount.subtract(reverseBalance.getAmount());
        }
        
        return this.amount; // Not reverse, return original amount
    }

    /**
     * Checks if another balance is the reverse of this one.
     */
    public boolean isReverseOf(Balance other) {
        return other != null &&
               this.group.getId().equals(other.getGroup().getId()) &&
               this.owedBy.equals(other.getOwedTo()) &&
               this.owedTo.equals(other.getOwedBy());
    }
}
