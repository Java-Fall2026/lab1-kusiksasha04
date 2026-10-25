package ua.restaurant.io;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.AccessDeniedException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ua.restaurant.exception.InvalidDataException;
import ua.restaurant.model.RestaurantTable;
import ua.restaurant.model.Zone;

public final class TableLoader {

    private static final Logger log = LoggerFactory.getLogger(TableLoader.class);
    private static final int COLUMNS = 3;

    private TableLoader() {
    }

    public static List<RestaurantTable> load(Path path) {
        List<RestaurantTable> tables = new ArrayList<>();
        int skipped = 0;
        log.info("Loading tables from {}", path);
        try (BufferedReader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
            reader.readLine(); // header
            int lineNumber = 1;
            String line;
            while ((line = reader.readLine()) != null) {
                lineNumber++;
                try {
                    tables.add(parseLine(line, lineNumber));
                } catch (InvalidDataException e) {
                    skipped++;
                    log.warn("Skipping line: {}", e.getMessage(), e);
                }
            }
        } catch (NoSuchFileException | AccessDeniedException e) {
            log.error("File is missing or not accessible: {}", path, e);
        } catch (IOException e) {
            log.error("Failed to read file {}", path, e);
        } finally {
            log.info("Finished reading {}: {} loaded, {} skipped", path, tables.size(), skipped);
        }
        return tables;
    }

    static RestaurantTable parseLine(String line, int lineNumber) throws InvalidDataException {
        String[] parts = line.split(",", -1);
        if (parts.length != COLUMNS) {
            throw new InvalidDataException("Line " + lineNumber + ": expected " + COLUMNS + " columns but found " + parts.length + ": '" + line + "'");
        }
        try {
            int tableNumber = Integer.parseInt(parts[0].trim());
            int capacity = Integer.parseInt(parts[1].trim());
            Zone zone = Zone.valueOf(parts[2].trim());
            return new RestaurantTable(tableNumber, capacity, zone);
        } catch (IllegalArgumentException e) {
            throw new InvalidDataException("Line " + lineNumber + ": invalid value in '" + line + "' (" + e.getMessage() + ")", e);
        }
    }
}
