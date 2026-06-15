package com.gym.crm.util;

import lombok.experimental.UtilityClass;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

@UtilityClass
public class JsonResourceReader {

    public String readResource(String path) {
        try (InputStream inputStream = JsonResourceReader.class.getResourceAsStream(path)) {
            if (inputStream == null) {
                throw new IllegalArgumentException(String.format("Resource not found: %s", path));
            }

            return new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException exception) {
            throw new IllegalStateException(String.format("Failed to read JSON resource: %s", path), exception);
        }
    }
}