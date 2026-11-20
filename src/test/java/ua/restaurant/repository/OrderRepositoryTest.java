package ua.restaurant.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import ua.restaurant.model.Customer;
import ua.restaurant.model.Reservation;
import ua.restaurant.model.ReservationStatus;
import ua.restaurant.model.RestaurantOrder;
import ua.restaurant.model.RestaurantTable;
import ua.restaurant.model.Zone;

class OrderRepositoryTest {

    private static RestaurantOrder order(String id, double amount) {
        Customer customer = new Customer("+380501234567", "ivan petrenko", "ivan@mail.com");
        RestaurantTable table = new RestaurantTable(12, 4, Zone.TERRACE);
        Reservation reservation = new Reservation(customer, table, LocalDate.of(2026, 10, 10),
                LocalTime.of(18, 0), LocalTime.of(20, 0), 2, ReservationStatus.CONFIRMED);
        return new RestaurantOrder(id, reservation, amount, 0.0);
    }

    private static List<String> ids(List<RestaurantOrder> list) {
        return list.stream().map(RestaurantOrder::getOrderId).toList();
    }

    @Test
    @DisplayName("findAllSortedByIdentity in ASC and DESC returns reversed lists")
    void sortByIdentityBothWays() {
        OrderRepository orders = new OrderRepository();
        orders.add(order("ORD-502", 100.0));
        orders.add(order("ORD-501", 200.0));
        orders.add(order("ORD-503", 300.0));

        List<RestaurantOrder> desc = new ArrayList<>(orders.findAllSortedByIdentity(SortOrder.DESC));
        Collections.reverse(desc);

        assertEquals(List.of("ORD-501", "ORD-502", "ORD-503"), ids(orders.findAllSortedByIdentity(SortOrder.ASC)));
        assertEquals(ids(orders.findAllSortedByIdentity(SortOrder.ASC)), ids(desc));
    }

    @Test
    @DisplayName("Orders with less than one unit price difference are sorted correctly")
    void sortsFractionalAmounts() {
        OrderRepository orders = new OrderRepository();
        orders.add(order("ORD-1", 850.40));
        orders.add(order("ORD-2", 850.90));

        assertEquals(List.of("ORD-2", "ORD-1"), ids(orders.sortByAmountDesc()));
    }

    @Test
    @DisplayName("Total revenue of an empty repository is zero")
    void totalRevenueOfEmptyRepository() {
        assertEquals(0.0, new OrderRepository().totalRevenue());
    }
}
