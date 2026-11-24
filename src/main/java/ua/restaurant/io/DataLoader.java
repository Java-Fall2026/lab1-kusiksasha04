package ua.restaurant.io;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import ua.restaurant.repository.GenericRepository;

public class DataLoader {

    private static final Logger log = LoggerFactory.getLogger(DataLoader.class);

    private final Validator validator;

    public DataLoader(Validator validator) {
        this.validator = validator;
    }

    public <T> LoadResult load(Path file, Class<T> type, GenericRepository<T> repository) {
        List<T> items;
        try {
            items = new FileStorage<>(type).load(file);
        } catch (IOException | IllegalArgumentException e) {
            String reason = e instanceof JsonProcessingException json ? json.getOriginalMessage() : e.getMessage();
            log.error("Failed to read file {}: {}", file, reason);
            return new LoadResult(0, 0, List.of(rejectedRecord(0, "file", "не вдалося прочитати файл: " + reason)));
        }

        List<LoadResult.Rejected> rejected = new ArrayList<>();
        int added = 0;
        for (int i = 0; i < items.size(); i++) {
            int record = i + 1;
            T item = items.get(i);
            Set<ConstraintViolation<T>> violations = validator.validate(item);
            if (!violations.isEmpty()) {
                List<LoadResult.Violation> details = violations.stream()
                        .map(v -> new LoadResult.Violation(v.getPropertyPath().toString(), v.getMessage()))
                        .sorted(Comparator.comparing(LoadResult.Violation::field).thenComparing(LoadResult.Violation::message))
                        .toList();
                rejected.add(new LoadResult.Rejected(record, details));
            } else if (repository.addIfAbsent(item)) {
                added++;
            } else {
                rejected.add(rejectedRecord(record, "identity", "дублікат ідентичності"));
            }
        }

        for (LoadResult.Rejected r : rejected) {
            log.warn("Rejected record {} in {}: {}", r.record(), file, describe(r));
        }
        log.info("File {}: read {}, added {}, rejected {}", file, items.size(), added, rejected.size());
        return new LoadResult(items.size(), added, rejected);
    }

    public static String describe(LoadResult.Rejected rejected) {
        return rejected.violations().stream()
                .map(v -> v.field() + " — " + v.message())
                .collect(Collectors.joining("; "));
    }

    private static LoadResult.Rejected rejectedRecord(int record, String field, String message) {
        return new LoadResult.Rejected(record, List.of(new LoadResult.Violation(field, message)));
    }
}
