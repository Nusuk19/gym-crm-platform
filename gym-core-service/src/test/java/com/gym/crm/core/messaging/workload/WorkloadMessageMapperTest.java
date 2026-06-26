package com.gym.crm.core.messaging.workload;

import com.gym.crm.core.model.Trainer;
import com.gym.crm.core.model.Training;
import com.gym.crm.core.model.User;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

class WorkloadMessageMapperTest {

    private static final String USERNAME = "abdul.hariton";
    private static final String FIRST_NAME = "Abdul";
    private static final String LAST_NAME = "Hariton";
    private static final int YEAR = 2026;
    private static final int MONTH = 9;
    private static final int DAY = 10;
    private static final int DURATION = 60;

    private final WorkloadMessageMapper mapper = new WorkloadMessageMapper();

    @Test
    void toMessage_shouldMapAllFields_whenActionTypeIsAdd() {
        Training training = buildTraining(true);

        TrainerWorkloadMessage actual = mapper.toMessage(training, ActionType.ADD);

        assertThat(actual.trainerUsername()).isEqualTo("abdul.hariton");
        assertThat(actual.trainerFirstName()).isEqualTo("Abdul");
        assertThat(actual.trainerLastName()).isEqualTo("Hariton");
        assertThat(actual.isActive()).isTrue();
        assertThat(actual.trainingDate()).isEqualTo(LocalDate.of(2026, 9, 10));
        assertThat(actual.trainingDuration()).isEqualTo(60);
        assertThat(actual.actionType()).isEqualTo(ActionType.ADD);
    }

    @Test
    void toMessage_shouldMapDeleteAction_whenTrainerIsInactive() {
        Training training = buildTraining(false);

        TrainerWorkloadMessage actual = mapper.toMessage(training, ActionType.DELETE);

        assertThat(actual.actionType()).isEqualTo(ActionType.DELETE);
        assertThat(actual.isActive()).isFalse();
        assertThat(actual.trainerUsername()).isEqualTo("abdul.hariton");
    }

    private Training buildTraining(boolean isActive) {
        User user = User.builder()
                .username(USERNAME)
                .firstName(FIRST_NAME)
                .lastName(LAST_NAME)
                .isActive(isActive)
                .build();
        Trainer trainer = Trainer.builder()
                .user(user)
                .build();
        return Training.builder()
                .trainer(trainer)
                .trainingDate(LocalDate.of(YEAR, MONTH, DAY))
                .trainingDuration(new BigDecimal(DURATION))
                .build();
    }
}