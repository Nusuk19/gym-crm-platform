package com.gym.crm.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class CreateTrainerRequest {

    @NotBlank(message = "First name cannot be blank")
    @Size(max = 50, message = "First name must not exceed 50 characters")
    private final String firstName;

    @NotBlank(message = "Last name cannot be blank")
    @Size(max = 50, message = "Last name must not exceed 50 characters")
    private final String lastName;

    @NotBlank(message = "Specialization is required")
    @Size(max = 50, message = "Specialization name must not exceed 50 characters")
    private final String specializationName;
}