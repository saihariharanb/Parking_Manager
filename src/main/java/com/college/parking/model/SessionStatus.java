package com.college.parking.model;

/**
 * Enumeration representing the status of a vehicle's parking stay.
 */
public enum SessionStatus {
    ACTIVE("Active"),
    COMPLETED("Completed");

    private final String displayName;

    SessionStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public static SessionStatus fromString(String text) {
        for (SessionStatus s : SessionStatus.values()) {
            if (s.name().equalsIgnoreCase(text) || s.displayName.equalsIgnoreCase(text)) {
                return s;
            }
        }
        return ACTIVE;
    }

    @Override
    public String toString() {
        return displayName;
    }
}
