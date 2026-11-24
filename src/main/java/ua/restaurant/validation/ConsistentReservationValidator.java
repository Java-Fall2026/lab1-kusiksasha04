package ua.restaurant.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import ua.restaurant.model.Reservation;

public class ConsistentReservationValidator implements ConstraintValidator<ConsistentReservation, Reservation> {

    @Override
    public boolean isValid(Reservation reservation, ConstraintValidatorContext context) {
        if (reservation == null) {
            return true;
        }
        context.disableDefaultConstraintViolation();
        boolean valid = true;
        if (reservation.getStartTime() != null && reservation.getEndTime() != null
                && !reservation.getEndTime().isAfter(reservation.getStartTime())) {
            addViolation(context, "endTime", "час завершення має бути пізнішим за час початку");
            valid = false;
        }
        if (reservation.getTable() != null && reservation.getGuestCount() > reservation.getTable().capacity()) {
            addViolation(context, "guestCount", "кількість гостей не може перевищувати місткість столика");
            valid = false;
        }
        return valid;
    }

    private static void addViolation(ConstraintValidatorContext context, String field, String message) {
        context.buildConstraintViolationWithTemplate(message)
                .addPropertyNode(field)
                .addConstraintViolation();
    }
}
