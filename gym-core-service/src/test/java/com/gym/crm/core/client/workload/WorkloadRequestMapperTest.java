package com.gym.crm.core.client.workload;

import com.gym.crm.core.client.workload.model.ActionType;
import com.gym.crm.core.client.workload.model.TrainerWorkloadRequest;
import com.gym.crm.core.model.Trainer;
import com.gym.crm.core.model.Training;
import com.gym.crm.core.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Month;

import static org.assertj.core.api.Assertions.assertThat;

class WorkloadRequestMapperTest {

    private static final String USERNAME = "abdul.hariton";
    private static final String FIRST_NAME = "Abdul";
    private static final String LAST_NAME = "Hariton";

    private WorkloadRequestMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = Mappers.getMapper(WorkloadRequestMapper.class);
    }

    @Test
    void toRequest_shouldMapTrainingToWorkloadRequest_withActionTypeAdd() {
        Training training = buildTraining();

        TrainerWorkloadRequest actual = mapper.toRequest(training, ActionType.ADD);

        assertThat(actual.getTrainerUsername()).isEqualTo(USERNAME);
        assertThat(actual.getTrainerFirstName()).isEqualTo(FIRST_NAME);
        assertThat(actual.getTrainerLastName()).isEqualTo(LAST_NAME);
        assertThat(actual.getIsActive()).isTrue();
        assertThat(actual.getTrainingDate()).isEqualTo(LocalDate.of(2026, Month.JUNE, 10));
        assertThat(actual.getTrainingDuration()).isEqualTo(60);
        assertThat(actual.getActionType()).isEqualTo(ActionType.ADD);
    }

    @Test
    void toRequest_shouldMapTrainingToWorkloadRequest_withActionTypeDelete() {
        Training training = buildTraining();

        TrainerWorkloadRequest actual = mapper.toRequest(training, ActionType.DELETE);

        assertThat(actual.getActionType()).isEqualTo(ActionType.DELETE);
        assertThat(actual.getTrainerUsername()).isEqualTo(USERNAME);
    }

    @Test
    void toRequest_shouldConvertBigDecimalDurationToInteger() {
        Training training = buildTraining();

        TrainerWorkloadRequest actual = mapper.toRequest(training, ActionType.ADD);

        assertThat(actual.getTrainingDuration()).isEqualTo(60);
    }

    private Training buildTraining() {
        User user = User.builder()
                .username(USERNAME)
                .firstName(FIRST_NAME)
                .lastName(LAST_NAME)
                .isActive(Boolean.TRUE)
                .build();

        Trainer trainer = Trainer.builder()
                .user(user)
                .build();

        return Training.builder()
                .trainer(trainer)
                .trainingDate(LocalDate.of(2026, Month.JUNE, 10))
                .trainingDuration(BigDecimal.valueOf(60))
                .build();
    }
}