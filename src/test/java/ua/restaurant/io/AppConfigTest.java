package ua.restaurant.io;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Properties;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class AppConfigTest {

    @Test
    @DisplayName("Missing key throws an exception with the key name")
    void missingKeyThrows() {
        AppConfig config = new AppConfig(new Properties());

        IllegalStateException e = assertThrows(IllegalStateException.class, () -> config.path("data.tables"));

        assertTrue(e.getMessage().contains("data.tables"));
    }
}
