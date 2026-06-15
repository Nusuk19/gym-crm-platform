package com.gym.crm.core.dto.response;

import com.gym.crm.core.model.TrainingType;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Builder
public class TrainingResponse {
    private final Long id;
    private final Long traineeId;
    private final Long trainerId;
    private final String traineeUsername;
    private final String trainerUsername;
    private final String trainingName;
    private final TrainingType trainingType;
    private final String trainingTypeName;
    private final LocalDate trainingDate;
    private final BigDecimal trainingDuration;
}