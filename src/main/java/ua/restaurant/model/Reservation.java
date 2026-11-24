package ua.restaurant.model;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import ua.common.BaseEntity;
import ua.restaurant.validation.ConsistentReservation;

@ConsistentReservation
@JsonIgnoreProperties("createdAt")
public class Reservation extends BaseEntity {

    @NotNull(message = "клієнт є обов'язковим")
    @Valid
    private final Customer customer;

    @NotNull(message = "столик є обов'язковим")
    @Valid
    private final RestaurantTable table;

    @NotNull(message = "дата бронювання є обов'язковою")
    private final LocalDate reservationDate;

    @NotNull(message = "час початку є обов'язковим")
    private final LocalTime startTime;

    @NotNull(message = "час завершення є обов'язковим")
    private final LocalTime endTime;

    @Positive(message = "кількість гостей має бути додатною")
    private final int guestCount;

    @NotNull(message = "статус є обов'язковим")
    private ReservationStatus status;

    @JsonCreator
    public Reservation(@JsonProperty("customer") Customer customer,
                       @JsonProperty("table") RestaurantTable table,
                       @JsonProperty("reservationDate") LocalDate reservationDate,
                       @JsonProperty("startTime") LocalTime startTime,
                       @JsonProperty("endTime") LocalTime endTime,
                       @JsonProperty("guestCount") int guestCount,
                       @JsonProperty("status") ReservationStatus status) {
        super();
        this.customer = customer;
        this.table = table;
        this.reservationDate = reservationDate;
        this.startTime = startTime;
        this.endTime = endTime;
        this.guestCount = guestCount;
        this.status = status;
    }

    public Customer getCustomer() {
        return customer;
    }

    public RestaurantTable getTable() {
        return table;
    }

    public LocalDate getReservationDate() {
        return reservationDate;
    }

    public LocalTime getStartTime() {
        return startTime;
    }

    public LocalTime getEndTime() {
        return endTime;
    }

    public int getGuestCount() {
        return guestCount;
    }

    public ReservationStatus getStatus() {
        return status;
    }

    public final void setStatus(ReservationStatus status) {
        this.status = status;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Reservation other = (Reservation) o;
        return Objects.equals(table, other.table)
                && Objects.equals(reservationDate, other.reservationDate)
                && Objects.equals(startTime, other.startTime);
    }

    @Override
    public int hashCode() {
        return Objects.hash(table, reservationDate, startTime);
    }

    @Override
    public String toString() {
        return "Reservation{customer=" + (customer == null ? null : customer.phone())
                + ", table=" + (table == null ? null : table.tableNumber())
                + ", reservationDate=" + reservationDate + ", startTime=" + startTime
                + ", endTime=" + endTime + ", guestCount=" + guestCount
                + ", status=" + status + ", createdAt=" + createdAt + "}";
    }
}
