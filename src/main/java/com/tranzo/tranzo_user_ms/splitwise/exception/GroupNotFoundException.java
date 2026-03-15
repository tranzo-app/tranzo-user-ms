package com.tranzo.tranzo_user_ms.splitwise.exception;

import java.util.UUID;

/**
 * Exception thrown when a group is not found.
 */
public class GroupNotFoundException extends SplitwiseException {

    public GroupNotFoundException(Long groupId) {
        super("Group not found with ID: " + groupId);
    }

    public GroupNotFoundException(UUID tripId) {
        super("Group not found for trip: " + tripId);
    }

    public GroupNotFoundException(String message) {
        super(message);
    }
}
