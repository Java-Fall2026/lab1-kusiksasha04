package ua.restaurant.io;

import java.nio.file.Path;
import java.util.Locale;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

public enum DataFormat {
    JSON(new ObjectMapper()),
    YAML(new YAMLMapper());

    private final ObjectMapper mapper;

    DataFormat(ObjectMapper mapper) {
        this.mapper = mapper.registerModule(new JavaTimeModule())
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
                .enable(SerializationFeature.INDENT_OUTPUT);
    }

    public static DataFormat fromPath(Path file) {
        String name = file.getFileName().toString().toLowerCase(Locale.ROOT);
        if (name.endsWith(".json")) {
            return JSON;
        }
        if (name.endsWith(".yaml") || name.endsWith(".yml")) {
            return YAML;
        }
        throw new IllegalArgumentException("Unsupported file extension: " + file.getFileName());
    }

    public ObjectMapper mapper() {
        return mapper;
    }
}
