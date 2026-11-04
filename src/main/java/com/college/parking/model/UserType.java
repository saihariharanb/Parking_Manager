package com.college.parking.model;

/**
 * Enumeration representing user classification in the college.
 */
public enum UserType {
    STUDENT("Student"),
    STAFF("Staff");

    private final String displayName;

    UserType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public static UserType fromString(String text) {
        for (UserType t : UserType.values()) {
            if (t.name().equalsIgnoreCase(text) || t.displayName.equalsIgnoreCase(text)) {
                return t;
            }
        }
        return STUDENT;
    }

    @Override
    public String toString() {
        return displayName;
    }
}
