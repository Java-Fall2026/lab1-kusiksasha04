package ua.restaurant.model;

import ua.restaurant.util.RestaurantUtils;

public record RestaurantTable(int tableNumber, int capacity, Zone zone) {

    public RestaurantTable {
        RestaurantUtils.validateTableNumber(tableNumber);
        RestaurantUtils.validateCapacity(capacity);
        RestaurantUtils.requireNotNull(zone, "Zone");
    }
}
