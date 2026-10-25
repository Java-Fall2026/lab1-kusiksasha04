package ua.restaurant.io;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import ua.restaurant.exception.InvalidDataException;
import ua.restaurant.model.RestaurantTable;
import ua.restaurant.model.Zone;

class TableLoaderTest {

    @Test
    @DisplayName("Valid line is parsed into a RestaurantTable")
    void parsesValidLine() throws InvalidDataException {
        RestaurantTable table = TableLoader.parseLine("12,4,TERRACE", 2);

        assertEquals(new RestaurantTable(12, 4, Zone.TERRACE), table);
    }

    @Test
    @DisplayName("Wrong column count throws InvalidDataException with the line number")
    void rejectsWrongColumnCount() {
        InvalidDataException e = assertThrows(InvalidDataException.class, () -> TableLoader.parseLine("4,4", 5));

        assertTrue(e.getMessage().contains("Line 5"));
    }

    @Test
    @DisplayName("Non-numeric capacity throws InvalidDataException with NumberFormatException as cause")
    void rejectsNonNumericValue() {
        InvalidDataException e = assertThrows(InvalidDataException.class, () -> TableLoader.parseLine("3,six,VIP", 4));

        assertTrue(e.getCause() instanceof NumberFormatException);
    }

    @Test
    @DisplayName("Unknown zone throws InvalidDataException")
    void rejectsUnknownZone() {
        assertThrows(InvalidDataException.class, () -> TableLoader.parseLine("5,4,GARDEN", 6));
    }

    @Test
    @DisplayName("Domain rule violation in the record constructor becomes InvalidDataException")
    void rejectsCapacityOutOfRange() {
        InvalidDataException e = assertThrows(InvalidDataException.class, () -> TableLoader.parseLine("6,25,BAR", 7));

        assertTrue(e.getCause() instanceof IllegalArgumentException);
    }

    @Test
    @DisplayName("Loader skips invalid lines and keeps reading")
    void loadSkipsInvalidLines(@TempDir Path dir) throws IOException {
        Path file = dir.resolve("tables.csv");
        Files.writeString(file, String.join("\n",
                "tableNumber,capacity,zone",
                "1,4,MAIN_HALL",
                "2,two,VIP",
                "3,2,TERRACE",
                "4,4"));

        List<RestaurantTable> tables = TableLoader.load(file);

        assertEquals(2, tables.size());
    }

    @Test
    @DisplayName("Missing file returns an empty list instead of throwing")
    void loadMissingFileReturnsEmptyList(@TempDir Path dir) {
        List<RestaurantTable> tables = TableLoader.load(dir.resolve("missing.csv"));

        assertTrue(tables.isEmpty());
    }
}
