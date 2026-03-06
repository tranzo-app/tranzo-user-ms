package com.tranzo.tranzo_user_ms.splitwise.exception;

/**
 * Exception thrown when a settlement is not found.
 */
public class SettlementNotFoundException extends SplitwiseException {

    public SettlementNotFoundException(Long settlementId) {
        super("Settlement not found with ID: " + settlementId);
    }

    public SettlementNotFoundException(String message) {
        super(message);
    }
}
