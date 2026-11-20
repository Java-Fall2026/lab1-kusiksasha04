package ua.restaurant.repository;

import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ua.restaurant.model.RestaurantTable;
import ua.restaurant.model.Zone;

public class TableRepository extends GenericRepository<RestaurantTable> {

    private static final Logger log = LoggerFactory.getLogger(TableRepository.class);

    public TableRepository() {
        super("tables", RestaurantTable::identity);
    }

    public List<RestaurantTable> sortByCapacity() {
        return logResult("Sort by capacity", findAllSorted(Comparator.comparingInt(RestaurantTable::capacity)));
    }

    public List<RestaurantTable> sortByZoneThenCapacityDesc() {
        Comparator<RestaurantTable> comparator = Comparator.comparing(RestaurantTable::zone)
                .thenComparing(Comparator.comparingInt(RestaurantTable::capacity).reversed());
        return logResult("Sort by zone, then capacity desc", findAllSorted(comparator));
    }

    public List<RestaurantTable> findByZoneName(String part) {
        String upper = part.toUpperCase(Locale.ROOT);
        List<RestaurantTable> result = stream()
                .filter(table -> table.zone().name().contains(upper))
                .sorted()
                .toList();
        return logResult("Search by zone name '" + part + "'", result);
    }

    public List<RestaurantTable> findByCapacityRange(int min, int max) {
        List<RestaurantTable> result = stream()
                .filter(table -> table.capacity() >= min && table.capacity() <= max)
                .sorted(Comparator.comparingInt(RestaurantTable::capacity))
                .toList();
        return logResult("Search by capacity " + min + ".." + max, result);
    }

    public List<RestaurantTable> findByZoneAndMinCapacity(Zone zone, int minCapacity) {
        List<RestaurantTable> result = stream()
                .filter(table -> table.zone() == zone)
                .filter(table -> table.capacity() >= minCapacity)
                .sorted()
                .toList();
        return logResult("Search by zone " + zone + " and capacity >= " + minCapacity, result);
    }

    public Optional<RestaurantTable> findFirstForGuests(int guests) {
        Optional<RestaurantTable> result = stream()
                .filter(table -> table.capacity() >= guests)
                .sorted(Comparator.comparingInt(RestaurantTable::capacity))
                .findFirst();
        logResult("Search first table for " + guests + " guests", result.stream().toList());
        return result;
    }

    public Map<Zone, List<RestaurantTable>> groupByZone() {
        Map<Zone, List<RestaurantTable>> result = stream()
                .collect(Collectors.groupingBy(RestaurantTable::zone));
        log.info("Grouped tables by zone: {} groups", result.size());
        return result;
    }

    public Map<Zone, Long> countByZone() {
        Map<Zone, Long> result = stream()
                .collect(Collectors.groupingBy(RestaurantTable::zone, Collectors.counting()));
        log.info("Counted tables by zone: {} groups", result.size());
        return result;
    }
}
