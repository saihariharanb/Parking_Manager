package com.college.parking.model;

/**
 * Enumeration representing allowed vehicle categories in the college campus.
 */
public enum VehicleType {
    BIKE("Bike"),
    SCOOTER("Scooter"),
    CAR("Car");

    private final String displayName;

    VehicleType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public static VehicleType fromString(String text) {
        for (VehicleType v : VehicleType.values()) {
            if (v.name().equalsIgnoreCase(text) || v.displayName.equalsIgnoreCase(text)) {
                return v;
            }
        }
        return CAR;
    }

    @Override
    public String toString() {
        return displayName;
    }
}
