package ua.restaurant.repository;

import java.util.Comparator;
import java.util.List;
import java.util.Locale;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ua.restaurant.model.ReservationStatus;
import ua.restaurant.model.RestaurantOrder;
import ua.restaurant.util.RestaurantUtils;

public class OrderRepository extends GenericRepository<RestaurantOrder> {

    private static final Logger log = LoggerFactory.getLogger(OrderRepository.class);

    public OrderRepository() {
        super("orders", RestaurantOrder::getOrderId);
    }

    public List<RestaurantOrder> sortByAmountDesc() {
        Comparator<RestaurantOrder> comparator = Comparator.comparingDouble(RestaurantOrder::getTotalAmount).reversed();
        return logResult("Sort by amount desc", findAllSorted(comparator));
    }

    public List<RestaurantOrder> sortByDateThenAmount() {
        Comparator<RestaurantOrder> comparator = Comparator
                .comparing((RestaurantOrder order) -> order.getReservation().getReservationDate())
                .thenComparingDouble(RestaurantOrder::getTotalAmount);
        return logResult("Sort by date, then amount", findAllSorted(comparator));
    }

    public List<RestaurantOrder> findByCustomerName(String part) {
        String lower = part.toLowerCase(Locale.ROOT);
        List<RestaurantOrder> result = stream()
                .filter(order -> order.getReservation().getCustomer().name().toLowerCase(Locale.ROOT).contains(lower))
                .sorted()
                .toList();
        return logResult("Search by customer name '" + part + "'", result);
    }

    public List<RestaurantOrder> findByAmountRange(double min, double max) {
        List<RestaurantOrder> result = stream()
                .filter(order -> order.getTotalAmount() >= min && order.getTotalAmount() <= max)
                .sorted(Comparator.comparingDouble(RestaurantOrder::getTotalAmount))
                .toList();
        return logResult("Search by amount " + min + ".." + max, result);
    }

    public List<RestaurantOrder> findByStatusAndMinAmount(ReservationStatus status, double minAmount) {
        List<RestaurantOrder> result = stream()
                .filter(order -> order.getReservation().getStatus() == status)
                .filter(order -> order.getTotalAmount() >= minAmount)
                .sorted()
                .toList();
        return logResult("Search by status " + status + " and amount >= " + minAmount, result);
    }

    public List<String> findTopCustomerNames(int limit) {
        List<String> result = stream()
                .sorted(Comparator.comparingDouble(RestaurantOrder::getTotalAmount).reversed())
                .map(order -> order.getReservation().getCustomer().name())
                .distinct()
                .limit(limit)
                .toList();
        return logResult("Search top " + limit + " customers by amount", result);
    }

    public double totalRevenue() {
        double result = stream()
                .mapToDouble(RestaurantUtils::finalAmount)
                .sum();
        log.info("Total revenue of {} orders: {}", size(), result);
        return result;
    }
}
