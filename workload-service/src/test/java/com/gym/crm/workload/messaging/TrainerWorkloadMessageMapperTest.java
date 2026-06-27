package com.gym.crm.workload.messaging;

import com.gym.crm.workload.openapi.ActionType;
import com.gym.crm.workload.openapi.TrainerWorkloadRequest;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

class TrainerWorkloadMessageMapperTest {

    private static final String USERNAME = "abdul.hariton";
    private static final String FIRST_NAME = "Abdul";
    private static final String LAST_NAME = "Hariton";
    private static final int YEAR = 2026;
    private static final int MONTH = 6;
    private static final int DAY = 10;
    private static final int DURATION = 60;

    private final TrainerWorkloadMessageMapper mapper = new TrainerWorkloadMessageMapper();

    @Test
    void toRequest_shouldMapAllFields_whenActionTypeIsAdd() {
        TrainerWorkloadMessage message = buildMessage(ActionType.ADD);

        TrainerWorkloadRequest actual = mapper.toRequest(message);

        assertThat(actual.getTrainerUsername()).isEqualTo(USERNAME);
        assertThat(actual.getTrainerFirstName()).isEqualTo(FIRST_NAME);
        assertThat(actual.getTrainerLastName()).isEqualTo(LAST_NAME);
        assertThat(actual.getIsActive()).isTrue();
        assertThat(actual.getTrainingDate()).isEqualTo(LocalDate.of(YEAR, MONTH, DAY));
        assertThat(actual.getTrainingDuration()).isEqualTo(DURATION);
        assertThat(actual.getActionType()).isEqualTo(ActionType.ADD);
    }

    @Test
    void toRequest_shouldMapDeleteAction_whenActionTypeIsDelete() {
        TrainerWorkloadMessage message = buildMessage(ActionType.DELETE);

        TrainerWorkloadRequest actual = mapper.toRequest(message);

        assertThat(actual.getActionType()).isEqualTo(ActionType.DELETE);
    }

    private TrainerWorkloadMessage buildMessage(ActionType actionType) {
        return new TrainerWorkloadMessage(USERNAME, FIRST_NAME, LAST_NAME, true, LocalDate.of(YEAR, MONTH, DAY),
                DURATION, actionType);
    }
}