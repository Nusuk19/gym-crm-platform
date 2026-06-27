package com.gym.crm.core.messaging.workload;

import com.gym.crm.core.model.Training;
import org.springframework.stereotype.Component;

@Component
public class WorkloadMessageMapper {

    public TrainerWorkloadMessage toMessage(Training training, ActionType actionType) {
        return new TrainerWorkloadMessage(training.getTrainer().getUser().getUsername(),
                training.getTrainer().getUser().getFirstName(),
                training.getTrainer().getUser().getLastName(),
                training.getTrainer().getUser().getIsActive(),
                training.getTrainingDate(),
                training.getTrainingDuration().intValue(),
                actionType);
    }
}