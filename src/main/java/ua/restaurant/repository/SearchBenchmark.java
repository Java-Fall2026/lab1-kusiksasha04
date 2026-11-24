package ua.restaurant.repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.function.Supplier;

import ua.restaurant.model.Customer;

public class SearchBenchmark {

    private static final int SIZE = 100_000;
    private static final int LOOKUPS = 1_000;
    private static final int RUNS = 7;

    private static long found;

    public static void main(String[] args) {
        IdentityExtractor<Customer> byPhone = customer -> customer.phone();
        GenericRepository<Customer> repository = new GenericRepository<>("customers", byPhone);

        List<Customer> customers = new ArrayList<>();
        for (int i = 0; i < SIZE; i++) {
            customers.add(new Customer("+380" + (500_000_000 + i), "customer " + i, "customer" + i + "@mail.com"));
        }
        repository.addAll(customers);

        List<Customer> list = repository.findAll();
        String first = list.get(0).phone();
        String last = list.get(SIZE - 1).phone();
        String missing = "+380000000000";

        for (int i = 0; i < 3; i++) {
            runAll(repository, list, byPhone, first, last, missing, false);
        }
        runAll(repository, list, byPhone, first, last, missing, true);
    }

    private static void runAll(GenericRepository<Customer> repository, List<Customer> list,
                               IdentityExtractor<Customer> extractor,
                               String first, String last, String missing, boolean print) {
        String[] names = {"Перший", "Останній", "Відсутній"};
        String[] identities = {first, last, missing};
        if (print) {
            System.out.println("| Сценарій | Map, нс | List, нс |");
            System.out.println("|---|---|---|");
        }
        for (int i = 0; i < identities.length; i++) {
            String identity = identities[i];
            double map = measure(() -> repository.findByIdentity(identity));
            double linear = measure(() -> linearSearch(list, identity, extractor));
            if (print) {
                System.out.println(String.format(Locale.US, "| %s | %.1f | %.1f |", names[i], map, linear));
            }
        }
    }

    private static double measure(Supplier<Optional<Customer>> search) {
        long best = Long.MAX_VALUE;
        for (int run = 0; run < RUNS; run++) {
            long start = System.nanoTime();
            for (int i = 0; i < LOOKUPS; i++) {
                if (search.get().isPresent()) {
                    found++;
                }
            }
            best = Math.min(best, System.nanoTime() - start);
        }
        return (double) best / LOOKUPS;
    }

    private static <E> Optional<E> linearSearch(
            List<E> items, String identity, IdentityExtractor<E> extractor) {
        for (E item : items) {
            if (identity.equals(extractor.identityOf(item))) {
                return Optional.of(item);
            }
        }
        return Optional.empty();
    }
}
