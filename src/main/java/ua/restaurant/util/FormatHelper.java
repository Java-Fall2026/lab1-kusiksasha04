package ua.restaurant.util;

import java.util.Locale;

final class FormatHelper {

    private FormatHelper() {
    }

    static String normalize(String value) {
        return value.trim().toUpperCase(Locale.ROOT);
    }

    static String capitalize(String value) {
        StringBuilder result = new StringBuilder();
        for (String word : value.trim().split("\\s+")) {
            if (result.length() > 0) {
                result.append(' ');
            }
            result.append(Character.toUpperCase(word.charAt(0))).append(word.substring(1));
        }
        return result.toString();
    }

    static String formatMoney(double amount) {
        return String.format(Locale.US, "%.2f", amount);
    }
}
