package com.tranzo.tranzo_user_ms.splitwise.exception;

import java.util.UUID;

/**
 * Exception thrown when a user is not a member of a group.
 */
public class UserNotMemberException extends SplitwiseException {

    public UserNotMemberException(UUID userId, Long groupId) {
        super(String.format("User %d is not a member of group %d", userId, groupId));
    }

    public UserNotMemberException(String message) {
        super(message);
    }
}
