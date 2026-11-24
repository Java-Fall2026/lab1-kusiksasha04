package ua.restaurant.api;

import java.util.List;

public class ValidationFailedException extends ApiException {

    private final List<ApiError.Violation> violations;

    public ValidationFailedException(List<ApiError.Violation> violations) {
        super(400, "Тіло запиту не пройшло валідацію");
        this.violations = violations;
    }

    public List<ApiError.Violation> violations() {
        return violations;
    }
}
