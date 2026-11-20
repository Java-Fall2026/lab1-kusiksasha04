package ua.restaurant.model;

import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.PositiveOrZero;
import ua.common.BaseEntity;
import ua.restaurant.validation.DiscountNotGreaterThanTotal;

@DiscountNotGreaterThanTotal
@JsonIgnoreProperties("createdAt")
public class RestaurantOrder extends BaseEntity implements Comparable<RestaurantOrder> {

    @NotBlank(message = "номер замовлення не може бути порожнім")
    @Pattern(regexp = "ORD-\\d+", message = "номер замовлення має бути у форматі ORD-<число>")
    private final String orderId;

    @NotNull(message = "бронювання є обов'язковим")
    private final Reservation reservation;

    @PositiveOrZero(message = "сума не може бути від'ємною")
    private final double totalAmount;

    @PositiveOrZero(message = "знижка не може бути від'ємною")
    private double discountAmount;

    @JsonCreator
    public RestaurantOrder(@JsonProperty("orderId") String orderId,
                           @JsonProperty("reservation") Reservation reservation,
                           @JsonProperty("totalAmount") double totalAmount,
                           @JsonProperty("discountAmount") double discountAmount) {
        super();
        this.orderId = orderId;
        this.reservation = reservation;
        this.totalAmount = totalAmount;
        this.discountAmount = discountAmount;
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
        this.discountAmount = discountAmount;
    }

    @Override
    public int compareTo(RestaurantOrder other) {
        return orderId.compareTo(other.orderId);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        return Objects.equals(orderId, ((RestaurantOrder) o).orderId);
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
