package com.gym.crm.workload.service;

import com.gym.crm.workload.config.AbstractMongoIntegrationTest;
import com.gym.crm.workload.openapi.ActionType;
import com.gym.crm.workload.openapi.TrainerMonthlyWorkloadResponse;
import com.gym.crm.workload.openapi.TrainerWorkloadRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.time.Month;
import java.util.NoSuchElementException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("test")
class TrainerWorkloadServiceTest extends AbstractMongoIntegrationTest {

    private static final String USERNAME = "abdul.hariton";
    private static final String FIRST_NAME = "Abdul";
    private static final String LAST_NAME = "Hariton";
    private static final int YEAR = 2026;
    private static final int MONTH = 6;

    @Autowired
    private TrainerWorkloadServiceImpl service;

    @Test
    void updateTrainerWorkload_shouldAddDuration_whenActionTypeAdd() {
        service.updateTrainerWorkload(createRequest(60, ActionType.ADD));

        TrainerMonthlyWorkloadResponse actual = service.getMonthlyWorkload(USERNAME, YEAR, MONTH);

        assertThat(actual.getTrainingSummaryDuration()).isEqualTo(60);
    }

    @Test
    void updateTrainerWorkload_shouldSumDuration_whenAddCalledTwice() {
        service.updateTrainerWorkload(createRequest(60, ActionType.ADD));
        service.updateTrainerWorkload(createRequest(30, ActionType.ADD));

        TrainerMonthlyWorkloadResponse actual = service.getMonthlyWorkload(USERNAME, YEAR, MONTH);

        assertThat(actual.getTrainingSummaryDuration()).isEqualTo(90);
    }

    @Test
    void updateTrainerWorkload_shouldSubtractDuration_whenActionTypeDelete() {
        service.updateTrainerWorkload(createRequest(90, ActionType.ADD));
        service.updateTrainerWorkload(createRequest(30, ActionType.DELETE));

        TrainerMonthlyWorkloadResponse actual = service.getMonthlyWorkload(USERNAME, YEAR, MONTH);

        assertThat(actual.getTrainingSummaryDuration()).isEqualTo(60);
    }

    @Test
    void updateTrainerWorkload_shouldNotSetNegativeDuration_whenDeleteGreaterThanCurrent() {
        service.updateTrainerWorkload(createRequest(30, ActionType.ADD));
        service.updateTrainerWorkload(createRequest(60, ActionType.DELETE));

        TrainerMonthlyWorkloadResponse actual = service.getMonthlyWorkload(USERNAME, YEAR, MONTH);

        assertThat(actual.getTrainingSummaryDuration()).isEqualTo(0);
    }

    @Test
    void updateTrainerWorkload_shouldNotChangeOtherMonth_whenAddingDurationToDifferentMonth() {
        service.updateTrainerWorkload(createRequest(60, ActionType.ADD));
        TrainerWorkloadRequest nextMonthRequest = createRequest(30, ActionType.ADD)
                .trainingDate(LocalDate.of(YEAR, Month.JULY, 10));
        service.updateTrainerWorkload(nextMonthRequest);

        TrainerMonthlyWorkloadResponse actualJune = service.getMonthlyWorkload(USERNAME, YEAR, MONTH);
        TrainerMonthlyWorkloadResponse actualJuly = service.getMonthlyWorkload(USERNAME, YEAR, 7);

        assertThat(actualJune.getTrainingSummaryDuration()).isEqualTo(60);
        assertThat(actualJuly.getTrainingSummaryDuration()).isEqualTo(30);
    }

    @Test
    void getMonthlyWorkload_shouldReturnZero_whenTrainerExistsButMonthNotExists() {
        service.updateTrainerWorkload(createRequest(60, ActionType.ADD));

        TrainerMonthlyWorkloadResponse actual = service.getMonthlyWorkload(USERNAME, YEAR, 7);

        assertThat(actual.getTrainingSummaryDuration()).isEqualTo(0);
    }

    @Test
    void getMonthlyWorkload_shouldThrowException_whenTrainerNotFound() {
        assertThatThrownBy(() -> service.getMonthlyWorkload("unknown.user", YEAR, MONTH))
                .isInstanceOf(NoSuchElementException.class)
                .hasMessage("Trainer workload not found: unknown.user");
    }

    private TrainerWorkloadRequest createRequest(int duration, ActionType actionType) {
        return new TrainerWorkloadRequest()
                .trainerUsername(USERNAME)
                .trainerFirstName(FIRST_NAME)
                .trainerLastName(LAST_NAME)
                .isActive(true)
                .trainingDate(LocalDate.of(YEAR, MONTH, 10))
                .trainingDuration(duration)
                .actionType(actionType);
    }
}