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
import ua.restaurant.model.RestaurantTable;
import ua.restaurant.repository.TableRepository;

public class TableServlet extends HttpServlet {

    private static final Set<String> PARAMS = Set.of("sort", "order", "zone", "minCapacity", "maxCapacity", "limit", "offset");

    private static final Map<String, Comparator<RestaurantTable>> SORTS = Map.of(
            "number", Comparator.comparingInt(RestaurantTable::tableNumber),
            "capacity", Comparator.comparingInt(RestaurantTable::capacity),
            "zone", Comparator.comparing(RestaurantTable::zone));

    private final TableRepository repository;
    private final JsonSupport json;

    public TableServlet(TableRepository repository, JsonSupport json) {
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
            json.write(response, 200, Map.of("countByZone", repository.countByZone()));
        } else {
            json.write(response, 200, find(id));
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        RequestPath.requireCollection(request);
        RestaurantTable table = json.readValid(request, RestaurantTable.class);
        repository.add(table);
        response.setHeader("Location", "/api/tables/" + repository.identityOf(table));
        json.write(response, 201, table);
    }

    @Override
    protected void doPut(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String id = RequestPath.requireId(request);
        RestaurantTable table = json.readValid(request, RestaurantTable.class);
        if (!id.equals(repository.identityOf(table))) {
            throw ApiException.badRequest("Номер столика в тілі (" + repository.identityOf(table) + ") не збігається зі шляхом (" + id + ")");
        }
        if (!repository.replace(table)) {
            throw ApiException.notFound("Столик " + id + " не знайдено");
        }
        json.write(response, 200, table);
    }

    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response) {
        String id = RequestPath.requireId(request);
        if (!repository.removeByIdentity(id)) {
            throw ApiException.notFound("Столик " + id + " не знайдено");
        }
        response.setStatus(204);
    }

    private RestaurantTable find(String id) {
        return repository.findByIdentity(id)
                .orElseThrow(() -> ApiException.notFound("Столик " + id + " не знайдено"));
    }

    private List<RestaurantTable> list(HttpServletRequest request) {
        QueryParams params = QueryParams.of(request, PARAMS);
        List<RestaurantTable> result = repository.findAllSorted(params.comparator(SORTS, Comparator.naturalOrder()));

        String zone = params.text("zone");
        if (zone != null) {
            Set<RestaurantTable> matches = new HashSet<>(repository.findByZoneName(zone));
            result = result.stream().filter(matches::contains).toList();
        }

        Integer minCapacity = params.integer("minCapacity", 0);
        Integer maxCapacity = params.integer("maxCapacity", 0);
        if (minCapacity != null || maxCapacity != null) {
            Set<RestaurantTable> matches = new HashSet<>(repository.findByCapacityRange(
                    minCapacity == null ? 0 : minCapacity,
                    maxCapacity == null ? Integer.MAX_VALUE : maxCapacity));
            result = result.stream().filter(matches::contains).toList();
        }

        return params.page(result);
    }
}
