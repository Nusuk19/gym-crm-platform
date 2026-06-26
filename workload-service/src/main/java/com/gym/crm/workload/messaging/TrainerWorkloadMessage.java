package com.gym.crm.workload.messaging;

import com.gym.crm.workload.openapi.ActionType;

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