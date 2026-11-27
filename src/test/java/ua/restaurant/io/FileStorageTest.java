package ua.restaurant.io;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import ua.restaurant.model.Customer;
import ua.restaurant.model.Reservation;
import ua.restaurant.model.ReservationStatus;
import ua.restaurant.model.RestaurantOrder;
import ua.restaurant.model.RestaurantTable;
import ua.restaurant.model.Zone;

class FileStorageTest {

    @TempDir
    Path dir;

    private static RestaurantOrder order(String id) {
        Customer customer = new Customer("+380501234567", "ivan petrenko", "ivan@mail.com");
        RestaurantTable table = new RestaurantTable(12, 4, Zone.TERRACE);
        Reservation reservation = new Reservation(customer, table, LocalDate.of(2026, 11, 2),
                LocalTime.of(18, 0), LocalTime.of(20, 0), 2, ReservationStatus.CONFIRMED);
        return new RestaurantOrder(id, reservation, 1000.0, 100.0);
    }

    @Test
    @DisplayName("JSON save and load return an equal list")
    void jsonRoundTrip() throws IOException {
        FileStorage<RestaurantTable> storage = new FileStorage<>(RestaurantTable.class);
        List<RestaurantTable> tables = List.of(new RestaurantTable(1, 4, Zone.BAR), new RestaurantTable(2, 6, Zone.VIP));
        Path file = dir.resolve("tables.json");

        storage.save(file, tables);

        assertEquals(tables, storage.load(file));
    }

    @Test
    @DisplayName("YAML save and load return an equal list")
    void yamlRoundTrip() throws IOException {
        FileStorage<RestaurantOrder> storage = new FileStorage<>(RestaurantOrder.class);
        List<RestaurantOrder> orders = List.of(order("ORD-1"), order("ORD-2"));
        Path file = dir.resolve("orders.yaml");

        storage.save(file, orders);
        List<RestaurantOrder> loaded = storage.load(file);

        assertEquals(orders, loaded);
        assertEquals(LocalDate.of(2026, 11, 2), loaded.get(0).getReservation().getReservationDate());
    }

    @Test
    @DisplayName("Dates are written as ISO strings")
    void dateIsIsoString() throws IOException {
        Path file = dir.resolve("orders.json");

        new FileStorage<>(RestaurantOrder.class).save(file, List.of(order("ORD-1")));

        assertTrue(Files.readString(file).contains("\"reservationDate\" : \"2026-11-02\""));
    }

    @Test
    @DisplayName("Unknown file extension throws an exception")
    void unknownExtensionThrows() {
        FileStorage<RestaurantTable> storage = new FileStorage<>(RestaurantTable.class);

        assertThrows(IllegalArgumentException.class, () -> storage.load(dir.resolve("tables.txt")));
    }
}
