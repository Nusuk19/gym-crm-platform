package com.gym.crm.service.common;

import com.gym.crm.dto.request.ChangePasswordRequest;
import com.gym.crm.exception.EntityValidationException;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.hibernate.validator.messageinterpolation.ParameterMessageInterpolator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CoreValidatorTest {

    private CoreValidator coreValidator;

    @BeforeEach
    void setUp() {
        Validator validator = Validation.byDefaultProvider()
                .configure()
                .messageInterpolator(new ParameterMessageInterpolator())
                .buildValidatorFactory()
                .getValidator();

        coreValidator = new CoreValidator(validator);
    }

    @Test
    void validate_validObject_doesNotThrow() {
        ChangePasswordRequest request = ChangePasswordRequest.builder()
                .username("Abdul.Hariton")
                .oldPassword("oldPassword")
                .newPassword("newPassword123")
                .build();

        assertThatCode(() -> coreValidator.validate(request)).doesNotThrowAnyException();
    }

    @Test
    void validate_blankUsername_throwsWithMessage() {
        ChangePasswordRequest request = ChangePasswordRequest.builder()
                .username("")
                .oldPassword("oldPassword")
                .newPassword("newPassword123")
                .build();

        assertThatThrownBy(() -> coreValidator.validate(request))
                .isInstanceOf(EntityValidationException.class)
                .hasMessageContaining("Validation failed")
                .hasMessageContaining("username");
    }

    @Test
    void validate_nullUsername_throwsWithMessage() {
        ChangePasswordRequest request = ChangePasswordRequest.builder()
                .username(null)
                .oldPassword("oldPassword")
                .newPassword("newPassword123")
                .build();

        assertThatThrownBy(() -> coreValidator.validate(request))
                .isInstanceOf(EntityValidationException.class)
                .hasMessageContaining("username");
    }

    @Test
    void validate_tooShortUsername_throwsWithMessage() {
        ChangePasswordRequest request = ChangePasswordRequest.builder()
                .username("ab")
                .oldPassword("oldPassword")
                .newPassword("newPassword123")
                .build();

        assertThatThrownBy(() -> coreValidator.validate(request))
                .isInstanceOf(EntityValidationException.class)
                .hasMessageContaining("username")
                .hasMessageContaining("Username must be between 3 and 110 characters long");
    }

    @Test
    void validate_tooLongUsername_throwsWithMessage() {
        ChangePasswordRequest request = ChangePasswordRequest.builder()
                .username("a".repeat(111))
                .oldPassword("oldPassword")
                .newPassword("newPassword123")
                .build();

        assertThatThrownBy(() -> coreValidator.validate(request))
                .isInstanceOf(EntityValidationException.class)
                .hasMessageContaining("username")
                .hasMessageContaining("Username must be between 3 and 110 characters long");
    }

    @Test
    void validate_blankOldPassword_throwsWithMessage() {
        ChangePasswordRequest request = ChangePasswordRequest.builder()
                .username("Abdul.Hariton")
                .oldPassword("")
                .newPassword("newPassword123")
                .build();

        assertThatThrownBy(() -> coreValidator.validate(request))
                .isInstanceOf(EntityValidationException.class)
                .hasMessageContaining("oldPassword")
                .hasMessageContaining("Old password is required");
    }

    @Test
    void validate_nullOldPassword_throwsWithMessage() {
        ChangePasswordRequest request = ChangePasswordRequest.builder()
                .username("Abdul.Hariton")
                .oldPassword(null)
                .newPassword("newPassword123")
                .build();

        assertThatThrownBy(() -> coreValidator.validate(request))
                .isInstanceOf(EntityValidationException.class)
                .hasMessageContaining("oldPassword");
    }

    @Test
    void validate_blankNewPassword_throwsWithMessage() {
        ChangePasswordRequest request = ChangePasswordRequest.builder()
                .username("Abdul.Hariton")
                .oldPassword("oldPassword")
                .newPassword("")
                .build();

        assertThatThrownBy(() -> coreValidator.validate(request))
                .isInstanceOf(EntityValidationException.class)
                .hasMessageContaining("newPassword")
                .hasMessageContaining("New password is required");
    }

    @Test
    void validate_tooShortNewPassword_throwsWithMessage() {
        ChangePasswordRequest request = ChangePasswordRequest.builder()
                .username("Abdul.Hariton")
                .oldPassword("oldPassword")
                .newPassword("short")
                .build();

        assertThatThrownBy(() -> coreValidator.validate(request))
                .isInstanceOf(EntityValidationException.class)
                .hasMessageContaining("newPassword")
                .hasMessageContaining("New password must be between 10 and 100 characters");
    }

    @Test
    void validate_tooLongNewPassword_throwsWithMessage() {
        ChangePasswordRequest request = ChangePasswordRequest.builder()
                .username("Abdul.Hariton")
                .oldPassword("oldPassword")
                .newPassword("a".repeat(101))
                .build();

        assertThatThrownBy(() -> coreValidator.validate(request))
                .isInstanceOf(EntityValidationException.class)
                .hasMessageContaining("newPassword")
                .hasMessageContaining("New password must be between 10 and 100 characters");
    }

    @Test
    void validate_multipleViolations_messageContainsAllViolations() {
        ChangePasswordRequest request = ChangePasswordRequest.builder()
                .username("")
                .oldPassword("")
                .newPassword("")
                .build();

        assertThatThrownBy(() -> coreValidator.validate(request))
                .isInstanceOf(EntityValidationException.class)
                .hasMessageContaining("Validation failed")
                .hasMessageContaining("username")
                .hasMessageContaining("oldPassword")
                .hasMessageContaining("newPassword");
    }

    @Test
    void validate_usernameAtMinBoundary_doesNotThrow() {
        ChangePasswordRequest request = ChangePasswordRequest.builder()
                .username("abc")
                .oldPassword("oldPassword")
                .newPassword("newPassword123")
                .build();

        assertThatCode(() -> coreValidator.validate(request)).doesNotThrowAnyException();
    }

    @Test
    void validate_usernameAtMaxBoundary_doesNotThrow() {
        ChangePasswordRequest request = ChangePasswordRequest.builder()
                .username("a".repeat(110))
                .oldPassword("oldPassword")
                .newPassword("newPassword123")
                .build();

        assertThatCode(() -> coreValidator.validate(request)).doesNotThrowAnyException();
    }

    @Test
    void validate_newPasswordAtMinBoundary_doesNotThrow() {
        ChangePasswordRequest request = ChangePasswordRequest.builder()
                .username("Abdul.Hariton")
                .oldPassword("oldPassword")
                .newPassword("1234567890")
                .build();

        assertThatCode(() -> coreValidator.validate(request)).doesNotThrowAnyException();
    }

    @Test
    void validate_newPasswordAtMaxBoundary_doesNotThrow() {
        ChangePasswordRequest request = ChangePasswordRequest.builder()
                .username("Abdul.Hariton")
                .oldPassword("oldPassword")
                .newPassword("a".repeat(100))
                .build();

        assertThatCode(() -> coreValidator.validate(request)).doesNotThrowAnyException();
    }
}