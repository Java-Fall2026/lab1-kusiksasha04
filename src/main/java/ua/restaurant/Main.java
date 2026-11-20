package ua.restaurant;

import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Comparator;
import java.util.List;
import java.util.TreeSet;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ua.restaurant.io.TableLoader;
import ua.restaurant.model.Customer;
import ua.restaurant.model.Reservation;
import ua.restaurant.model.ReservationStatus;
import ua.restaurant.model.RestaurantOrder;
import ua.restaurant.model.RestaurantTable;
import ua.restaurant.model.Zone;
import ua.restaurant.repository.OrderRepository;
import ua.restaurant.repository.SortOrder;
import ua.restaurant.repository.TableRepository;

public class Main {

    private static final Logger log = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) {
        System.out.println("1. Наповнення доменних репозиторіїв");
        TableRepository tables = new TableRepository();
        tables.addAll(TableLoader.load(Path.of("data", "tables.csv")));
        RestaurantTable terrace = new RestaurantTable(12, 4, Zone.TERRACE);
        RestaurantTable hall = new RestaurantTable(15, 6, Zone.MAIN_HALL);
        tables.add(terrace);
        tables.add(hall);

        Customer ivan = new Customer("+380501234567", "іван петренко", "ivan@mail.com");
        Customer olena = new Customer("+380671112233", "олена коваль", "olena@mail.com");
        Customer petro = new Customer("+380931234567", "петро сидоренко", "petro@mail.com");
        RestaurantTable vip = tables.findByIdentity("7").orElseThrow();
        RestaurantTable first = tables.findByIdentity("1").orElseThrow();

        OrderRepository orders = new OrderRepository();
        orders.add(new RestaurantOrder("ORD-501", reservation(ivan, terrace, 10, 3, ReservationStatus.CONFIRMED), 1200.0, 100.0));
        orders.add(new RestaurantOrder("ORD-502", reservation(olena, vip, 9, 5, ReservationStatus.COMPLETED), 850.40, 0.0));
        orders.add(new RestaurantOrder("ORD-503", reservation(petro, first, 10, 2, ReservationStatus.COMPLETED), 850.90, 50.0));
        orders.add(new RestaurantOrder("ORD-504", reservation(ivan, hall, 11, 4, ReservationStatus.PENDING), 2300.0, 200.0));
        System.out.println("  Столиків: " + tables.size() + ", замовлень: " + orders.size());

        System.out.println("\n2. Сортування");
        System.out.println("  Столики за природним порядком (номер):");
        List<RestaurantTable> natural = tables.findAllSorted(Comparator.naturalOrder());
        log.info("Sort tables by natural order: {} items", natural.size());
        printTables(natural);
        System.out.println("  Столики за місткістю:");
        printTables(tables.sortByCapacity());
        System.out.println("  Столики за зоною, потім за місткістю за спаданням (багаторівневе):");
        printTables(tables.sortByZoneThenCapacityDesc());
        System.out.println("  Замовлення за сумою за спаданням:");
        printOrders(orders.sortByAmountDesc());
        System.out.println("  Замовлення за датою, потім за сумою:");
        printOrders(orders.sortByDateThenAmount());

        System.out.println("\n3. findAllSortedByIdentity в обидва боки");
        printOrders(orders.findAllSortedByIdentity(SortOrder.ASC));
        printOrders(orders.findAllSortedByIdentity(SortOrder.DESC));

        System.out.println("\n4. TreeSet з компаратором за місткістю");
        TreeSet<RestaurantTable> byCapacity = new TreeSet<>(Comparator.comparingInt(RestaurantTable::capacity));
        RestaurantTable a = new RestaurantTable(1, 4, Zone.MAIN_HALL);
        RestaurantTable b = new RestaurantTable(8, 4, Zone.BAR);
        System.out.println("  Додано столик 1: " + byCapacity.add(a));
        System.out.println("  Додано столик 8: " + byCapacity.add(b));
        System.out.println("  a.equals(b): " + a.equals(b) + ", розмір TreeSet: " + byCapacity.size());

        System.out.println("\n5. Пошук");
        System.out.println("  Зона містить 'hall':");
        printTables(tables.findByZoneName("hall"));
        System.out.println("  Місткість від 3 до 5:");
        printTables(tables.findByCapacityRange(3, 5));
        System.out.println("  Зона MAIN_HALL і місткість від 5:");
        printTables(tables.findByZoneAndMinCapacity(Zone.MAIN_HALL, 5));
        tables.findFirstForGuests(5).ifPresent(table -> System.out.println("  Перший столик на 5 гостей: " + table));
        System.out.println("  Замовлення клієнта 'іван':");
        printOrders(orders.findByCustomerName("іван"));
        System.out.println("  Сума від 800 до 1000:");
        printOrders(orders.findByAmountRange(800, 1000));
        System.out.println("  Статус COMPLETED і сума від 850.5:");
        printOrders(orders.findByStatusAndMinAmount(ReservationStatus.COMPLETED, 850.5));
        System.out.println("  Топ-2 клієнти за сумою: " + orders.findTopCustomerNames(2));

        System.out.println("\n6. Результат, якого немає");
        String bigTable = tables.findFirstForGuests(25)
                .map(table -> "столик " + table.tableNumber())
                .orElse("столика на 25 гостей немає");
        System.out.println("  " + bigTable);
        String missingOrder = orders.findByIdentity("ORD-999")
                .map(RestaurantOrder::getOrderId)
                .orElseGet(() -> "замовлення ORD-999 не знайдено");
        System.out.println("  " + missingOrder);
        System.out.println("  Зона містить 'garden': " + tables.findByZoneName("garden").size() + " столиків");

        System.out.println("\n7. Групування та агрегація");
        tables.groupByZone().forEach((zone, list) ->
                System.out.println("  " + zone + ": " + list.stream().map(RestaurantTable::tableNumber).toList()));
        System.out.println("  Кількість за зонами: " + tables.countByZone());
        System.out.println("  Загальна виручка: " + orders.totalRevenue());

        System.out.println("\n8. Повторне використання стріму");
        Stream<RestaurantTable> stream = tables.stream();
        System.out.println("  Перший прохід: " + stream.count() + " столиків");
        try {
            stream.count();
        } catch (IllegalStateException e) {
            System.out.println("  Другий прохід: виключення — " + e.getMessage());
            log.warn("Stream reuse failed: {}", e.getMessage());
        }

        System.out.println("\n9. Рівні логування (див. лог вище)");
        System.out.println("INFO  — сортування й пошук: критерій і кількість елементів");
        System.out.println("WARN  — порожній результат пошуку, повторне використання стріму");
        System.out.println("ERROR — виключення, що летить далі (дублікат через add); у цьому запуску не виникає");
    }

    private static Reservation reservation(Customer customer, RestaurantTable table, int day, int guests, ReservationStatus status) {
        return new Reservation(customer, table, LocalDate.of(2026, 10, day),
                LocalTime.of(18, 0), LocalTime.of(20, 0), guests, status);
    }

    private static void printTables(List<RestaurantTable> list) {
        list.forEach(table -> System.out.println("    " + table));
    }

    private static void printOrders(List<RestaurantOrder> list) {
        list.forEach(order -> System.out.println("    " + order.getOrderId()
                + " " + order.getReservation().getReservationDate()
                + " " + order.getReservation().getCustomer().name()
                + " " + order.getTotalAmount()));
    }
}
