package ua.restaurant.api;

import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import jakarta.servlet.http.HttpServletRequest;
import ua.restaurant.repository.SortOrder;

final class QueryParams {

    private final HttpServletRequest request;

    private QueryParams(HttpServletRequest request) {
        this.request = request;
    }

    static QueryParams of(HttpServletRequest request, Set<String> allowed) {
        request.getParameterMap().keySet().stream()
                .filter(name -> !allowed.contains(name))
                .findFirst()
                .ifPresent(name -> {
                    throw ApiException.badRequest("Невідомий параметр: " + name + ". Дозволені: " + allowed);
                });
        return new QueryParams(request);
    }

    String text(String name) {
        return request.getParameter(name);
    }

    Integer integer(String name, int min) {
        String value = request.getParameter(name);
        if (value == null) {
            return null;
        }
        int number;
        try {
            number = Integer.parseInt(value);
        } catch (NumberFormatException e) {
            throw ApiException.badRequest("Параметр " + name + " має бути цілим числом, отримано: " + value);
        }
        if (number < min) {
            throw ApiException.badRequest("Параметр " + name + " має бути не менше " + min + ", отримано: " + value);
        }
        return number;
    }

    Double decimal(String name) {
        String value = request.getParameter(name);
        if (value == null) {
            return null;
        }
        try {
            double number = Double.parseDouble(value);
            if (!Double.isFinite(number)) {
                throw new NumberFormatException();
            }
            return number;
        } catch (NumberFormatException e) {
            throw ApiException.badRequest("Параметр " + name + " має бути числом, отримано: " + value);
        }
    }

    <T> Comparator<T> comparator(Map<String, Comparator<T>> sorts, Comparator<T> defaultSort) {
        String sort = request.getParameter("sort");
        Comparator<T> comparator = defaultSort;
        if (sort != null) {
            comparator = sorts.get(sort);
            if (comparator == null) {
                throw ApiException.badRequest("Невідоме значення sort: " + sort + ". Дозволені: " + sorts.keySet());
            }
        }
        return sortOrder() == SortOrder.DESC ? comparator.reversed() : comparator;
    }

    <T> List<T> page(List<T> items) {
        Integer offset = integer("offset", 0);
        Integer limit = integer("limit", 0);
        return items.stream()
                .skip(offset == null ? 0 : offset)
                .limit(limit == null ? Long.MAX_VALUE : limit)
                .toList();
    }

    private SortOrder sortOrder() {
        String order = request.getParameter("order");
        if (order == null) {
            return SortOrder.ASC;
        }
        try {
            return SortOrder.valueOf(order.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            throw ApiException.badRequest("Невідоме значення order: " + order + ". Дозволені: ASC, DESC");
        }
    }
}
