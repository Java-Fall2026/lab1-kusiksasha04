package ua.restaurant.api;

import java.io.IOException;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Validator;

public class JsonSupport {

    private static final String JSON_CONTENT_TYPE = "application/json; charset=UTF-8";

    private final ObjectMapper mapper;
    private final Validator validator;

    public JsonSupport(ObjectMapper mapper, Validator validator) {
        this.mapper = mapper;
        this.validator = validator;
    }

    public <T> T readValid(HttpServletRequest request, Class<T> type) throws IOException {
        String contentType = request.getContentType();
        if (contentType == null || !contentType.toLowerCase(Locale.ROOT).startsWith("application/json")) {
            throw ApiException.unsupportedMediaType("Content-Type має бути application/json, отримано: " + contentType);
        }
        T item = mapper.readValue(request.getInputStream(), type);
        if (item == null) {
            throw ApiException.badRequest("Тіло запиту не може бути null");
        }
        List<ApiError.Violation> violations = validator.validate(item).stream()
                .map(v -> new ApiError.Violation(v.getPropertyPath().toString(), v.getMessage()))
                .sorted(Comparator.comparing(ApiError.Violation::field).thenComparing(ApiError.Violation::message))
                .toList();
        if (!violations.isEmpty()) {
            throw new ValidationFailedException(violations);
        }
        return item;
    }

    public <T> void write(HttpServletResponse response, int status, T body) throws IOException {
        response.setStatus(status);
        response.setContentType(JSON_CONTENT_TYPE);
        mapper.writeValue(response.getOutputStream(), body);
    }
}
