package ua.restaurant.api;

import java.io.IOException;
import java.util.List;

import org.eclipse.jetty.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.exc.UnrecognizedPropertyException;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import ua.restaurant.exception.DuplicateIdentityException;

public class ErrorHandlingFilter implements Filter {

    private static final Logger log = LoggerFactory.getLogger(ErrorHandlingFilter.class);

    private final JsonSupport json;

    public ErrorHandlingFilter(JsonSupport json) {
        this.json = json;
    }

    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) req;
        HttpServletResponse response = (HttpServletResponse) res;
        try {
            chain.doFilter(request, response);
        } catch (Exception e) {
            handle(unwrap(e), request, response);
        }
    }

    private void handle(Throwable e, HttpServletRequest request, HttpServletResponse response) throws IOException {
        int status;
        String message;
        List<ApiError.Violation> violations = List.of();
        if (e instanceof ValidationFailedException validation) {
            status = validation.status();
            message = validation.getMessage();
            violations = validation.violations();
        } else if (e instanceof ApiException api) {
            status = api.status();
            message = api.getMessage();
        } else if (e instanceof DuplicateIdentityException) {
            status = 409;
            message = "Об'єкт з такою identity вже існує";
        } else if (e instanceof UnrecognizedPropertyException unknown) {
            status = 400;
            message = "Невідоме поле в тілі запиту: " + unknown.getPropertyName();
        } else if (e instanceof JsonProcessingException) {
            status = 400;
            message = "Тіло запиту не є коректним JSON потрібного формату";
        } else {
            status = 500;
            message = "Внутрішня помилка сервера";
            log.error("Unexpected error on {} {}", request.getMethod(), request.getRequestURI(), e);
        }
        if (response.isCommitted()) {
            return;
        }
        response.reset();
        ApiError error = new ApiError(status, HttpStatus.getMessage(status), message, request.getRequestURI(), violations);
        json.write(response, status, error);
    }

    private static Throwable unwrap(Throwable e) {
        Throwable current = e;
        while (current instanceof ServletException && current.getCause() != null) {
            current = current.getCause();
        }
        return current;
    }
}
