package com.gym.crm.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

@ToString
@EqualsAndHashCode
@Getter
@Builder
public class ChangePasswordRequest {

    @NotBlank(message = "Username is required")
    @Size(min = 3, max = 110, message = "Username must be between 3 and 110 characters long")
    private final String username;

    @NotBlank(message = "Old password is required")
    @Size(max = 100, message = "Old password must not exceed 100 characters")
    @ToString.Exclude
    private final String oldPassword;

    @NotBlank(message = "New password is required")
    @Size(min = 10, max = 100, message = "New password must be between 10 and 100 characters")
    @ToString.Exclude
    private final String newPassword;
}
