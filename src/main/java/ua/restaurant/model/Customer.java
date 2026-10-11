package ua.restaurant.model;

import ua.restaurant.util.RestaurantUtils;

public record Customer(String phone, String name, String email) {

    public Customer {
        phone = RestaurantUtils.validatePhone(phone);
        name = RestaurantUtils.validateName(name);
        email = RestaurantUtils.validateEmail(email);
    }
}
