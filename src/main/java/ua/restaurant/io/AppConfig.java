package ua.restaurant.io;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.Properties;

public class AppConfig {

    private final Properties properties;

    public AppConfig(Properties properties) {
        this.properties = properties;
    }

    public static AppConfig load(String resource) throws IOException {
        Properties properties = new Properties();
        try (InputStream in = AppConfig.class.getClassLoader().getResourceAsStream(resource)) {
            if (in == null) {
                throw new IOException("Configuration resource not found: " + resource);
            }
            properties.load(in);
        }
        return new AppConfig(properties);
    }

    public Path path(String key) {
        String value = properties.getProperty(key);
        if (value == null || value.isBlank()) {
            throw new IllegalStateException("Missing configuration key: " + key);
        }
        return Path.of(value);
    }
}
