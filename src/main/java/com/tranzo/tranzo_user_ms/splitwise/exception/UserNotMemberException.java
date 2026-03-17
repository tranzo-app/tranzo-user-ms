package com.tranzo.tranzo_user_ms.splitwise.exception;

import java.util.UUID;

/**
 * Exception thrown when a user is not a member of a group.
 */
public class UserNotMemberException extends SplitwiseException {

    public UserNotMemberException(UUID userId, UUID groupId) {
        super(String.format("User %s is not a member of group %s", userId.toString(), groupId));
    }

    public UserNotMemberException(String message) {
        super(message);
    }
}
