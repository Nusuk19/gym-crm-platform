package com.gym.crm.automation.support;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.LinkedHashMap;
import java.util.Map;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class Payloads {

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

    public static Map<String, Object> workload(String trainerUsername, String firstName, String lastName,
                                               boolean isActive, String trainingDate, int trainingDuration,
                                               String actionType) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("trainerUsername", trainerUsername);
        body.put("trainerFirstName", firstName);
        body.put("trainerLastName", lastName);
        body.put("isActive", isActive);
        body.put("trainingDate", trainingDate);
        body.put("trainingDuration", trainingDuration);
        body.put("actionType", actionType);

        return body;
    }
}
