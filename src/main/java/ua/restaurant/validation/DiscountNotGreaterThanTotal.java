package ua.restaurant.validation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = DiscountNotGreaterThanTotalValidator.class)
public @interface DiscountNotGreaterThanTotal {

    String message() default "знижка не може бути більшою за суму замовлення";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
