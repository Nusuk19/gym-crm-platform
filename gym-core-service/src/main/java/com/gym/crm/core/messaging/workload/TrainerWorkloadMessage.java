package com.gym.crm.core.messaging.workload;

import java.time.LocalDate;

public record TrainerWorkloadMessage(
        String trainerUsername,
        String trainerFirstName,
        String trainerLastName,
        Boolean isActive,
        LocalDate trainingDate,
        Integer trainingDuration,
        ActionType actionType) {
}