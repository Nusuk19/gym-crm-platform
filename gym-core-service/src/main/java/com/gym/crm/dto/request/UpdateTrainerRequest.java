package com.gym.crm.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class UpdateTrainerRequest {

    @NotBlank(message = "Username is required")
    @Size(min = 3, max = 110, message = "Username must be between 3 and 110 characters")
    private final String username;

    @NotBlank(message = "First name cannot be blank")
    @Size(max = 50, message = "First name must not exceed 50 characters")
    private final String firstName;

    @NotBlank(message = "Last name cannot be blank")
    @Size(max = 50, message = "Last name must not exceed 50 characters")
    private final String lastName;

    @NotNull(message = "Is active flag is required")
    private final Boolean isActive;
}