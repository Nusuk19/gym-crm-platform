package com.gym.crm.core.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.util.List;

@Getter
@Builder(toBuilder = true)
public class TraineeProfileResponse {
    private final Long id;
    private final String username;
    private final String firstName;
    private final String lastName;
    private final LocalDate dateOfBirth;
    private final String address;
    private final Boolean isActive;
    private final List<AssignedTrainerInfo> trainers;
}