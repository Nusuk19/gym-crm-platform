package com.gym.crm.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Builder
public class CreateTrainingRequest {

    @NotBlank(message = "Trainee username is required")
    @Size(min = 3, max = 110, message = "Username must be between 3 and 110 characters")
    private final String traineeUsername;

    @NotBlank(message = "Trainer username is required")
    @Size(min = 3, max = 110, message = "Username must be between 3 and 110 characters")
    private final String trainerUsername;

    @NotBlank(message = "Training name is required")
    @Size(max = 100, message = "Training name must not exceed 100 characters")
    private final String trainingName;

    @NotNull(message = "Training date is required")
    private final LocalDate trainingDate;

    @Positive(message = "Training duration must be a positive number")
    @DecimalMin(value = "1", message = "Training duration must be at least 1 minute")
    private final BigDecimal trainingDuration;
}