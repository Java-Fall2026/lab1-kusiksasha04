package ua.restaurant;

import java.time.LocalDate;
import java.time.LocalTime;

import ua.restaurant.model.Customer;
import ua.restaurant.model.Reservation;
import ua.restaurant.model.ReservationStatus;
import ua.restaurant.model.RestaurantTable;
import ua.restaurant.model.Zone;
import ua.restaurant.util.RestaurantUtils;

public class Main {

    public static void main(String[] args) {
        System.out.println("1. Record-об'єкти та їх компоненти");
        Customer customer = new Customer(" +380501234567 ", "  олександр петренко ", "Oleksandr@MAIL.com");
        RestaurantTable table = new RestaurantTable(12, 4, Zone.TERRACE);
        System.out.println("Клієнт: " + customer.name() + ", " + customer.phone() + ", " + customer.email());
        System.out.println("Столик №" + table.tableNumber() + ", місць: " + table.capacity() + ", зона: " + table.zone());

        System.out.println("\n2. Enum");
        for (Zone zone : Zone.values()) {
            System.out.println(zone + ": коефіцієнт ціни " + zone.getPriceFactor());
        }
        for (ReservationStatus status : ReservationStatus.values()) {
            System.out.println("Статус " + status + ", порядковий номер " + status.ordinal());
        }
        try {
            Zone.valueOf("GARDEN");
        } catch (IllegalArgumentException e) {
            System.out.println("valueOf(\"GARDEN\"): " + e.getMessage());
        }

        System.out.println("\n3. Switch-вирази");
        for (Zone zone : Zone.values()) {
            System.out.println(zone + " -> " + RestaurantUtils.zoneDescription(zone));
        }
        Reservation reservation = new Reservation(customer, table, LocalDate.of(2026, 10, 10),
                LocalTime.of(18, 0), LocalTime.of(20, 30), 3, ReservationStatus.PENDING);
        for (ReservationStatus status : ReservationStatus.values()) {
            reservation.setStatus(status);
            System.out.println(status + " -> " + RestaurantUtils.availableAction(reservation.getStatus()));
        }

        System.out.println("\n4. Record з невалідними даними");
        try {
            new RestaurantTable(250, 4, Zone.BAR);
        } catch (IllegalArgumentException e) {
            System.out.println(e.getMessage());
        }
        try {
            new Customer("+380931234567", "Марія", "maria-mail.com");
        } catch (IllegalArgumentException e) {
            System.out.println(e.getMessage());
        }
        try {
            new RestaurantTable(5, 4, null);
        } catch (IllegalArgumentException e) {
            System.out.println(e.getMessage());
        }

        System.out.println("\n5. equals і hashCode, згенеровані компілятором");
        Customer same1 = new Customer("+380671112233", "Іван", "ivan@mail.com");
        Customer same2 = new Customer("+380671112233", "Іван", "ivan@mail.com");
        Customer samePhone = new Customer("+380671112233", "Іван Петрович", "ivan@mail.com");
        System.out.println("== : " + (same1 == same2) + ", equals: " + same1.equals(same2)
                + ", hashCode рівні: " + (same1.hashCode() == same2.hashCode()));
        System.out.println("Той самий телефон, інше ім'я, equals: " + same1.equals(samePhone));

        System.out.println("\n6. Згенерований toString()");
        System.out.println(customer);
        System.out.println(table);
    }
}
