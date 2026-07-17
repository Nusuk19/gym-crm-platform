package com.gym.crm.automation.support;

import java.util.LinkedHashMap;
import java.util.Map;

public final class Payloads {

    private Payloads() {
    }

    public static Map<String, Object> login(String username, String password) {
        Map<String, Object> body = new LinkedHashMap<>();

        body.put("username", username);
        body.put("password", password);

        return body;
    }

    public static Map<String, Object> trainee(String firstName, String lastName) {
        Map<String, Object> body = new LinkedHashMap<>();

        body.put("firstName", firstName);
        body.put("lastName", lastName);

        return body;
    }

    public static Map<String, Object> trainer(String firstName, String lastName, String specialization) {
        Map<String, Object> body = new LinkedHashMap<>();

        body.put("firstName", firstName);
        body.put("lastName", lastName);
        body.put("specialization", specialization);

        return body;
    }

    public static Map<String, Object> training(String trainingName, String trainingDate, int trainingDuration,
                                               String traineeUsername, String trainerUsername) {
        Map<String, Object> body = new LinkedHashMap<>();

        body.put("trainingName", trainingName);
        body.put("trainingDate", trainingDate);
        body.put("trainingDuration", trainingDuration);
        body.put("traineeUsername", traineeUsername);
        body.put("trainerUsername", trainerUsername);

        return body;
    }
}
