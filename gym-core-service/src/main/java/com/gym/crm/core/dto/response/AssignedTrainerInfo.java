package com.gym.crm.core.dto.response;

import com.gym.crm.core.model.TrainingType;
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