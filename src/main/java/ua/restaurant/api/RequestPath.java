package ua.restaurant.api;

import java.util.Set;

import jakarta.servlet.http.HttpServletRequest;

final class RequestPath {

    private static final Set<String> SUPPORTED_METHODS = Set.of("GET", "POST", "PUT", "DELETE");

    private RequestPath() {
    }

    static void requireSupportedMethod(HttpServletRequest request) {
        if (!SUPPORTED_METHODS.contains(request.getMethod())) {
            throw notAllowed(request);
        }
    }

    static String id(HttpServletRequest request) {
        String pathInfo = request.getPathInfo();
        if (pathInfo == null || pathInfo.equals("/")) {
            return null;
        }
        String id = pathInfo.substring(1);
        if (id.contains("/")) {
            throw ApiException.notFound("Шлях не знайдено: " + request.getRequestURI());
        }
        return id;
    }

    static String requireId(HttpServletRequest request) {
        String id = id(request);
        if (id == null) {
            throw notAllowed(request);
        }
        return id;
    }

    static void requireCollection(HttpServletRequest request) {
        if (id(request) != null) {
            throw notAllowed(request);
        }
    }

    private static ApiException notAllowed(HttpServletRequest request) {
        return ApiException.methodNotAllowed("Метод " + request.getMethod() + " не підтримується для " + request.getRequestURI());
    }
}
