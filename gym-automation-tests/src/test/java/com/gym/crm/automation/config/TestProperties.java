package com.gym.crm.automation.config;

import lombok.experimental.UtilityClass;
import org.yaml.snakeyaml.Yaml;

import java.io.InputStream;
import java.util.LinkedHashMap;
import java.util.Map;

@UtilityClass
public class TestProperties {

    private static final Map<String, String> CONFIG = loadFlattened("automation-test.yml");

    public static String coreBaseUrl() {
        return value("system.tests.core.base-url");
    }

    public static String workloadBaseUrl() {
        return value("system.tests.workload.base-url");
    }

    private static String value(String key) {
        String override = System.getProperty(key);
        if (override != null && !override.isBlank()) {
            return override;
        }

        String fromFile = CONFIG.get(key);
        if (fromFile == null || fromFile.isBlank()) {
            throw new IllegalStateException(
                    "Missing automation-test property '" + key + "': set it in automation-test.yml or pass -D" + key + "=...");
        }

        return fromFile;
    }

    @SuppressWarnings("unchecked")
    private static Map<String, String> loadFlattened(String resourceName) {
        try (InputStream input = TestProperties.class.getClassLoader().getResourceAsStream(resourceName)) {
            if (input == null) {
                throw new IllegalStateException(resourceName + " not found on the test classpath");
            }

            Map<String, Object> raw = new Yaml().load(input);
            Map<String, String> flattened = new LinkedHashMap<>();
            flatten("", raw, flattened);

            return flattened;
        } catch (IllegalStateException e) {
            throw e;
        } catch (Exception e) {
            throw new IllegalStateException("Failed to load " + resourceName, e);
        }
    }

    @SuppressWarnings("unchecked")
    private static void flatten(String prefix, Map<String, Object> node, Map<String, String> target) {
        for (Map.Entry<String, Object> entry : node.entrySet()) {
            String key = prefix.isEmpty() ? entry.getKey() : prefix + "." + entry.getKey();
            Object value = entry.getValue();

            if (value instanceof Map<?, ?>) {
                flatten(key, (Map<String, Object>) value, target);
            } else {
                target.put(key, value == null ? null : value.toString());
            }
        }
    }
}
