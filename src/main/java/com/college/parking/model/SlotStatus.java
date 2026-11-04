package com.college.parking.model;

/**
 * Enumeration representing the operational status of a parking slot.
 */
public enum SlotStatus {
    AVAILABLE("Available"),
    OCCUPIED("Occupied"),
    MAINTENANCE("Maintenance");

    private final String displayName;

    SlotStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public static SlotStatus fromString(String text) {
        for (SlotStatus s : SlotStatus.values()) {
            if (s.name().equalsIgnoreCase(text) || s.displayName.equalsIgnoreCase(text)) {
                return s;
            }
        }
        return AVAILABLE;
    }

    @Override
    public String toString() {
        return displayName;
    }
}
