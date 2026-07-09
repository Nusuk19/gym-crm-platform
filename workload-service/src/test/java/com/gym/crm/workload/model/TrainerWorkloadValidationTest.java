package com.gym.crm.workload.model;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class TrainerWorkloadValidationTest {

    private static final String USERNAME = "abdul.hariton";
    private static final String FIRST_NAME = "Abdul";
    private static final String LAST_NAME = "Hariton";
    private static final int YEAR = 2026;
    private static final int MONTH = 6;
    private static final int DURATION = 60;

    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    @Test
    void validate_shouldPassValidation_whenAllFieldsValid() {
        TrainerWorkload workload = buildValidWorkload();

        Set<ConstraintViolation<TrainerWorkload>> violations = validator.validate(workload);

        assertThat(violations).isEmpty();
    }

    @Test
    void validate_shouldFailValidation_whenUsernameIsBlank() {
        TrainerWorkload workload = buildValidWorkload();
        workload.setUsername("");

        Set<ConstraintViolation<TrainerWorkload>> violations = validator.validate(workload);

        assertThat(violations).isNotEmpty();
        assertThat(violations).anyMatch(v -> v.getMessage().equals("Trainer username is required"));
    }

    @Test
    void validate_shouldFailValidation_whenUsernameIsNull() {
        TrainerWorkload workload = buildValidWorkload();
        workload.setUsername(null);

        Set<ConstraintViolation<TrainerWorkload>> violations = validator.validate(workload);

        assertThat(violations).isNotEmpty();
        assertThat(violations).anyMatch(v -> v.getMessage().equals("Trainer username is required"));
    }

    @Test
    void validate_shouldFailValidation_whenFirstNameIsNull() {
        TrainerWorkload workload = buildValidWorkload();
        workload.setTrainerFirstName(null);

        Set<ConstraintViolation<TrainerWorkload>> violations = validator.validate(workload);

        assertThat(violations).isNotEmpty();
        assertThat(violations).anyMatch(v -> v.getMessage().equals("Trainer first name is required"));
    }

    @Test
    void validate_shouldFailValidation_whenFirstNameIsBlank() {
        TrainerWorkload workload = buildValidWorkload();
        workload.setTrainerFirstName("   ");

        Set<ConstraintViolation<TrainerWorkload>> violations = validator.validate(workload);

        assertThat(violations).isNotEmpty();
        assertThat(violations).anyMatch(v -> v.getMessage().equals("Trainer first name is required"));
    }

    @Test
    void validate_shouldFailValidation_whenLastNameIsNull() {
        TrainerWorkload workload = buildValidWorkload();
        workload.setTrainerLastName(null);

        Set<ConstraintViolation<TrainerWorkload>> violations = validator.validate(workload);

        assertThat(violations).isNotEmpty();
        assertThat(violations).anyMatch(v -> v.getMessage().equals("Trainer last name is required"));
    }

    @Test
    void validate_shouldFailValidation_whenLastNameIsBlank() {
        TrainerWorkload workload = buildValidWorkload();
        workload.setTrainerLastName("   ");

        Set<ConstraintViolation<TrainerWorkload>> violations = validator.validate(workload);

        assertThat(violations).isNotEmpty();
        assertThat(violations).anyMatch(v -> v.getMessage().equals("Trainer last name is required"));
    }

    @Test
    void validate_shouldFailValidation_whenIsActiveIsNull() {
        TrainerWorkload workload = buildValidWorkload();
        workload.setIsActive(null);

        Set<ConstraintViolation<TrainerWorkload>> violations = validator.validate(workload);

        assertThat(violations).isNotEmpty();
        assertThat(violations).anyMatch(v -> v.getMessage().equals("Trainer status is required"));
    }

    @Test
    void validate_shouldFailValidation_whenYearsIsNull() {
        TrainerWorkload workload = buildValidWorkload();
        workload.setYears(null);

        Set<ConstraintViolation<TrainerWorkload>> violations = validator.validate(workload);

        assertThat(violations).isNotEmpty();
        assertThat(violations).anyMatch(v -> v.getMessage().equals("Years list is required"));
    }

    @Test
    void validate_shouldPassValidation_whenYearsIsEmpty() {
        TrainerWorkload workload = buildValidWorkload();
        workload.setYears(new ArrayList<>());

        Set<ConstraintViolation<TrainerWorkload>> violations = validator.validate(workload);

        assertThat(violations).isEmpty();
    }

    @Test
    void validate_shouldFailValidation_whenYearIsNull() {
        TrainerWorkload workload = buildValidWorkload();
        workload.getYears().get(0).setYear(null);

        Set<ConstraintViolation<TrainerWorkload>> violations = validator.validate(workload);

        assertThat(violations).isNotEmpty();
        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("years[0].year"));
    }

    @Test
    void validate_shouldFailValidation_whenMonthsIsNull() {
        TrainerWorkload workload = buildValidWorkload();
        workload.getYears().get(0).setMonths(null);

        Set<ConstraintViolation<TrainerWorkload>> violations = validator.validate(workload);

        assertThat(violations).isNotEmpty();
        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("years[0].months"));
    }

    @Test
    void validate_shouldPassValidation_whenMonthsIsEmpty() {
        TrainerWorkload workload = buildValidWorkload();
        workload.getYears().get(0).setMonths(new ArrayList<>());

        Set<ConstraintViolation<TrainerWorkload>> violations = validator.validate(workload);

        assertThat(violations).isEmpty();
    }

    @Test
    void validate_shouldFailValidation_whenMonthIsNull() {
        TrainerWorkload workload = buildValidWorkload();
        workload.getYears().get(0).getMonths().get(0).setMonth(null);

        Set<ConstraintViolation<TrainerWorkload>> violations = validator.validate(workload);

        assertThat(violations).isNotEmpty();
        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("years[0].months[0].month"));
    }

    @Test
    void validate_shouldFailValidation_whenTrainingSummaryDurationIsNull() {
        TrainerWorkload workload = buildValidWorkload();
        workload.getYears().get(0).getMonths().get(0).setTrainingSummaryDuration(null);

        Set<ConstraintViolation<TrainerWorkload>> violations = validator.validate(workload);

        assertThat(violations).isNotEmpty();
        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("years[0].months[0].trainingSummaryDuration"));
    }

    @Test
    void validate_shouldFailValidation_whenTrainingSummaryDurationIsNegative() {
        TrainerWorkload workload = buildValidWorkload();
        workload.getYears().get(0).getMonths().get(0).setTrainingSummaryDuration(-1);

        Set<ConstraintViolation<TrainerWorkload>> violations = validator.validate(workload);

        assertThat(violations).isNotEmpty();
        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("years[0].months[0].trainingSummaryDuration"));
    }

    @Test
    void validate_shouldPassValidation_whenTrainingSummaryDurationIsZero() {
        TrainerWorkload workload = buildValidWorkload();
        workload.getYears().get(0).getMonths().get(0).setTrainingSummaryDuration(0);

        Set<ConstraintViolation<TrainerWorkload>> violations = validator.validate(workload);

        assertThat(violations).isEmpty();
    }

    @Test
    void validate_shouldReportMultipleViolations_whenMultipleFieldsInvalid() {
        TrainerWorkload workload = buildValidWorkload();
        workload.setUsername(null);
        workload.setTrainerFirstName(null);
        workload.setTrainerLastName(null);

        Set<ConstraintViolation<TrainerWorkload>> violations = validator.validate(workload);

        assertThat(violations).hasSize(3);
    }

    private TrainerWorkload buildValidWorkload() {
        MonthSummary monthSummary = MonthSummary.builder()
                .month(MONTH)
                .trainingSummaryDuration(DURATION)
                .build();

        YearSummary yearSummary = YearSummary.builder()
                .year(YEAR)
                .months(new ArrayList<>(List.of(monthSummary)))
                .build();

        return TrainerWorkload.builder()
                .username(USERNAME)
                .trainerFirstName(FIRST_NAME)
                .trainerLastName(LAST_NAME)
                .isActive(true)
                .years(new ArrayList<>(List.of(yearSummary)))
                .build();
    }
}