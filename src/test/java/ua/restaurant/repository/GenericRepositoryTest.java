package ua.restaurant.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import ua.restaurant.exception.DuplicateIdentityException;
import ua.restaurant.model.Customer;
import ua.restaurant.model.RestaurantTable;
import ua.restaurant.model.Zone;

class GenericRepositoryTest {

    private GenericRepository<Customer> customers;
    private Customer ivan;

    @BeforeEach
    void setUp() {
        customers = new GenericRepository<>("customers", customer -> customer.phone());
        ivan = new Customer("+380501234567", "ivan petrenko", "ivan@mail.com");
    }

    @Test
    @DisplayName("Added item is found by its identity")
    void addThenFind() {
        customers.add(ivan);

        assertEquals(ivan, customers.findByIdentity("+380501234567").orElseThrow());
    }

    @Test
    @DisplayName("Missing identity returns an empty Optional")
    void findMissingReturnsEmpty() {
        assertTrue(customers.findByIdentity("+380000000000").isEmpty());
    }

    @Test
    @DisplayName("Adding the same identity twice throws DuplicateIdentityException")
    void addDuplicateThrows() {
        customers.add(ivan);

        assertThrows(DuplicateIdentityException.class, () -> customers.add(ivan));
    }

    @Test
    @DisplayName("addIfAbsent returns false and keeps the size for a duplicate")
    void addIfAbsentDuplicate() {
        customers.add(ivan);

        assertFalse(customers.addIfAbsent(ivan));
        assertEquals(1, customers.size());
    }

    @Test
    @DisplayName("removeByIdentity removes an existing item and returns false for a missing one")
    void removeExistingAndMissing() {
        customers.add(ivan);

        assertTrue(customers.removeByIdentity("+380501234567"));
        assertFalse(customers.removeByIdentity("+380501234567"));
        assertEquals(0, customers.size());
    }

    @Test
    @DisplayName("Changing the result of findAll does not change the repository")
    void findAllCannotChangeRepository() {
        customers.add(ivan);
        List<Customer> all = customers.findAll();

        assertThrows(UnsupportedOperationException.class, () -> all.clear());
        assertEquals(1, customers.size());
    }

    @Test
    @DisplayName("The same repository class works with two different types")
    void worksWithDifferentTypes() {
        GenericRepository<RestaurantTable> tables = new GenericRepository<>("tables", table -> String.valueOf(table.tableNumber()));
        RestaurantTable table = new RestaurantTable(12, 4, Zone.TERRACE);

        customers.add(ivan);
        tables.add(table);

        assertEquals(ivan, customers.findByIdentity("+380501234567").orElseThrow());
        assertEquals(table, tables.findByIdentity("12").orElseThrow());
    }

    @Test
    @DisplayName("Adding null throws IllegalArgumentException")
    void addNullThrows() {
        assertThrows(IllegalArgumentException.class, () -> customers.add(null));
    }
}
