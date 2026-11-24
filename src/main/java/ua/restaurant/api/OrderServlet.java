package ua.restaurant.api;

import java.io.IOException;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import ua.restaurant.model.RestaurantOrder;
import ua.restaurant.repository.OrderRepository;

public class OrderServlet extends HttpServlet {

    private static final Set<String> PARAMS = Set.of("sort", "order", "customer", "minAmount", "maxAmount", "limit", "offset");

    private static final Map<String, Comparator<RestaurantOrder>> SORTS = Map.of(
            "id", Comparator.comparing(RestaurantOrder::getOrderId),
            "amount", Comparator.comparingDouble(RestaurantOrder::getTotalAmount),
            "date", Comparator.comparing(order -> order.getReservation().getReservationDate()));

    private final OrderRepository repository;
    private final JsonSupport json;

    public OrderServlet(OrderRepository repository, JsonSupport json) {
        this.repository = repository;
        this.json = json;
    }

    @Override
    protected void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        RequestPath.requireSupportedMethod(request);
        super.service(request, response);
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String id = RequestPath.id(request);
        if (id == null) {
            json.write(response, 200, list(request));
        } else if (id.equals("stats")) {
            json.write(response, 200, Map.of("totalRevenue", repository.totalRevenue()));
        } else {
            json.write(response, 200, find(id));
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        RequestPath.requireCollection(request);
        RestaurantOrder order = json.readValid(request, RestaurantOrder.class);
        repository.add(order);
        response.setHeader("Location", "/api/orders/" + repository.identityOf(order));
        json.write(response, 201, order);
    }

    @Override
    protected void doPut(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String id = RequestPath.requireId(request);
        RestaurantOrder order = json.readValid(request, RestaurantOrder.class);
        if (!id.equals(repository.identityOf(order))) {
            throw ApiException.badRequest("Номер замовлення в тілі (" + repository.identityOf(order) + ") не збігається зі шляхом (" + id + ")");
        }
        if (!repository.replace(order)) {
            throw ApiException.notFound("Замовлення " + id + " не знайдено");
        }
        json.write(response, 200, order);
    }

    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response) {
        String id = RequestPath.requireId(request);
        if (!repository.removeByIdentity(id)) {
            throw ApiException.notFound("Замовлення " + id + " не знайдено");
        }
        response.setStatus(204);
    }

    private RestaurantOrder find(String id) {
        return repository.findByIdentity(id)
                .orElseThrow(() -> ApiException.notFound("Замовлення " + id + " не знайдено"));
    }

    private List<RestaurantOrder> list(HttpServletRequest request) {
        QueryParams params = QueryParams.of(request, PARAMS);
        List<RestaurantOrder> result = repository.findAllSorted(params.comparator(SORTS, Comparator.naturalOrder()));

        String customer = params.text("customer");
        if (customer != null) {
            Set<RestaurantOrder> matches = new HashSet<>(repository.findByCustomerName(customer));
            result = result.stream().filter(matches::contains).toList();
        }

        Double minAmount = params.decimal("minAmount");
        Double maxAmount = params.decimal("maxAmount");
        if (minAmount != null || maxAmount != null) {
            Set<RestaurantOrder> matches = new HashSet<>(repository.findByAmountRange(
                    minAmount == null ? 0 : minAmount,
                    maxAmount == null ? Double.MAX_VALUE : maxAmount));
            result = result.stream().filter(matches::contains).toList();
        }

        return params.page(result);
    }
}
