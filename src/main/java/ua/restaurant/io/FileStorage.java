package ua.restaurant.io;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;

public class FileStorage<T> {

    private final Class<T> type;

    public FileStorage(Class<T> type) {
        this.type = type;
    }

    public List<T> load(Path file) throws IOException {
        ObjectMapper mapper = DataFormat.fromPath(file).mapper();
        return mapper.readValue(file.toFile(), listType(mapper));
    }

    public void save(Path file, List<T> items) throws IOException {
        ObjectMapper mapper = DataFormat.fromPath(file).mapper();
        if (file.getParent() != null) {
            Files.createDirectories(file.getParent());
        }
        mapper.writerFor(listType(mapper)).writeValue(file.toFile(), items);
    }

    private JavaType listType(ObjectMapper mapper) {
        return mapper.getTypeFactory().constructCollectionType(List.class, type);
    }
}
