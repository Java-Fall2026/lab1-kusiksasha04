package ua.restaurant;

import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ua.restaurant.io.TableLoader;
import ua.restaurant.model.Customer;
import ua.restaurant.model.Reservation;
import ua.restaurant.model.ReservationStatus;
import ua.restaurant.model.RestaurantOrder;
import ua.restaurant.model.RestaurantTable;
import ua.restaurant.model.Zone;

public class Main {

    private static final Logger log = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) {
        System.out.println("1. Читання файла з коректними й некоректними рядками");
        List<RestaurantTable> tables = TableLoader.load(Path.of("data", "tables.csv"));
        System.out.println("Прочитано столиків: " + tables.size());
        for (RestaurantTable loaded : tables) {
            System.out.println("  " + loaded);
        }

        System.out.println("\n2. Відсутній файл");
        List<RestaurantTable> missing = TableLoader.load(Path.of("data", "missing.csv"));
        System.out.println("Програма не зупинилась, прочитано столиків: " + missing.size());

        System.out.println("\n3. Некоректне значення в сеттері");
        Customer customer = new Customer("+380501234567", "олександр петренко", "oleksandr@mail.com");
        log.info("Created customer {}", customer.name());
        RestaurantTable table = new RestaurantTable(12, 4, Zone.TERRACE);
        log.info("Created table {}", table);
        Reservation reservation = new Reservation(customer, table, LocalDate.of(2026, 10, 10), LocalTime.of(18, 0), LocalTime.of(20, 30), 3, ReservationStatus.PENDING);
        log.info("Created reservation for table {} on {} at {}, guests: {}", table.tableNumber(), reservation.getReservationDate(), reservation.getStartTime(), reservation.getGuestCount());
        RestaurantOrder order = new RestaurantOrder("ord-1", reservation, 1000.0, 100.0);
        log.info("Created order {} with total {}", order.getOrderId(), order.getTotalAmount());
        try {
            order.setDiscountAmount(5000.0);
        } catch (IllegalArgumentException e) {
            System.out.println("Знижку відхилено: " + e.getMessage());
            log.warn("Rejected discount for order {}: {}", order.getOrderId(), e.getMessage());
        }
        System.out.println("Знижка залишилась: " + order.getDiscountAmount());

        System.out.println("\n4. Рівні логування (див. лог вище)");
        System.out.println("INFO  — початок і кінець читання файла, створення об'єктів");
        System.out.println("WARN  — пропущені некоректні рядки, відхилена знижка");
        System.out.println("ERROR — відсутній файл");
    }
}
