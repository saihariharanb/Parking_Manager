package com.college.parking.util;

import java.util.regex.Pattern;

/**
 * Validation utility for user inputs across the College Parking Manager application.
 * Demonstrates proper defensive programming and input sanitization.
 */
public class ValidationUtil {

    private static final Pattern PHONE_PATTERN = Pattern.compile("^[0-9]{10}$");
    private static final Pattern COLLEGE_ID_PATTERN = Pattern.compile("^[A-Za-z0-9\\-_]{3,20}$");
    private static final Pattern VEHICLE_REG_PATTERN = Pattern.compile("^[A-Za-z0-9\\-\\s]{4,20}$");

    /**
     * Checks if a string is null or blank.
     */
    public static boolean isBlank(String str) {
        return str == null || str.trim().isEmpty();
    }

    /**
     * Validates a person's full name.
     */
    public static boolean isValidName(String name) {
        if (isBlank(name)) return false;
        String trimmed = name.trim();
        return trimmed.length() >= 2 && trimmed.length() <= 100;
    }

    /**
     * Validates a college identification number.
     */
    public static boolean isValidCollegeId(String collegeId) {
        if (isBlank(collegeId)) return false;
        return COLLEGE_ID_PATTERN.matcher(collegeId.trim()).matches();
    }

    /**
     * Validates a 10-digit Indian/standard mobile phone number.
     */
    public static boolean isValidPhone(String phone) {
        if (isBlank(phone)) return false;
        String sanitized = phone.replaceAll("[^0-9]", "");
        return PHONE_PATTERN.matcher(sanitized).matches();
    }

    /**
     * Validates and cleans a vehicle registration number (e.g. KA-01-AB-1234).
     */
    public static boolean isValidRegistrationNumber(String regNo) {
        if (isBlank(regNo)) return false;
        return VEHICLE_REG_PATTERN.matcher(regNo.trim()).matches();
    }

    /**
     * Standardizes vehicle registration numbers to uppercase trimmed format.
     */
    public static String cleanRegistrationNumber(String regNo) {
        if (regNo == null) return "";
        return regNo.trim().toUpperCase().replaceAll("\\s+", "-");
    }
}
