package ua.restaurant.validation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = ConsistentReservationValidator.class)
public @interface ConsistentReservation {

    String message() default "бронювання містить суперечливі дані";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
