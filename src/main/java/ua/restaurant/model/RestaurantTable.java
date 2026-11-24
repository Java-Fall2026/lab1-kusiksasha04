package ua.restaurant.model;

import java.util.Comparator;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record RestaurantTable(
        @Min(value = 1, message = "номер столика має бути не менше 1")
        @Max(value = 200, message = "номер столика має бути не більше 200")
        int tableNumber,
        @Positive(message = "місткість має бути додатною")
        @Max(value = 20, message = "місткість має бути не більше 20")
        int capacity,
        @NotNull(message = "зона є обов'язковою")
        Zone zone) implements Comparable<RestaurantTable> {

    private static final Comparator<RestaurantTable> NATURAL_ORDER = Comparator
            .comparingInt(RestaurantTable::tableNumber)
            .thenComparingInt(RestaurantTable::capacity)
            .thenComparing(RestaurantTable::zone, Comparator.nullsLast(Comparator.naturalOrder()));

    public String identity() {
        return String.valueOf(tableNumber);
    }

    @Override
    public int compareTo(RestaurantTable other) {
        return NATURAL_ORDER.compare(this, other);
    }
}
