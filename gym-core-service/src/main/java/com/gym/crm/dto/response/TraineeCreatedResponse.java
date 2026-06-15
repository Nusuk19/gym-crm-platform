package com.gym.crm.dto.response;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

@Getter
@Builder
@ToString
public class TraineeCreatedResponse {
    private final String username;
    @ToString.Exclude
    private final String password;
}