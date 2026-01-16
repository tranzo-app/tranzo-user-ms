package com.tranzo.tranzo_user_ms.trip.enums;

public enum TripStatus {
    DRAFT,
    PUBLISHED,
    ONGOING,
    COMPLETED,
    CANCELLED;

    public boolean canManuallyTransitionTo(TripStatus next) {
        return switch (this) {
            case DRAFT -> next == PUBLISHED || next == CANCELLED;
            case PUBLISHED -> next == CANCELLED;
            default -> false;
        };
    }

    public boolean canAutomaticallyTransitionTo(TripStatus next) {
        return switch (this) {
            case PUBLISHED -> next == ONGOING;
            case ONGOING -> next == COMPLETED;
            default -> false;
        };
    }
}
