package ua.restaurant.model;

import java.util.Locale;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import ua.restaurant.util.RestaurantUtils;

public record Customer(
        @NotBlank(message = "телефон не може бути порожнім")
        String phone,
        @NotBlank(message = "ім'я не може бути порожнім")
        String name,
        @NotBlank(message = "email не може бути порожнім")
        @Email(message = "email має бути коректною адресою")
        String email) {

    public Customer {
        phone = phone == null ? null : phone.trim();
        name = name == null || name.isBlank() ? name : RestaurantUtils.formatName(name);
        email = email == null ? null : email.trim().toLowerCase(Locale.ROOT);
    }
}
