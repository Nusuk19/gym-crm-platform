package com.gym.crm.core.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AssignedTraineeInfo {
    private final String username;
    private final String firstName;
    private final String lastName;
}