package ua.restaurant.io;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import jakarta.validation.Validation;
import jakarta.validation.ValidatorFactory;
import ua.restaurant.model.RestaurantOrder;
import ua.restaurant.model.RestaurantTable;
import ua.restaurant.repository.OrderRepository;
import ua.restaurant.repository.TableRepository;

class DataLoaderTest {

    private static final ValidatorFactory FACTORY = Validation.buildDefaultValidatorFactory();

    @TempDir
    Path dir;

    private final DataLoader loader = new DataLoader(FACTORY.getValidator());

    @AfterAll
    static void close() {
        FACTORY.close();
    }

    @Test
    @DisplayName("Only valid records reach the repository, one rejected entry per record")
    void invalidRecordsAreRejected() throws IOException {
        Path file = dir.resolve("tables.json");
        Files.writeString(file, """
                [
                  { "tableNumber": 1, "capacity": 4, "zone": "BAR" },
                  { "tableNumber": 250, "capacity": 0, "zone": "BAR" },
                  { "tableNumber": 2, "capacity": 0, "zone": "VIP" },
                  { "tableNumber": 3, "capacity": 2, "zone": "TERRACE" }
                ]
                """);
        TableRepository tables = new TableRepository();

        LoadResult result = loader.load(file, RestaurantTable.class, tables);

        assertEquals(4, result.read());
        assertEquals(2, tables.size());
        assertEquals(2, result.rejected().size());
        assertEquals(2, result.rejected().get(0).violations().size());
    }

    @Test
    @DisplayName("Duplicate identity goes to rejected instead of throwing")
    void duplicateIsRejected() throws IOException {
        Path file = dir.resolve("tables.json");
        Files.writeString(file, """
                [
                  { "tableNumber": 1, "capacity": 4, "zone": "BAR" },
                  { "tableNumber": 1, "capacity": 6, "zone": "VIP" }
                ]
                """);
        TableRepository tables = new TableRepository();

        LoadResult result = loader.load(file, RestaurantTable.class, tables);

        assertEquals(1, result.added());
        assertEquals("identity", result.rejected().get(0).violations().get(0).field());
    }

    @Test
    @DisplayName("Inconsistent reservation rejects one order, not the whole file")
    void inconsistentReservationRejectsOneRecord() throws IOException {
        Path file = dir.resolve("orders.yaml");
        Files.writeString(file, """
                - orderId: ORD-1
                  reservation:
                    customer: { phone: "+380501234567", name: Ivan Petrenko, email: ivan@mail.com }
                    table: { tableNumber: 2, capacity: 2, zone: TERRACE }
                    reservationDate: 2026-11-02
                    startTime: "18:00"
                    endTime: "20:00"
                    guestCount: 2
                    status: CONFIRMED
                  totalAmount: 500.0
                  discountAmount: 0.0
                - orderId: ORD-2
                  reservation:
                    customer: { phone: "+380501234567", name: Ivan Petrenko, email: ivan@mail.com }
                    table: { tableNumber: 2, capacity: 2, zone: TERRACE }
                    reservationDate: 2026-11-02
                    startTime: "18:00"
                    endTime: "20:00"
                    guestCount: 5
                    status: CONFIRMED
                  totalAmount: 500.0
                  discountAmount: 0.0
                """);
        OrderRepository orders = new OrderRepository();

        LoadResult result = loader.load(file, RestaurantOrder.class, orders);

        assertEquals(1, orders.size());
        assertEquals(1, result.rejected().size());
        assertEquals("reservation.guestCount", result.rejected().get(0).violations().get(0).field());
    }

    @Test
    @DisplayName("Broken and missing files do not break loading")
    void brokenAndMissingFiles() throws IOException {
        Path broken = dir.resolve("broken.json");
        Files.writeString(broken, "[ { \"tableNumber\": 1, ");
        TableRepository tables = new TableRepository();

        LoadResult brokenResult = loader.load(broken, RestaurantTable.class, tables);
        LoadResult missingResult = loader.load(dir.resolve("missing.json"), RestaurantTable.class, tables);

        assertEquals(1, brokenResult.rejected().size());
        assertEquals(1, missingResult.rejected().size());
        assertEquals(0, tables.size());
    }
}
