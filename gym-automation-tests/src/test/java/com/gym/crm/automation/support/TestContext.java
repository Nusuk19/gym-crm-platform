package com.gym.crm.automation.support;

import io.restassured.response.Response;
import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;
import java.util.Map;

@Getter
@Setter
public class TestContext {

    private final Map<String, String> values = new HashMap<>();

    private String token;
    private Response lastResponse;

    public void put(String key, String value) {
        values.put(key, value);
    }

    public String get(String key) {
        return values.get(key);
    }
}
