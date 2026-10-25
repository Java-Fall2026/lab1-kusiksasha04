package ua.restaurant.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.LocalDate;
import java.time.LocalTime;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import ua.restaurant.model.Customer;
import ua.restaurant.model.Reservation;
import ua.restaurant.model.ReservationStatus;
import ua.restaurant.model.RestaurantOrder;
import ua.restaurant.model.RestaurantTable;
import ua.restaurant.model.Zone;

class RestaurantUtilsTest {

    private static Reservation reservation() {
        Customer customer = new Customer("+380501234567", "ivan petrenko", "ivan@mail.com");
        RestaurantTable table = new RestaurantTable(12, 4, Zone.TERRACE);
        return new Reservation(customer, table, LocalDate.of(2026, 10, 10), LocalTime.of(18, 0), LocalTime.of(20, 30), 3, ReservationStatus.PENDING);
    }

    @Test
    @DisplayName("Reservation is created with the given values")
    void createsReservation() {
        Reservation reservation = reservation();

        assertEquals(3, reservation.getGuestCount());
        assertEquals(ReservationStatus.PENDING, reservation.getStatus());
    }

    @Test
    @DisplayName("Duration is counted in minutes between start and end time")
    void durationMinutes() {
        assertEquals(150, RestaurantUtils.durationMinutes(reservation()));
    }

    @Test
    @DisplayName("Final amount is total minus discount")
    void finalAmount() {
        RestaurantOrder order = new RestaurantOrder("ord-1", reservation(), 1000.0, 100.0);

        assertEquals(900.0, RestaurantUtils.finalAmount(order));
    }

    @Test
    @DisplayName("Discount greater than total is rejected by the setter")
    void rejectsTooBigDiscount() {
        RestaurantOrder order = new RestaurantOrder("ord-1", reservation(), 1000.0, 100.0);

        assertThrows(IllegalArgumentException.class, () -> order.setDiscountAmount(5000.0));
    }
}
