package ua.restaurant.model;

import java.util.Objects;

import ua.common.BaseEntity;
import ua.restaurant.util.RestaurantUtils;

public class RestaurantOrder extends BaseEntity {

    private final String orderId;
    private final Reservation reservation;
    private final double totalAmount;
    private double discountAmount;

    public RestaurantOrder(String orderId, Reservation reservation, double totalAmount, double discountAmount) {
        super();
        this.orderId = RestaurantUtils.validateOrderId(orderId);
        this.reservation = RestaurantUtils.requireNotNull(reservation, "Reservation");
        this.totalAmount = RestaurantUtils.validateTotalAmount(totalAmount);
        setDiscountAmount(discountAmount);
    }

    public String getOrderId() {
        return orderId;
    }

    public Reservation getReservation() {
        return reservation;
    }

    public double getTotalAmount() {
        return totalAmount;
    }

    public double getDiscountAmount() {
        return discountAmount;
    }

    public final void setDiscountAmount(double discountAmount) {
        this.discountAmount = RestaurantUtils.validateDiscount(discountAmount, totalAmount);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        return orderId.equals(((RestaurantOrder) o).orderId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(orderId);
    }

    @Override
    public String toString() {
        return "RestaurantOrder{orderId='" + orderId + "', reservation=" + reservation
                + ", totalAmount=" + totalAmount + ", discountAmount=" + discountAmount
                + ", createdAt=" + createdAt + "}";
    }
}
