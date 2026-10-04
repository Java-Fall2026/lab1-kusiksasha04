package ua.restaurant;

import java.time.LocalDate;
import java.time.LocalTime;

import ua.restaurant.model.Customer;
import ua.restaurant.model.Reservation;
import ua.restaurant.model.RestaurantOrder;
import ua.restaurant.model.RestaurantTable;
import ua.restaurant.util.RestaurantUtils;

public class Main {

    public static void main(String[] args) {
        LocalDate date = LocalDate.of(2026, 10, 10);

        System.out.println("1-2. Створення та нормалізація");
        Customer customer = Customer.of(" +380501234567 ", "  олександр петренко ", "Oleksandr@MAIL.com");
        RestaurantTable table = RestaurantTable.of(12, 4, " terrace ");
        Reservation reservation = new Reservation(customer, table, date,
                LocalTime.of(18, 0), LocalTime.of(20, 30), 3, "pending");
        RestaurantOrder order = new RestaurantOrder("ord-501", reservation, 1500.0, 100.0);
        System.out.println("Ім'я: " + customer.getName() + ", email: " + customer.getEmail()
                + ", зона: " + table.getZone() + ", статус: " + reservation.getStatus()
                + ", id замовлення: " + order.getOrderId());
        System.out.println("createdAt клієнта: " + customer.getCreatedAt());

        System.out.println("\n3. Невалідні дані в конструкторах");
        try {
            RestaurantTable.of(250, 4, "BAR");
        } catch (IllegalArgumentException e) {
            System.out.println(e.getMessage());
        }
        try {
            Customer.of("+380931234567", "Марія", "maria-mail.com");
        } catch (IllegalArgumentException e) {
            System.out.println(e.getMessage());
        }
        try {
            new Reservation(customer, table, date, LocalTime.of(18, 0), LocalTime.of(20, 0), 12, "PENDING");
        } catch (IllegalArgumentException e) {
            System.out.println(e.getMessage());
        }

        System.out.println("\n4. Псуємо об'єкти сеттерами");
        try {
            reservation.setStatus("DONE");
        } catch (IllegalArgumentException e) {
            System.out.println(e.getMessage());
        }
        try {
            order.setDiscountAmount(5000.0);
        } catch (IllegalArgumentException e) {
            System.out.println(e.getMessage());
        }
        System.out.println("Значення не змінились: " + reservation.getStatus() + ", " + order.getDiscountAmount());

        System.out.println("\n5. ==, equals, hashCode");
        Customer same1 = Customer.of("+380671112233", "Іван", "ivan@mail.com");
        Customer same2 = Customer.of("+380671112233", "Іван", "ivan@mail.com");
        Customer other = Customer.of("+380999999999", "Олена", "olena@mail.com");
        System.out.println("== : " + (same1 == same2) + ", equals: " + same1.equals(same2)
                + ", hashCode рівні: " + (same1.hashCode() == same2.hashCode()));
        System.out.println("Різні клієнти, equals: " + same1.equals(other));

        System.out.println("\n6. Обчислювані методи");
        System.out.println("Тривалість: " + RestaurantUtils.durationMinutes(reservation) + " хв");
        System.out.println("До сплати: " + RestaurantUtils.formatMoney(RestaurantUtils.finalAmount(order)));

        System.out.println("\n7. toString()");
        System.out.println(customer);
        System.out.println(table);
        System.out.println(reservation);
        System.out.println(order);

        // 8. These lines do not compile:
        // new ValidationHelper();
        //   ValidationHelper is package-private in ua.restaurant.util, Main is in another package.
        // new RestaurantTable(1, 2, "BAR");
        //   The constructor is private, only RestaurantTable.of(...) is allowed.
        // System.out.println(customer.createdAt);
        //   createdAt is protected; Main is not a subclass of BaseEntity. Use getCreatedAt().
    }
}
