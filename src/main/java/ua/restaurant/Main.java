package ua.restaurant;

import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ua.restaurant.exception.DuplicateIdentityException;
import ua.restaurant.io.TableLoader;
import ua.restaurant.model.Customer;
import ua.restaurant.model.Reservation;
import ua.restaurant.model.ReservationStatus;
import ua.restaurant.model.RestaurantOrder;
import ua.restaurant.model.RestaurantTable;
import ua.restaurant.model.Zone;
import ua.restaurant.repository.GenericRepository;

public class Main {

    private static final Logger log = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) {
        System.out.println("1. Три репозиторії різних типів і пошук за identity");
        GenericRepository<Customer> customers = new GenericRepository<>("customers", customer -> customer.phone());
        GenericRepository<RestaurantTable> tables = new GenericRepository<>("tables", table -> String.valueOf(table.tableNumber()));
        GenericRepository<RestaurantOrder> orders = new GenericRepository<>("orders", order -> order.getOrderId());

        Customer ivan = new Customer("+380501234567", "іван петренко", "ivan@mail.com");
        Customer olena = new Customer("+380671112233", "олена коваль", "olena@mail.com");
        customers.add(ivan);
        customers.add(olena);

        RestaurantTable table = new RestaurantTable(12, 4, Zone.TERRACE);
        tables.add(table);

        Reservation reservation = new Reservation(ivan, table, LocalDate.of(2026, 10, 10),
                LocalTime.of(18, 0), LocalTime.of(20, 30), 3, ReservationStatus.PENDING);
        orders.add(new RestaurantOrder("ORD-501", reservation, 1200.0, 100.0));

        printSearch(customers, "+380501234567");
        printSearch(customers, "+380000000000");
        printSearch(tables, "12");
        printSearch(tables, "99");
        printSearch(orders, "ORD-501");
        printSearch(orders, "ORD-999");

        System.out.println("\n2. Наповнення репозиторію з CSV-файла");
        tables.addAll(TableLoader.load(Path.of("data", "tables.csv")));
        System.out.println("  Столиків у репозиторії: " + tables.size());
        for (RestaurantTable loaded : tables.findAll()) {
            System.out.println("  " + loaded);
        }

        System.out.println("\n3. Додавання дубліката");
        Customer duplicate = new Customer("+380501234567", "інший клієнт", "other@mail.com");
        try {
            customers.add(duplicate);
        } catch (DuplicateIdentityException e) {
            System.out.println("  add: виключення — " + e.getMessage());
        }
        boolean added = customers.addIfAbsent(duplicate);
        System.out.println("  addIfAbsent: повернув " + added + ", виключення немає");
        System.out.println("  Клієнтів у репозиторії: " + customers.size());

        System.out.println("\n4. Видалення");
        System.out.println("  Видалення +380671112233: " + customers.removeByIdentity("+380671112233"));
        System.out.println("  Видалення +380000000000: " + customers.removeByIdentity("+380000000000"));
        System.out.println("  Клієнтів у репозиторії: " + customers.size());

        System.out.println("\n5. Рівні логування (див. лог вище)");
        System.out.println("INFO  — успішне додавання, видалення, читання файла");
        System.out.println("WARN  — відхилений дублікат, ненайдений елемент, некоректні рядки CSV");
        System.out.println("ERROR — дублікат через add (виключення летить далі)");
    }

    private static <T> void printSearch(GenericRepository<T> repository, String identity) {
        Optional<T> found = repository.findByIdentity(identity);
        if (found.isPresent()) {
            System.out.println("  Знайдено " + identity + ": " + found.get());
        } else {
            System.out.println("  Не знайдено: " + identity);
            log.warn("Item '{}' not found", identity);
        }
    }
}
