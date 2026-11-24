package ua.restaurant.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.CompletableFuture;
import java.util.stream.IntStream;
import java.util.stream.StreamSupport;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import ua.restaurant.io.DataFormat;
import ua.restaurant.model.Customer;
import ua.restaurant.model.Reservation;
import ua.restaurant.model.ReservationStatus;
import ua.restaurant.model.RestaurantOrder;
import ua.restaurant.model.RestaurantTable;
import ua.restaurant.model.Zone;
import ua.restaurant.repository.OrderRepository;
import ua.restaurant.repository.TableRepository;

class ApiServerTest {

    private static final ObjectMapper MAPPER = DataFormat.JSON.mapper();
    private static final String JSON = "application/json";

    private TableRepository tables;
    private OrderRepository orders;
    private ApiServer server;
    private HttpClient client;
    private String base;

    @BeforeEach
    void startServer() throws Exception {
        tables = new TableRepository();
        tables.add(new RestaurantTable(1, 4, Zone.MAIN_HALL));
        tables.add(new RestaurantTable(2, 2, Zone.TERRACE));
        tables.add(new RestaurantTable(3, 6, Zone.VIP));

        orders = new OrderRepository();
        Customer customer = new Customer("+380501234567", "Іван Петренко", "ivan@mail.com");
        Reservation reservation = new Reservation(customer, new RestaurantTable(1, 4, Zone.MAIN_HALL),
                LocalDate.of(2026, 11, 2), LocalTime.of(18, 0), LocalTime.of(20, 0), 2, ReservationStatus.CONFIRMED);
        orders.add(new RestaurantOrder("ORD-1", reservation, 500.0, 50.0));

        server = new ApiServer(0, tables, orders);
        server.start();
        client = HttpClient.newHttpClient();
        base = "http://localhost:" + server.port();
    }

    @AfterEach
    void stopServer() throws Exception {
        server.stop();
    }

    private HttpRequest request(String method, String path, String contentType, String body) {
        HttpRequest.Builder builder = HttpRequest.newBuilder(URI.create(base + path));
        if (contentType != null) {
            builder.header("Content-Type", contentType);
        }
        HttpRequest.BodyPublisher publisher = body == null
                ? HttpRequest.BodyPublishers.noBody()
                : HttpRequest.BodyPublishers.ofString(body);
        return builder.method(method, publisher).build();
    }

    private HttpResponse<String> send(String method, String path, String contentType, String body)
            throws IOException, InterruptedException {
        return client.send(request(method, path, contentType, body), HttpResponse.BodyHandlers.ofString());
    }

    private HttpResponse<String> get(String path) throws IOException, InterruptedException {
        return send("GET", path, null, null);
    }

    private static JsonNode json(HttpResponse<String> response) throws IOException {
        return MAPPER.readTree(response.body());
    }

    private static List<Integer> tableNumbers(HttpResponse<String> response) throws IOException {
        return StreamSupport.stream(json(response).spliterator(), false)
                .map(node -> node.get("tableNumber").asInt())
                .toList();
    }

    private static String orderJson(String id, String customerName, double total, double discount) {
        return """
                {
                  "orderId": "%s",
                  "reservation": {
                    "customer": { "phone": "+380671112233", "name": "%s", "email": "olena@mail.com" },
                    "table": { "tableNumber": 2, "capacity": 2, "zone": "TERRACE" },
                    "reservationDate": "2026-11-05",
                    "startTime": "19:00",
                    "endTime": "21:00",
                    "guestCount": 2,
                    "status": "PENDING"
                  },
                  "totalAmount": %s,
                  "discountAmount": %s
                }
                """.formatted(id, customerName, total, discount);
    }

    @Test
    @DisplayName("GET collection returns a JSON array with UTF-8 content type")
    void getCollection() throws Exception {
        HttpResponse<String> response = get("/api/tables");

        String contentType = response.headers().firstValue("Content-Type").orElse("").toLowerCase(Locale.ROOT);
        assertEquals(200, response.statusCode());
        assertEquals(3, json(response).size());
        assertTrue(contentType.contains("application/json"));
        assertTrue(contentType.contains("charset=utf-8"));
    }

    @Test
    @DisplayName("GET by identity returns 200 for an existing object and 404 ApiError otherwise")
    void getByIdentity() throws Exception {
        HttpResponse<String> found = get("/api/tables/1");
        HttpResponse<String> missing = get("/api/tables/99");

        assertEquals(200, found.statusCode());
        assertEquals(1, json(found).get("tableNumber").asInt());
        assertEquals(404, missing.statusCode());
        assertEquals("Not Found", json(missing).get("error").asText());
        assertEquals(404, json(missing).get("status").asInt());
    }

    @Test
    @DisplayName("POST of a valid object returns 201 with Location pointing to the same object")
    void postValid() throws Exception {
        String body = "{\"tableNumber\":20,\"capacity\":4,\"zone\":\"BAR\"}";

        HttpResponse<String> created = send("POST", "/api/tables", JSON, body);
        String location = created.headers().firstValue("Location").orElseThrow();
        HttpResponse<String> fetched = get(location);

        assertEquals(201, created.statusCode());
        assertEquals("/api/tables/20", location);
        assertEquals(MAPPER.readTree(body), json(fetched));
    }

    @Test
    @DisplayName("POST of an invalid object returns 400 with all violations, including the custom one")
    void postInvalid() throws Exception {
        HttpResponse<String> response = send("POST", "/api/orders", JSON, orderJson("BAD", "Олена Коваль", 500.0, 900.0));

        JsonNode violations = json(response).get("violations");
        List<String> fields = StreamSupport.stream(violations.spliterator(), false)
                .map(v -> v.get("field").asText())
                .toList();
        assertEquals(400, response.statusCode());
        assertEquals(2, violations.size());
        assertTrue(fields.contains("discountAmount"));
        assertEquals(1, orders.size());
    }

    @Test
    @DisplayName("POST of a duplicate returns 409 and keeps the repository size")
    void postDuplicate() throws Exception {
        HttpResponse<String> response = send("POST", "/api/tables", JSON, "{\"tableNumber\":1,\"capacity\":8,\"zone\":\"VIP\"}");

        assertEquals(409, response.statusCode());
        assertEquals(3, tables.size());
    }

    @Test
    @DisplayName("POST of broken JSON returns 400, not 500")
    void postBrokenJson() throws Exception {
        HttpResponse<String> response = send("POST", "/api/tables", JSON, "{ \"tableNumber\": 1, ");

        assertEquals(400, response.statusCode());
        assertEquals(400, json(response).get("status").asInt());
    }

    @Test
    @DisplayName("POST with text/plain content type returns 415")
    void postTextPlain() throws Exception {
        HttpResponse<String> response = send("POST", "/api/tables", "text/plain", "{\"tableNumber\":20,\"capacity\":4,\"zone\":\"BAR\"}");

        assertEquals(415, response.statusCode());
    }

    @Test
    @DisplayName("PUT updates an existing object, returns 404 for a missing one and 400 for identity mismatch")
    void put() throws Exception {
        HttpResponse<String> updated = send("PUT", "/api/tables/1", JSON, "{\"tableNumber\":1,\"capacity\":8,\"zone\":\"VIP\"}");
        HttpResponse<String> missing = send("PUT", "/api/tables/99", JSON, "{\"tableNumber\":99,\"capacity\":8,\"zone\":\"VIP\"}");
        HttpResponse<String> mismatch = send("PUT", "/api/tables/1", JSON, "{\"tableNumber\":2,\"capacity\":8,\"zone\":\"VIP\"}");

        assertEquals(200, updated.statusCode());
        assertEquals(8, json(get("/api/tables/1")).get("capacity").asInt());
        assertEquals(404, missing.statusCode());
        assertEquals(400, mismatch.statusCode());
    }

    @Test
    @DisplayName("DELETE returns 204 and the object is gone afterwards")
    void delete() throws Exception {
        HttpResponse<String> deleted = send("DELETE", "/api/tables/2", null, null);

        assertEquals(204, deleted.statusCode());
        assertEquals(404, get("/api/tables/2").statusCode());
    }

    @Test
    @DisplayName("Unsupported method returns 405 in ApiError format")
    void unsupportedMethod() throws Exception {
        HttpResponse<String> patch = send("PATCH", "/api/tables/1", JSON, "{}");
        HttpResponse<String> deleteCollection = send("DELETE", "/api/tables", null, null);

        assertEquals(405, patch.statusCode());
        assertEquals("Method Not Allowed", json(patch).get("error").asText());
        assertEquals(405, deleteCollection.statusCode());
    }

    @Test
    @DisplayName("order=ASC and order=DESC give reversed lists, unknown sort gives 400")
    void sortOrder() throws Exception {
        List<Integer> asc = tableNumbers(get("/api/tables?sort=capacity&order=ASC"));
        List<Integer> desc = tableNumbers(get("/api/tables?sort=capacity&order=DESC"));

        assertEquals(List.of(2, 1, 3), asc);
        assertEquals(List.of(3, 1, 2), desc);
        assertEquals(400, get("/api/tables?sort=price").statusCode());
    }

    @Test
    @DisplayName("Range filter and pagination return the expected subset")
    void rangeAndPagination() throws Exception {
        assertEquals(List.of(1, 3), tableNumbers(get("/api/tables?minCapacity=3&maxCapacity=6")));
        assertEquals(List.of(2, 3), tableNumbers(get("/api/tables?sort=number&offset=1&limit=2")));
    }

    @Test
    @DisplayName("Ukrainian text survives POST and GET unchanged")
    void cyrillicRoundTrip() throws Exception {
        send("POST", "/api/orders", JSON, orderJson("ORD-77", "Олена Коваль", 300.0, 0.0));

        HttpResponse<String> response = get("/api/orders/ORD-77");

        assertEquals(200, response.statusCode());
        assertEquals("Олена Коваль", json(response).get("reservation").get("customer").get("name").asText());
    }

    @Test
    @DisplayName("Parallel POSTs with the same identity give exactly one 201 and the rest 409")
    void parallelPosts() {
        String body = "{\"tableNumber\":50,\"capacity\":4,\"zone\":\"BAR\"}";

        List<Integer> statuses = IntStream.range(0, 10)
                .mapToObj(i -> client.sendAsync(request("POST", "/api/tables", JSON, body), HttpResponse.BodyHandlers.ofString()))
                .toList()
                .stream()
                .map(CompletableFuture::join)
                .map(HttpResponse::statusCode)
                .toList();

        assertEquals(1, statuses.stream().filter(s -> s == 201).count());
        assertEquals(9, statuses.stream().filter(s -> s == 409).count());
        assertEquals(4, tables.size());
    }
}
