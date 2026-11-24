package ua.restaurant.api;

import java.util.List;

public record ApiError(int status, String error, String message, String path, List<Violation> violations) {

    public record Violation(String field, String message) {
    }
}
