package ua.restaurant.validation;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import ua.restaurant.model.Customer;
import ua.restaurant.model.Reservation;
import ua.restaurant.model.ReservationStatus;
import ua.restaurant.model.RestaurantOrder;
import ua.restaurant.model.RestaurantTable;
import ua.restaurant.model.Zone;

class ValidationTest {

    private static final ValidatorFactory FACTORY = Validation.buildDefaultValidatorFactory();
    private static final Validator VALIDATOR = FACTORY.getValidator();

    @AfterAll
    static void close() {
        FACTORY.close();
    }

    private static Reservation reservation() {
        Customer customer = new Customer("+380501234567", "ivan petrenko", "ivan@mail.com");
        return new Reservation(customer, new RestaurantTable(12, 4, Zone.TERRACE), LocalDate.of(2026, 11, 2),
                LocalTime.of(18, 0), LocalTime.of(20, 0), 2, ReservationStatus.CONFIRMED);
    }

    static Stream<Arguments> invalidTables() {
        return Stream.of(
                Arguments.of(new RestaurantTable(0, 4, Zone.BAR), "tableNumber"),
                Arguments.of(new RestaurantTable(250, 4, Zone.BAR), "tableNumber"),
                Arguments.of(new RestaurantTable(1, 0, Zone.BAR), "capacity"),
                Arguments.of(new RestaurantTable(1, 4, null), "zone"));
    }

    static Stream<Arguments> invalidOrders() {
        return Stream.of(
                Arguments.of(new RestaurantOrder(" ", reservation(), 100.0, 0.0), "orderId"),
                Arguments.of(new RestaurantOrder("ORDER-1", reservation(), 100.0, 0.0), "orderId"),
                Arguments.of(new RestaurantOrder("ORD-1", null, 100.0, 0.0), "reservation"),
                Arguments.of(new RestaurantOrder("ORD-1", reservation(), -1.0, -1.0), "totalAmount"));
    }

    @ParameterizedTest
    @MethodSource("invalidTables")
    @DisplayName("Standard annotations on RestaurantTable report the invalid field")
    void tableAnnotations(RestaurantTable table, String field) {
        assertTrue(VALIDATOR.validate(table).stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals(field)));
    }

    @ParameterizedTest
    @MethodSource("invalidOrders")
    @DisplayName("Standard annotations on RestaurantOrder report the invalid field")
    void orderAnnotations(RestaurantOrder order, String field) {
        assertTrue(VALIDATOR.validate(order).stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals(field)));
    }

    @Test
    @DisplayName("Reservation rule rejects end before start and more guests than capacity")
    void consistentReservation() {
        Customer customer = new Customer("+380501234567", "ivan petrenko", "ivan@mail.com");
        RestaurantTable table = new RestaurantTable(2, 2, Zone.TERRACE);
        Reservation invalid = new Reservation(customer, table, LocalDate.of(2026, 11, 2),
                LocalTime.of(20, 0), LocalTime.of(18, 0), 5, ReservationStatus.PENDING);

        Set<String> fields = VALIDATOR.validate(invalid).stream()
                .map(v -> v.getPropertyPath().toString())
                .collect(Collectors.toSet());

        assertTrue(VALIDATOR.validate(reservation()).isEmpty());
        assertTrue(fields.containsAll(Set.of("endTime", "guestCount")));
    }

    @Test
    @DisplayName("Custom annotation accepts discount within total and rejects a bigger one")
    void discountNotGreaterThanTotal() {
        RestaurantOrder valid = new RestaurantOrder("ORD-1", reservation(), 500.0, 500.0);
        RestaurantOrder invalid = new RestaurantOrder("ORD-2", reservation(), 500.0, 700.0);

        assertTrue(VALIDATOR.validate(valid).isEmpty());
        assertFalse(VALIDATOR.validate(invalid).isEmpty());
    }
}
