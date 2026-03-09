package com.tranzo.tranzo_user_ms.splitwise.exception;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Splitwise Exception Unit Tests")
class SplitwiseExceptionsTest {

    @Test
    @DisplayName("SplitwiseException with message")
    void splitwiseException_Message() {
        SplitwiseException e = new SplitwiseException("Something failed");
        assertEquals("Something failed", e.getMessage());
        assertNull(e.getCause());
    }

    @Test
    @DisplayName("SplitwiseException with message and cause")
    void splitwiseException_MessageAndCause() {
        RuntimeException cause = new RuntimeException("root");
        SplitwiseException e = new SplitwiseException("Wrapped", cause);
        assertEquals("Wrapped", e.getMessage());
        assertSame(cause, e.getCause());
    }

    @Test
    @DisplayName("GroupNotFoundException")
    void groupNotFoundException() {
        GroupNotFoundException e = new GroupNotFoundException(999L);
        assertNotNull(e.getMessage());
        assertTrue(e.getMessage().contains("999"));
    }

    @Test
    @DisplayName("ExpenseNotFoundException")
    void expenseNotFoundException() {
        ExpenseNotFoundException e = new ExpenseNotFoundException(100L);
        assertNotNull(e.getMessage());
        assertTrue(e.getMessage().contains("100"));
    }

    @Test
    @DisplayName("UserNotMemberException")
    void userNotMemberException() {
        UUID userId = UUID.randomUUID();
        Long groupId = 1L;
        UserNotMemberException e = new UserNotMemberException(userId, groupId);
        assertNotNull(e.getMessage());
        assertTrue(e.getMessage().contains(userId.toString()));
        assertTrue(e.getMessage().contains("1"));
    }

    @Test
    @DisplayName("InvalidSplitException")
    void invalidSplitException() {
        InvalidSplitException e = new InvalidSplitException("Splits must sum to total");
        assertEquals("Splits must sum to total", e.getMessage());
    }

    @Test
    @DisplayName("InsufficientBalanceException with available and requested")
    void insufficientBalanceException_AvailableRequested() {
        InsufficientBalanceException e = new InsufficientBalanceException(new BigDecimal("50"), new BigDecimal("100"));
        assertNotNull(e.getMessage());
        assertTrue(e.getMessage().contains("50"));
        assertTrue(e.getMessage().contains("100"));
    }

    @Test
    @DisplayName("InsufficientBalanceException with message")
    void insufficientBalanceException_Message() {
        InsufficientBalanceException e = new InsufficientBalanceException("Not enough balance");
        assertEquals("Not enough balance", e.getMessage());
    }

    @Test
    @DisplayName("SettlementNotFoundException")
    void settlementNotFoundException() {
        SettlementNotFoundException e = new SettlementNotFoundException("Settlement not found: 10");
        assertEquals("Settlement not found: 10", e.getMessage());
    }
}
