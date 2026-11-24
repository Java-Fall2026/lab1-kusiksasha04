package ua.restaurant.api;

public class ApiException extends RuntimeException {

    private final int status;

    public ApiException(int status, String message) {
        super(message);
        this.status = status;
    }

    public int status() {
        return status;
    }

    public static ApiException badRequest(String message) {
        return new ApiException(400, message);
    }

    public static ApiException notFound(String message) {
        return new ApiException(404, message);
    }

    public static ApiException methodNotAllowed(String message) {
        return new ApiException(405, message);
    }

    public static ApiException unsupportedMediaType(String message) {
        return new ApiException(415, message);
    }
}
