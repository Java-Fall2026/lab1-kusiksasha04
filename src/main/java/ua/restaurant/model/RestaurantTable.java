package ua.restaurant.model;

import java.util.Comparator;

import ua.restaurant.util.RestaurantUtils;

public record RestaurantTable(int tableNumber, int capacity, Zone zone) implements Comparable<RestaurantTable> {

    private static final Comparator<RestaurantTable> NATURAL_ORDER = Comparator
            .comparingInt(RestaurantTable::tableNumber)
            .thenComparingInt(RestaurantTable::capacity)
            .thenComparing(RestaurantTable::zone);

    public RestaurantTable {
        RestaurantUtils.validateTableNumber(tableNumber);
        RestaurantUtils.validateCapacity(capacity);
        RestaurantUtils.requireNotNull(zone, "Zone");
    }

    public String identity() {
        return String.valueOf(tableNumber);
    }

    @Override
    public int compareTo(RestaurantTable other) {
        return NATURAL_ORDER.compare(this, other);
    }
}
