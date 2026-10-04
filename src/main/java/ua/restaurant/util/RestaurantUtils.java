package ua.restaurant.util;

import java.time.Duration;
import java.time.LocalTime;

import ua.restaurant.model.Reservation;
import ua.restaurant.model.RestaurantOrder;

public final class RestaurantUtils {

    private RestaurantUtils() {
    }

    public static <T> T requireNotNull(T value, String field) {
        return ValidationHelper.requireNotNull(value, field);
    }

    public static String validatePhone(String phone) {
        return ValidationHelper.requireNotBlank(phone, "Phone").trim();
    }

    public static String validateName(String name) {
        ValidationHelper.requireNotBlank(name, "Name");
        return FormatHelper.capitalize(name);
    }

    public static String validateEmail(String email) {
        ValidationHelper.requireNotBlank(email, "Email");
        String normalized = email.trim().toLowerCase();
        if (!normalized.contains("@")) {
            throw new IllegalArgumentException("Email must contain '@', got: " + normalized);
        }
        return normalized;
    }

    public static int validateTableNumber(int number) {
        return ValidationHelper.requireInRange(number, 1, ValidationHelper.TABLE_NUMBER_MAX, "Table number");
    }

    public static int validateCapacity(int capacity) {
        return ValidationHelper.requireInRange(capacity, 1, ValidationHelper.CAPACITY_MAX, "Capacity");
    }

    public static String validateZone(String zone) {
        ValidationHelper.requireNotBlank(zone, "Zone");
        return ValidationHelper.requireOneOf(FormatHelper.normalize(zone), ValidationHelper.ZONES, "Zone");
    }

    public static String validateStatus(String status) {
        ValidationHelper.requireNotBlank(status, "Status");
        return ValidationHelper.requireOneOf(FormatHelper.normalize(status), ValidationHelper.STATUSES, "Status");
    }

    public static int validateGuestCount(int guestCount, int tableCapacity) {
        return ValidationHelper.requireInRange(guestCount, 1, tableCapacity, "Guest count");
    }

    public static LocalTime validateEndTime(LocalTime start, LocalTime end) {
        ValidationHelper.requireNotNull(end, "End time");
        if (!end.isAfter(start)) {
            throw new IllegalArgumentException("End time must be after " + start + ", got: " + end);
        }
        return end;
    }

    public static String validateOrderId(String orderId) {
        ValidationHelper.requireNotBlank(orderId, "Order id");
        return FormatHelper.normalize(orderId);
    }

    public static double validateTotalAmount(double total) {
        return ValidationHelper.requireInRange(total, 0.0, Double.MAX_VALUE, "Total amount");
    }

    public static double validateDiscount(double discount, double total) {
        return ValidationHelper.requireInRange(discount, 0.0, total, "Discount amount");
    }

    public static String formatMoney(double amount) {
        return FormatHelper.formatMoney(amount);
    }

    public static long durationMinutes(Reservation reservation) {
        return Duration.between(reservation.getStartTime(), reservation.getEndTime()).toMinutes();
    }

    public static double finalAmount(RestaurantOrder order) {
        return order.getTotalAmount() - order.getDiscountAmount();
    }
}
