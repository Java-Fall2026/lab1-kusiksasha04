package ua.restaurant;

import java.nio.file.Path;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.validation.Validation;
import jakarta.validation.ValidatorFactory;
import ua.restaurant.api.ApiServer;
import ua.restaurant.io.AppConfig;
import ua.restaurant.io.DataLoader;
import ua.restaurant.io.FileStorage;
import ua.restaurant.io.LoadResult;
import ua.restaurant.model.RestaurantOrder;
import ua.restaurant.model.RestaurantTable;
import ua.restaurant.repository.OrderRepository;
import ua.restaurant.repository.TableRepository;

public class Main {

    private static final Logger log = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) throws Exception {
        System.out.println("1. Читання конфігурації");
        AppConfig config = AppConfig.load("app.properties");
        int port = config.integer("server.port");
        Path tablesFile = config.path("data.tables");
        Path ordersFile = config.path("data.orders");
        Path tablesOutput = config.path("output.tables");
        Path ordersOutput = config.path("output.orders");
        System.out.println("  Порт: " + port);
        System.out.println("  Дані: " + tablesFile + ", " + ordersFile);
        System.out.println("  Збереження: " + tablesOutput + ", " + ordersOutput);

        System.out.println("\n2. Завантаження файлів");
        TableRepository tables = new TableRepository();
        OrderRepository orders = new OrderRepository();
        try (ValidatorFactory factory = Validation.buildDefaultValidatorFactory()) {
            DataLoader loader = new DataLoader(factory.getValidator());
            printSummary(tablesFile, loader.load(tablesFile, RestaurantTable.class, tables));
            printSummary(ordersFile, loader.load(ordersFile, RestaurantOrder.class, orders));
        }

        System.out.println("\n3. Запуск сервера");
        ApiServer server = new ApiServer(port, tables, orders);
        server.start();
        String address = "http://localhost:" + server.port();
        System.out.println("  Сервер працює: " + address + "/api/tables і " + address + "/api/orders");
        System.out.println("  Для зупинки натисніть Ctrl+C");
        log.info("Server started at {}", address);

        Runtime.getRuntime().addShutdownHook(new Thread(() -> shutdown(server, tables, orders, tablesOutput, ordersOutput)));
    }

    private static void shutdown(ApiServer server, TableRepository tables, OrderRepository orders,
                                 Path tablesOutput, Path ordersOutput) {
        System.out.println("\n4. Зупинка сервера та збереження даних");
        try {
            server.stop();
            new FileStorage<>(RestaurantTable.class).save(tablesOutput, tables.findAll());
            new FileStorage<>(RestaurantOrder.class).save(ordersOutput, orders.findAll());
            System.out.println("  Збережено столиків: " + tables.size() + " у " + tablesOutput);
            System.out.println("  Збережено замовлень: " + orders.size() + " у " + ordersOutput);
            log.info("Server stopped, saved {} tables and {} orders", tables.size(), orders.size());
        } catch (Exception e) {
            log.error("Failed to stop server or save data", e);
        }
    }

    private static void printSummary(Path file, LoadResult result) {
        System.out.println("  " + file + ": прочитано " + result.read() + ", додано " + result.added()
                + ", відкинуто " + result.rejected().size());
    }
}
