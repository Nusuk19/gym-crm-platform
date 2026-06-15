package com.gym.crm.core.service.common;

import com.gym.crm.core.exception.EntityValidationException;
import com.gym.crm.core.model.Trainee;
import com.gym.crm.core.model.Trainer;
import com.gym.crm.core.model.Training;
import org.springframework.stereotype.Component;

@Component
public class EntityValidator {

    public void validateTrainee(Trainee trainee) {
        requireNonNull(trainee, "Trainee cannot be null");
        requireNonNull(trainee.getUser(), "User cannot be null");
        requireNonBlank(trainee.getUser().getFirstName(), "First name cannot be blank");
        requireNonBlank(trainee.getUser().getLastName(), "Last name cannot be blank");
    }

    public void validateTrainer(Trainer trainer) {
        requireNonNull(trainer, "Trainer cannot be null");
        requireNonNull(trainer.getUser(), "User cannot be null");
        requireNonBlank(trainer.getUser().getFirstName(), "First name cannot be blank");
        requireNonBlank(trainer.getUser().getLastName(), "Last name cannot be blank");
    }

    public void validateTraining(Training training) {
        requireNonNull(training, "Training cannot be null");
        requireNonBlank(training.getName(), "Training name cannot be blank");
        requireNonNull(training.getTrainee(), "Trainee cannot be null");
        requireNonNull(training.getTrainer(), "Trainer cannot be null");
        requireNonNull(training.getTrainingDate(), "Training date cannot be null");
        requireNonNull(training.getTrainingType(), "Training type cannot be null");
    }

    public void validateForUpdate(Object entity, Long id) {
        requireNonNull(entity, "Entity cannot be null");
        requireValidId(id);
    }

    public void requireValidId(Long id) {
        if (id == null || id <= 0) {
            throw new EntityValidationException("Id must be a positive integer, but was " + id);
        }
    }

    public void requireNonBlank(String value, String message) {
        if (value == null || value.isBlank()) {
            throw new EntityValidationException(message);
        }
    }

    public void requireNonNull(Object value, String message) {
        if (value == null) {
            throw new EntityValidationException(message);
        }
    }
}