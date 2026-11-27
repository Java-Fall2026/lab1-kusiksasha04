package ua.restaurant.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import ua.restaurant.model.RestaurantTable;
import ua.restaurant.model.Zone;

class TableRepositoryTest {

    private TableRepository tables;

    @BeforeEach
    void setUp() {
        tables = new TableRepository();
        tables.add(new RestaurantTable(7, 6, Zone.VIP));
        tables.add(new RestaurantTable(1, 4, Zone.MAIN_HALL));
        tables.add(new RestaurantTable(15, 6, Zone.MAIN_HALL));
        tables.add(new RestaurantTable(2, 2, Zone.TERRACE));
    }

    private static List<Integer> numbers(List<RestaurantTable> list) {
        return list.stream().map(RestaurantTable::tableNumber).toList();
    }

    @Test
    @DisplayName("Collections.sort uses the natural order by table number")
    void naturalOrder() {
        List<RestaurantTable> list = new ArrayList<>(tables.findAll());

        Collections.sort(list);

        assertEquals(List.of(1, 2, 7, 15), numbers(list));
    }

    @Test
    @DisplayName("Tables in the same zone are ordered by capacity descending")
    void multiLevelComparator() {
        List<RestaurantTable> sorted = tables.sortByZoneThenCapacityDesc();

        assertEquals(List.of(15, 1, 2, 7), numbers(sorted));
    }

    @Test
    @DisplayName("TreeSet keeps only one of two tables the comparator considers equal")
    void treeSetWithInconsistentComparator() {
        TreeSet<RestaurantTable> set = new TreeSet<>(Comparator.comparingInt(RestaurantTable::capacity));

        set.add(new RestaurantTable(1, 4, Zone.MAIN_HALL));
        set.add(new RestaurantTable(8, 4, Zone.BAR));

        assertEquals(1, set.size());
    }

    @Test
    @DisplayName("Search by zone name returns only matching zones")
    void findByZoneName() {
        assertEquals(List.of(1, 15), numbers(tables.findByZoneName("hall")));
    }

    @Test
    @DisplayName("Search by capacity range includes both bounds and excludes values just outside")
    void findByCapacityRange() {
        TableRepository repository = new TableRepository();
        repository.add(new RestaurantTable(1, 2, Zone.BAR));
        repository.add(new RestaurantTable(2, 3, Zone.BAR));
        repository.add(new RestaurantTable(3, 4, Zone.BAR));
        repository.add(new RestaurantTable(4, 5, Zone.BAR));
        repository.add(new RestaurantTable(5, 6, Zone.BAR));

        assertEquals(List.of(2, 3, 4), numbers(repository.findByCapacityRange(3, 5)));
    }

    @Test
    @DisplayName("Search by two conditions skips tables matching only one")
    void findByZoneAndMinCapacity() {
        assertEquals(List.of(15), numbers(tables.findByZoneAndMinCapacity(Zone.MAIN_HALL, 5)));
    }

    @Test
    @DisplayName("Single table search returns Optional.empty when nothing matches")
    void findFirstForGuestsEmpty() {
        assertTrue(tables.findFirstForGuests(25).isEmpty());
    }

    @Test
    @DisplayName("groupingBy returns one group per zone with expected sizes")
    void groupByZone() {
        Map<Zone, List<RestaurantTable>> groups = tables.groupByZone();

        assertEquals(3, groups.size());
        assertEquals(2, groups.get(Zone.MAIN_HALL).size());
    }

    @Test
    @DisplayName("Reusing a consumed stream throws IllegalStateException")
    void streamReuseThrows() {
        Stream<RestaurantTable> stream = tables.stream();
        stream.count();

        assertThrows(IllegalStateException.class, stream::count);
    }
}
