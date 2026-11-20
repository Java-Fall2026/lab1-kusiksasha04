package ua.restaurant;

import java.io.IOException;
import java.nio.file.Path;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.validation.Validation;
import jakarta.validation.ValidatorFactory;
import ua.restaurant.io.AppConfig;
import ua.restaurant.io.DataLoader;
import ua.restaurant.io.FileStorage;
import ua.restaurant.io.LoadResult;
import ua.restaurant.model.RestaurantOrder;
import ua.restaurant.model.RestaurantTable;
import ua.restaurant.model.Zone;
import ua.restaurant.repository.OrderRepository;
import ua.restaurant.repository.TableRepository;

public class Main {

    private static final Logger log = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) throws IOException {
        System.out.println("1. Читання конфігурації");
        AppConfig config = AppConfig.load("app.properties");
        Path tablesFile = config.path("data.tables");
        Path ordersFile = config.path("data.orders");
        Path brokenFile = config.path("data.broken");
        Path outputFile = config.path("data.output");
        System.out.println("  Столики: " + tablesFile);
        System.out.println("  Замовлення: " + ordersFile);
        System.out.println("  Битий файл: " + brokenFile);
        System.out.println("  Файл для збереження: " + outputFile);

        try (ValidatorFactory factory = Validation.buildDefaultValidatorFactory()) {
            DataLoader loader = new DataLoader(factory.getValidator());
            TableRepository tables = new TableRepository();
            OrderRepository orders = new OrderRepository();

            System.out.println("\n2. Завантаження файлів");
            LoadResult tablesResult = loader.load(tablesFile, RestaurantTable.class, tables);
            LoadResult ordersResult = loader.load(ordersFile, RestaurantOrder.class, orders);
            printSummary(tablesFile, tablesResult);
            printSummary(ordersFile, ordersResult);

            System.out.println("\n3. Відкинуті записи");
            printRejected(tablesFile, tablesResult);
            printRejected(ordersFile, ordersResult);

            System.out.println("\n4. Спроба завантажити битий файл");
            LoadResult brokenResult = loader.load(brokenFile, RestaurantTable.class, new TableRepository());
            printSummary(brokenFile, brokenResult);
            printRejected(brokenFile, brokenResult);
            System.out.println("  Програма продовжує роботу");

            System.out.println("\n5. Зміна репозиторію та збереження в новий файл");
            tables.removeByIdentity("1");
            tables.add(new RestaurantTable(15, 6, Zone.VIP));
            new FileStorage<>(RestaurantTable.class).save(outputFile, tables.findAll());
            System.out.println("  Видалено столик 1, додано столик 15, збережено " + tables.size() + " столиків у " + outputFile);
            log.info("Saved {} tables to {}", tables.size(), outputFile);

            System.out.println("\n6. Повторне завантаження збереженого файлу");
            TableRepository reloaded = new TableRepository();
            printSummary(outputFile, loader.load(outputFile, RestaurantTable.class, reloaded));
            System.out.println("  Вміст збігається: " + reloaded.findAll().equals(tables.findAll()));
        }

        System.out.println("\n7. Рівні логування (див. лог вище)");
        System.out.println("INFO  — підсумок по кожному файлу, збереження");
        System.out.println("WARN  — кожен відкинутий запис");
        System.out.println("ERROR — файл, який не вдалося прочитати");
    }

    private static void printSummary(Path file, LoadResult result) {
        System.out.println("  " + file + ": прочитано " + result.read() + ", додано " + result.added()
                + ", відкинуто " + result.rejected().size());
    }

    private static void printRejected(Path file, LoadResult result) {
        result.rejected().forEach(r -> System.out.println("  " + file + " запис " + r.record()
                + ", поле " + r.field() + ": " + r.message()));
    }
}
