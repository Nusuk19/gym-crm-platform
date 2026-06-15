package com.gym.crm.core.dto.response;

import com.gym.crm.core.model.TrainingType;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder(toBuilder = true)
public class TrainerProfileResponse {
    private final Long id;
    private final String username;
    private final String firstName;
    private final String lastName;
    private final TrainingType specialization;
    private final Boolean isActive;
    private final List<AssignedTraineeInfo> trainees;
}