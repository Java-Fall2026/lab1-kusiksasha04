package ua.restaurant.repository;

import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ua.restaurant.exception.DuplicateIdentityException;

public class GenericRepository<T> {

    private static final Logger log = LoggerFactory.getLogger(GenericRepository.class);

    private final String name;
    private final IdentityExtractor<T> extractor;
    private final Map<String, T> items = new LinkedHashMap<>();

    public GenericRepository(String name, IdentityExtractor<T> extractor) {
        this.name = name;
        this.extractor = extractor;
    }

    public synchronized void add(T item) {
        String identity = identityOf(item);
        if (items.containsKey(identity)) {
            log.error("Duplicate identity '{}' in repository '{}'", identity, name);
            throw new DuplicateIdentityException(identity, name);
        }
        items.put(identity, item);
        log.info("Added '{}' to repository '{}'", identity, name);
    }

    public synchronized boolean addIfAbsent(T item) {
        String identity = identityOf(item);
        if (items.putIfAbsent(identity, item) != null) {
            log.warn("Skipped duplicate '{}' in repository '{}'", identity, name);
            return false;
        }
        log.info("Added '{}' to repository '{}'", identity, name);
        return true;
    }

    public synchronized void addAll(Collection<? extends T> newItems) {
        int added = 0;
        for (T item : newItems) {
            String identity = identityOf(item);
            if (items.putIfAbsent(identity, item) == null) {
                added++;
            } else {
                log.warn("Skipped duplicate '{}' in repository '{}'", identity, name);
            }
        }
        log.info("Added {} of {} items to repository '{}'", added, newItems.size(), name);
    }

    public synchronized boolean replace(T item) {
        String identity = identityOf(item);
        if (!items.containsKey(identity)) {
            log.warn("Nothing to replace: '{}' not found in repository '{}'", identity, name);
            return false;
        }
        items.put(identity, item);
        log.info("Replaced '{}' in repository '{}'", identity, name);
        return true;
    }

    public synchronized boolean removeByIdentity(String identity) {
        if (items.remove(identity) == null) {
            log.warn("Nothing to remove: '{}' not found in repository '{}'", identity, name);
            return false;
        }
        log.info("Removed '{}' from repository '{}'", identity, name);
        return true;
    }

    public synchronized Optional<T> findByIdentity(String identity) {
        return Optional.ofNullable(items.get(identity));
    }

    public synchronized List<T> findAll() {
        return List.copyOf(items.values());
    }

    public synchronized int size() {
        return items.size();
    }

    public synchronized List<T> findAllSorted(Comparator<T> comparator) {
        Objects.requireNonNull(comparator, "Comparator must not be null");
        return items.values().stream()
                .sorted(comparator)
                .toList();
    }

    public synchronized List<T> findAllSortedByIdentity(SortOrder order) {
        Objects.requireNonNull(order, "Sort order must not be null");
        Comparator<String> byKey = order == SortOrder.ASC ? Comparator.naturalOrder() : Comparator.reverseOrder();
        List<T> result = items.entrySet().stream()
                .sorted(Map.Entry.comparingByKey(byKey))
                .map(Map.Entry::getValue)
                .toList();
        return logResult("Sort by identity " + order, result);
    }

    public Stream<T> stream() {
        return findAll().stream();
    }

    protected <R> List<R> logResult(String action, List<R> result) {
        if (result.isEmpty()) {
            log.warn("{} in repository '{}': nothing found", action, name);
        } else {
            log.info("{} in repository '{}': {} items", action, name, result.size());
        }
        return result;
    }

    public String identityOf(T item) {
        if (item == null) {
            log.error("Null item passed to repository '{}'", name);
            throw new IllegalArgumentException("Item must not be null");
        }
        String identity = extractor.identityOf(item);
        if (identity == null || identity.isBlank()) {
            log.error("Item with blank identity passed to repository '{}'", name);
            throw new IllegalArgumentException("Identity must not be blank");
        }
        return identity;
    }
}
