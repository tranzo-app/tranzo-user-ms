package com.tranzo.tranzo_user_ms.splitwise.exception;

import com.tranzo.tranzo_user_ms.splitwise.enums.SplitwiseErrorCode;

/**
 * Exception thrown when a settlement is not found.
 */
public class SettlementNotFoundException extends SplitwiseException {

    public SettlementNotFoundException(Long settlementId) {
        super(SplitwiseErrorCode.SETTLEMENT_NOT_FOUND, 404, "Settlement not found with ID: " + settlementId);
    }

    public SettlementNotFoundException(String message) {
        super(SplitwiseErrorCode.SETTLEMENT_NOT_FOUND, 404, message);
    }
}
