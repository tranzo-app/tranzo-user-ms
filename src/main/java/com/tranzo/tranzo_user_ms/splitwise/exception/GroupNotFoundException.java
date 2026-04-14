package com.tranzo.tranzo_user_ms.splitwise.exception;

import com.tranzo.tranzo_user_ms.splitwise.enums.SplitwiseErrorCode;

import java.util.UUID;

/**
 * Exception thrown when a group is not found.
 */
public class GroupNotFoundException extends SplitwiseException {

    public GroupNotFoundException(UUID groupId) {
        super(SplitwiseErrorCode.GROUP_NOT_FOUND, 404, "Group not found with ID: " + groupId);
    }

    public GroupNotFoundException(String message) {
        super(SplitwiseErrorCode.GROUP_NOT_FOUND, 404, message);
    }
}
