package com.gym.crm.workload.messaging;

import com.gym.crm.workload.openapi.ActionType;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class WorkloadMessageValidatorTest {

    private static final String USERNAME = "abdul.hariton";
    private static final String FIRST_NAME = "Abdul";
    private static final String LAST_NAME = "Hariton";
    private static final int YEAR = 2026;
    private static final int MONTH = 6;
    private static final int DAY = 10;
    private static final int DURATION = 60;

    private final WorkloadMessageValidator validator = new WorkloadMessageValidator();

    @Test
    void validate_shouldReturnEmpty_whenMessageIsValid() {
        TrainerWorkloadMessage message = buildMessage();

        Optional<String> actual = validator.validate(message);

        assertThat(actual).isEmpty();
    }

    @Test
    void validate_shouldReturnReason_whenMessageIsNull() {
        Optional<String> actual = validator.validate(null);

        assertThat(actual).isPresent();
        assertThat(actual.get()).isEqualTo("message is null");
    }

    @Test
    void validate_shouldReturnReason_whenTrainerUsernameIsNull() {
        TrainerWorkloadMessage message = new TrainerWorkloadMessage(null, FIRST_NAME, LAST_NAME, true,
                LocalDate.of(YEAR, MONTH, DAY), DURATION, ActionType.ADD);

        Optional<String> actual = validator.validate(message);

        assertThat(actual).isPresent();
        assertThat(actual.get()).isEqualTo("Required field is missing: trainerUsername");
    }

    @Test
    void validate_shouldReturnReason_whenTrainerUsernameIsBlank() {
        TrainerWorkloadMessage message = new TrainerWorkloadMessage("  ", FIRST_NAME, LAST_NAME, true,
                LocalDate.of(YEAR, MONTH, DAY), DURATION, ActionType.ADD);

        Optional<String> actual = validator.validate(message);

        assertThat(actual).isPresent();
        assertThat(actual.get()).isEqualTo("Required field is missing: trainerUsername");
    }

    @Test
    void validate_shouldReturnReason_whenTrainerFirstNameIsNull() {
        TrainerWorkloadMessage message = new TrainerWorkloadMessage(USERNAME, null, LAST_NAME, true,
                LocalDate.of(YEAR, MONTH, DAY), DURATION, ActionType.ADD);

        Optional<String> actual = validator.validate(message);

        assertThat(actual).isPresent();
        assertThat(actual.get()).isEqualTo("Required field is missing: trainerFirstName");
    }

    @Test
    void validate_shouldReturnReason_whenTrainerLastNameIsNull() {
        TrainerWorkloadMessage message = new TrainerWorkloadMessage(USERNAME, FIRST_NAME, null, true,
                LocalDate.of(YEAR, MONTH, DAY), DURATION, ActionType.ADD);

        Optional<String> actual = validator.validate(message);

        assertThat(actual).isPresent();
        assertThat(actual.get()).isEqualTo("Required field is missing: trainerLastName");
    }

    @Test
    void validate_shouldReturnReason_whenIsActiveIsNull() {
        TrainerWorkloadMessage message = new TrainerWorkloadMessage(USERNAME, FIRST_NAME, LAST_NAME, null,
                LocalDate.of(YEAR, MONTH, DAY), DURATION, ActionType.ADD);

        Optional<String> actual = validator.validate(message);

        assertThat(actual).isPresent();
        assertThat(actual.get()).isEqualTo("Required field is missing: isActive");
    }

    @Test
    void validate_shouldReturnReason_whenTrainingDateIsNull() {
        TrainerWorkloadMessage message = new TrainerWorkloadMessage(USERNAME, FIRST_NAME, LAST_NAME, true,
                null, DURATION, ActionType.ADD);

        Optional<String> actual = validator.validate(message);

        assertThat(actual).isPresent();
        assertThat(actual.get()).isEqualTo("Required field is missing: trainingDate");
    }

    @Test
    void validate_shouldReturnReason_whenActionTypeIsNull() {
        TrainerWorkloadMessage message = new TrainerWorkloadMessage(USERNAME, FIRST_NAME, LAST_NAME, true,
                LocalDate.of(YEAR, MONTH, DAY), DURATION, null);

        Optional<String> actual = validator.validate(message);

        assertThat(actual).isPresent();
        assertThat(actual.get()).isEqualTo("Required field is missing: actionType");
    }

    @Test
    void validate_shouldReturnReason_whenTrainingDurationIsNull() {
        TrainerWorkloadMessage message = new TrainerWorkloadMessage(USERNAME, FIRST_NAME, LAST_NAME, true,
                LocalDate.of(YEAR, MONTH, DAY), null, ActionType.ADD);

        Optional<String> actual = validator.validate(message);

        assertThat(actual).isPresent();
        assertThat(actual.get()).isEqualTo("Required field is missing: trainingDuration");
    }

    @Test
    void validate_shouldReturnReason_whenTrainingDurationIsZero() {
        TrainerWorkloadMessage message = new TrainerWorkloadMessage(USERNAME, FIRST_NAME, LAST_NAME, true,
                LocalDate.of(YEAR, MONTH, DAY), 0, ActionType.ADD);

        Optional<String> actual = validator.validate(message);

        assertThat(actual).isPresent();
        assertThat(actual.get()).isEqualTo("trainingDuration must be positive");
    }

    @Test
    void validate_shouldReturnReason_whenTrainingDurationIsNegative() {
        TrainerWorkloadMessage message = new TrainerWorkloadMessage(USERNAME, FIRST_NAME, LAST_NAME, true,
                LocalDate.of(YEAR, MONTH, DAY), -1, ActionType.ADD);

        Optional<String> actual = validator.validate(message);

        assertThat(actual).isPresent();
        assertThat(actual.get()).isEqualTo("trainingDuration must be positive");
    }

    private TrainerWorkloadMessage buildMessage() {
        return new TrainerWorkloadMessage(USERNAME, FIRST_NAME, LAST_NAME, true,
                LocalDate.of(YEAR, MONTH, DAY), DURATION, ActionType.ADD);
    }
}