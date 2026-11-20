package ua.restaurant.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import ua.restaurant.model.RestaurantOrder;

public class DiscountNotGreaterThanTotalValidator
        implements ConstraintValidator<DiscountNotGreaterThanTotal, RestaurantOrder> {

    @Override
    public boolean isValid(RestaurantOrder order, ConstraintValidatorContext context) {
        if (order == null || order.getDiscountAmount() <= order.getTotalAmount()) {
            return true;
        }
        context.disableDefaultConstraintViolation();
        context.buildConstraintViolationWithTemplate(context.getDefaultConstraintMessageTemplate())
                .addPropertyNode("discountAmount")
                .addConstraintViolation();
        return false;
    }
}
