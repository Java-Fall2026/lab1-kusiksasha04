package ua.restaurant.repository;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

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

    public void add(T item) {
        String identity = identityOf(item);
        if (items.containsKey(identity)) {
            log.error("Duplicate identity '{}' in repository '{}'", identity, name);
            throw new DuplicateIdentityException(identity, name);
        }
        items.put(identity, item);
        log.info("Added '{}' to repository '{}'", identity, name);
    }

    public boolean addIfAbsent(T item) {
        String identity = identityOf(item);
        if (items.putIfAbsent(identity, item) != null) {
            log.warn("Skipped duplicate '{}' in repository '{}'", identity, name);
            return false;
        }
        log.info("Added '{}' to repository '{}'", identity, name);
        return true;
    }

    public void addAll(Collection<? extends T> newItems) {
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

    public boolean removeByIdentity(String identity) {
        if (items.remove(identity) == null) {
            log.warn("Nothing to remove: '{}' not found in repository '{}'", identity, name);
            return false;
        }
        log.info("Removed '{}' from repository '{}'", identity, name);
        return true;
    }

    public Optional<T> findByIdentity(String identity) {
        return Optional.ofNullable(items.get(identity));
    }

    public List<T> findAll() {
        return List.copyOf(items.values());
    }

    public int size() {
        return items.size();
    }

    private String identityOf(T item) {
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
