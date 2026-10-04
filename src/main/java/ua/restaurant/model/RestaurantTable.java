package ua.restaurant.model;

import java.util.Objects;

import ua.common.BaseEntity;
import ua.restaurant.util.RestaurantUtils;

public class RestaurantTable extends BaseEntity {

    private final int tableNumber;
    private final int capacity;
    private final String zone;

    private RestaurantTable(int tableNumber, int capacity, String zone) {
        super();
        this.tableNumber = RestaurantUtils.validateTableNumber(tableNumber);
        this.capacity = RestaurantUtils.validateCapacity(capacity);
        this.zone = RestaurantUtils.validateZone(zone);
    }

    public static RestaurantTable of(int tableNumber, int capacity, String zone) {
        return new RestaurantTable(tableNumber, capacity, zone);
    }

    public int getTableNumber() {
        return tableNumber;
    }

    public int getCapacity() {
        return capacity;
    }

    public String getZone() {
        return zone;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        return tableNumber == ((RestaurantTable) o).tableNumber;
    }

    @Override
    public int hashCode() {
        return Objects.hash(tableNumber);
    }

    @Override
    public String toString() {
        return "RestaurantTable{tableNumber=" + tableNumber + ", capacity=" + capacity
                + ", zone='" + zone + "', createdAt=" + createdAt + "}";
    }
}
