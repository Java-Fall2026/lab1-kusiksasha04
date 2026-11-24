package ua.restaurant.io;

import java.util.List;

public record LoadResult(int read, int added, List<Rejected> rejected) {

    public record Rejected(int record, List<Violation> violations) {
    }

    public record Violation(String field, String message) {
    }
}
