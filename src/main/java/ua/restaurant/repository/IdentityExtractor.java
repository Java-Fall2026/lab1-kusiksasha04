package ua.restaurant.repository;

@FunctionalInterface
public interface IdentityExtractor<T> {
    String identityOf(T item);
}
