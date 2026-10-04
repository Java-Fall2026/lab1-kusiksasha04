package ua.restaurant.model;

import java.util.Objects;

import ua.common.BaseEntity;
import ua.restaurant.util.RestaurantUtils;

public class Customer extends BaseEntity {

    private final String phone;
    private final String name;
    private final String email;

    private Customer(String phone, String name, String email) {
        super();
        this.phone = RestaurantUtils.validatePhone(phone);
        this.name = RestaurantUtils.validateName(name);
        this.email = RestaurantUtils.validateEmail(email);
    }

    public static Customer of(String phone, String name, String email) {
        return new Customer(phone, name, email);
    }

    public String getPhone() {
        return phone;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        return phone.equals(((Customer) o).phone);
    }

    @Override
    public int hashCode() {
        return Objects.hash(phone);
    }

    @Override
    public String toString() {
        return "Customer{phone='" + phone + "', name='" + name + "', email='" + email
                + "', createdAt=" + createdAt + "}";
    }
}
