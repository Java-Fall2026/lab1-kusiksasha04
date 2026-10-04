package ua.restaurant.util;

import java.util.List;

final class ValidationHelper {

    static final int TABLE_NUMBER_MAX = 200;
    static final int CAPACITY_MAX = 20;
    static final List<String> ZONES = List.of("MAIN_HALL", "TERRACE", "VIP", "BAR");
    static final List<String> STATUSES = List.of("PENDING", "CONFIRMED", "SEATED", "COMPLETED", "CANCELLED");

    private ValidationHelper() {
    }

    static <T> T requireNotNull(T value, String field) {
        if (value == null) {
            throw new IllegalArgumentException(field + " must not be null");
        }
        return value;
    }

    static String requireNotBlank(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(field + " must not be blank");
        }
        return value;
    }

    static double requirePositive(double value, String field) {
        if (!(value > 0)) {
            throw new IllegalArgumentException(field + " must be greater than 0, got: " + value);
        }
        return value;
    }

    static int requireInRange(int value, int min, int max, String field) {
        if (value < min || value > max) {
            throw new IllegalArgumentException(field + " must be between " + min + " and " + max + ", got: " + value);
        }
        return value;
    }

    static double requireInRange(double value, double min, double max, String field) {
        if (!(value >= min && value <= max)) {
            throw new IllegalArgumentException(field + " must be between " + min + " and " + max + ", got: " + value);
        }
        return value;
    }

    static String requireOneOf(String value, List<String> allowed, String field) {
        if (!allowed.contains(value)) {
            throw new IllegalArgumentException(field + " must be one of " + allowed + ", got: " + value);
        }
        return value;
    }
}
