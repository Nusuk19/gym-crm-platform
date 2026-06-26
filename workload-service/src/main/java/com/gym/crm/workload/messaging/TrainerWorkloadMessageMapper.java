package com.gym.crm.workload.messaging;

import com.gym.crm.workload.openapi.ActionType;
import com.gym.crm.workload.openapi.TrainerWorkloadRequest;
import org.springframework.stereotype.Component;

@Component
public class TrainerWorkloadMessageMapper {

    public TrainerWorkloadRequest toRequest(TrainerWorkloadMessage message) {
        return new TrainerWorkloadRequest()
                .trainerUsername(message.trainerUsername())
                .trainerFirstName(message.trainerFirstName())
                .trainerLastName(message.trainerLastName())
                .isActive(message.isActive())
                .trainingDate(message.trainingDate())
                .trainingDuration(message.trainingDuration())
                .actionType(ActionType.valueOf(message.actionType().name()));
    }
}