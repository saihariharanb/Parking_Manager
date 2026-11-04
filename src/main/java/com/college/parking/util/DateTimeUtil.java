package com.college.parking.util;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Utility class for formatting and calculating date, time, and session durations.
 */
public class DateTimeUtil {

    public static final DateTimeFormatter DISPLAY_FORMATTER = DateTimeFormatter.ofPattern("dd-MMM-yyyy hh:mm a");
    public static final DateTimeFormatter TIME_ONLY_FORMATTER = DateTimeFormatter.ofPattern("hh:mm a");
    public static final DateTimeFormatter DATE_ONLY_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    /**
     * Formats a LocalDateTime object into a readable display string (e.g. 21-Oct-2026 09:30 AM).
     *
     * @param dateTime LocalDateTime to format
     * @return Formatted string or "-" if null
     */
    public static String format(LocalDateTime dateTime) {
        if (dateTime == null) {
            return "-";
        }
        return dateTime.format(DISPLAY_FORMATTER);
    }

    /**
     * Calculates the duration between two timestamps and returns a human-friendly string.
     * (e.g. "2 hrs 15 mins" or "45 mins").
     *
     * @param start start timestamp
     * @param end end timestamp (defaults to current time if null)
     * @return formatted duration string
     */
    public static String formatDuration(LocalDateTime start, LocalDateTime end) {
        if (start == null) {
            return "-";
        }
        if (end == null) {
            end = LocalDateTime.now();
        }
        Duration duration = Duration.between(start, end);
        long hours = duration.toHours();
        long minutes = duration.toMinutesPart();

        if (hours > 0) {
            return String.format("%d hr%s %d min%s", 
                hours, (hours == 1 ? "" : "s"), 
                minutes, (minutes == 1 ? "" : "s"));
        } else {
            return String.format("%d min%s", minutes, (minutes == 1 ? "" : "s"));
        }
    }
}
