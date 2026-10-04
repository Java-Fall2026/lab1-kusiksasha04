package ua.restaurant.model;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Objects;

import ua.common.BaseEntity;
import ua.restaurant.util.RestaurantUtils;

public class Reservation extends BaseEntity {

    private final Customer customer;
    private final RestaurantTable table;
    private final LocalDate reservationDate;
    private final LocalTime startTime;
    private final LocalTime endTime;
    private final int guestCount;
    private String status;

    public Reservation(Customer customer, RestaurantTable table, LocalDate reservationDate,
                       LocalTime startTime, LocalTime endTime, int guestCount, String status) {
        super();
        this.customer = RestaurantUtils.requireNotNull(customer, "Customer");
        this.table = RestaurantUtils.requireNotNull(table, "Table");
        this.reservationDate = RestaurantUtils.requireNotNull(reservationDate, "Reservation date");
        this.startTime = RestaurantUtils.requireNotNull(startTime, "Start time");
        this.endTime = RestaurantUtils.validateEndTime(startTime, endTime);
        this.guestCount = RestaurantUtils.validateGuestCount(guestCount, table.getCapacity());
        setStatus(status);
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

    public String getStatus() {
        return status;
    }

    public final void setStatus(String status) {
        this.status = RestaurantUtils.validateStatus(status);
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
        return table.equals(other.table)
                && reservationDate.equals(other.reservationDate)
                && startTime.equals(other.startTime);
    }

    @Override
    public int hashCode() {
        return Objects.hash(table, reservationDate, startTime);
    }

    @Override
    public String toString() {
        return "Reservation{customer=" + customer.getPhone() + ", table=" + table.getTableNumber()
                + ", reservationDate=" + reservationDate + ", startTime=" + startTime
                + ", endTime=" + endTime + ", guestCount=" + guestCount
                + ", status='" + status + "', createdAt=" + createdAt + "}";
    }
}
