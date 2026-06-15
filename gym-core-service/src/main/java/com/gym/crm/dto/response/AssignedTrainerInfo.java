package com.gym.crm.dto.response;

import com.gym.crm.model.TrainingType;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AssignedTrainerInfo {
    private final String username;
    private final String firstName;
    private final String lastName;
    private final TrainingType specialization;
}