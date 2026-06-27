package com.gym.crm.workload.messaging;

import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class WorkloadMessageValidator {

    public Optional<String> validate(TrainerWorkloadMessage message) {
        if (message == null) {
            return Optional.of("message is null");
        }

        return validateRequiredText(message.trainerUsername(), "trainerUsername")
                .or(() -> validateRequiredText(message.trainerFirstName(), "trainerFirstName"))
                .or(() -> validateRequiredText(message.trainerLastName(), "trainerLastName"))
                .or(() -> validateRequiredValue(message.isActive(), "isActive"))
                .or(() -> validateRequiredValue(message.trainingDate(), "trainingDate"))
                .or(() -> validateRequiredValue(message.actionType(), "actionType"))
                .or(() -> validateDuration(message.trainingDuration()));
    }

    private Optional<String> validateRequiredText(String value, String field) {
        return value == null || value.isBlank()
                ? Optional.of("Required field is missing: " + field)
                : Optional.empty();
    }

    private Optional<String> validateRequiredValue(Object value, String field) {
        return value == null
                ? Optional.of("Required field is missing: " + field)
                : Optional.empty();
    }

    private Optional<String> validateDuration(Integer duration) {
        if (duration == null) {
            return Optional.of("Required field is missing: trainingDuration");
        }

        return duration <= 0
                ? Optional.of("trainingDuration must be positive")
                : Optional.empty();
    }
}