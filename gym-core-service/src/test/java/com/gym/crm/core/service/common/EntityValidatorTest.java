package com.gym.crm.core.service.common;

import com.gym.crm.core.exception.EntityValidationException;
import com.gym.crm.core.model.Trainee;
import com.gym.crm.core.model.Trainer;
import com.gym.crm.core.model.Training;
import com.gym.crm.core.model.TrainingType;
import com.gym.crm.core.model.User;
import com.gym.crm.core.service.common.EntityValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class EntityValidatorTest {

    private EntityValidator validator;

    @BeforeEach
    void setUp() {
        validator = new EntityValidator();
    }

    @Test
    void validateTrainee_whenValid_doesNotThrow() {
        assertDoesNotThrow(() -> validator.validateTrainee(validTrainee()));
    }

    @Test
    void validateTrainee_whenNull_throwsWithCorrectMessage() {
        EntityValidationException ex = assertThrows(EntityValidationException.class,
                () -> validator.validateTrainee(null));

        assertEquals("Trainee cannot be null", ex.getMessage());
    }

    @Test
    void validateTrainee_whenUserIsNull_throwsWithCorrectMessage() {
        Trainee trainee = Trainee.builder().build();

        EntityValidationException ex = assertThrows(EntityValidationException.class,
                () -> validator.validateTrainee(trainee));

        assertEquals("User cannot be null", ex.getMessage());
    }

    @Test
    void validateTrainee_whenFirstNameIsNull_throwsWithCorrectMessage() {
        Trainee trainee = validTrainee().toBuilder()
                .user(validUser().toBuilder().firstName(null).build())
                .build();

        EntityValidationException ex = assertThrows(EntityValidationException.class,
                () -> validator.validateTrainee(trainee));

        assertEquals("First name cannot be blank", ex.getMessage());
    }

    @Test
    void validateTrainee_whenFirstNameIsBlank_throwsWithCorrectMessage() {
        Trainee trainee = validTrainee().toBuilder()
                .user(validUser().toBuilder().firstName("   ").build())
                .build();

        EntityValidationException ex = assertThrows(EntityValidationException.class,
                () -> validator.validateTrainee(trainee));

        assertEquals("First name cannot be blank", ex.getMessage());
    }

    @Test
    void validateTrainee_whenLastNameIsNull_throwsWithCorrectMessage() {
        Trainee trainee = validTrainee().toBuilder()
                .user(validUser().toBuilder().lastName(null).build())
                .build();

        EntityValidationException ex = assertThrows(EntityValidationException.class,
                () -> validator.validateTrainee(trainee));

        assertEquals("Last name cannot be blank", ex.getMessage());
    }

    @Test
    void validateTrainee_whenLastNameIsBlank_throwsWithCorrectMessage() {
        Trainee trainee = validTrainee().toBuilder()
                .user(validUser().toBuilder().lastName("").build())
                .build();

        EntityValidationException ex = assertThrows(EntityValidationException.class,
                () -> validator.validateTrainee(trainee));

        assertEquals("Last name cannot be blank", ex.getMessage());
    }

    @Test
    void validateTrainee_whenBothNamesAreNull_throwsOnFirstName() {
        Trainee trainee = validTrainee().toBuilder()
                .user(validUser().toBuilder().firstName(null).lastName(null).build())
                .build();

        EntityValidationException ex = assertThrows(EntityValidationException.class,
                () -> validator.validateTrainee(trainee));

        assertEquals("First name cannot be blank", ex.getMessage());
    }

    @Test
    void validateTrainer_whenValid_doesNotThrow() {
        assertDoesNotThrow(() -> validator.validateTrainer(validTrainer()));
    }

    @Test
    void validateTrainer_whenNull_throwsWithCorrectMessage() {
        EntityValidationException ex = assertThrows(EntityValidationException.class,
                () -> validator.validateTrainer(null));

        assertEquals("Trainer cannot be null", ex.getMessage());
    }

    @Test
    void validateTrainer_whenFirstNameIsBlank_throwsWithCorrectMessage() {
        Trainer trainer = validTrainer().toBuilder()
                .user(validUser().toBuilder().firstName("").build())
                .build();

        EntityValidationException ex = assertThrows(EntityValidationException.class,
                () -> validator.validateTrainer(trainer));

        assertEquals("First name cannot be blank", ex.getMessage());
    }

    @Test
    void validateTrainer_whenLastNameIsBlank_throwsWithCorrectMessage() {
        Trainer trainer = validTrainer().toBuilder()
                .user(validUser().toBuilder().lastName("  ").build())
                .build();

        EntityValidationException ex = assertThrows(EntityValidationException.class,
                () -> validator.validateTrainer(trainer));

        assertEquals("Last name cannot be blank", ex.getMessage());
    }

    @Test
    void validateTrainer_whenBothNamesAreBlank_throwsOnFirstName() {
        Trainer trainer = validTrainer().toBuilder()
                .user(validUser().toBuilder().firstName("").lastName("").build())
                .build();

        EntityValidationException ex = assertThrows(EntityValidationException.class,
                () -> validator.validateTrainer(trainer));

        assertEquals("First name cannot be blank", ex.getMessage());
    }

    @Test
    void validateTraining_whenValid_doesNotThrow() {
        assertDoesNotThrow(() -> validator.validateTraining(validTraining()));
    }

    @Test
    void validateTraining_whenNull_throwsWithCorrectMessage() {
        EntityValidationException ex = assertThrows(EntityValidationException.class,
                () -> validator.validateTraining(null));

        assertEquals("Training cannot be null", ex.getMessage());
    }

    @Test
    void validateTraining_whenTrainingNameIsBlank_throwsWithCorrectMessage() {
        Training training = validTraining().toBuilder()
                .name("  ")
                .build();

        EntityValidationException ex = assertThrows(EntityValidationException.class,
                () -> validator.validateTraining(training));

        assertEquals("Training name cannot be blank", ex.getMessage());
    }

    @Test
    void validateTraining_whenTraineeIsNull_throwsWithCorrectMessage() {
        Training training = validTraining().toBuilder()
                .trainee(null)
                .build();

        EntityValidationException ex = assertThrows(EntityValidationException.class,
                () -> validator.validateTraining(training));

        assertEquals("Trainee cannot be null", ex.getMessage());
    }

    @Test
    void validateTraining_whenTrainerIsNull_throwsWithCorrectMessage() {
        Training training = validTraining().toBuilder()
                .trainer(null)
                .build();

        EntityValidationException ex = assertThrows(EntityValidationException.class,
                () -> validator.validateTraining(training));

        assertEquals("Trainer cannot be null", ex.getMessage());
    }

    @Test
    void validateTraining_whenTrainingDateIsNull_throwsWithCorrectMessage() {
        Training training = validTraining().toBuilder()
                .trainingDate(null)
                .build();

        EntityValidationException ex = assertThrows(EntityValidationException.class,
                () -> validator.validateTraining(training));

        assertEquals("Training date cannot be null", ex.getMessage());
    }

    @Test
    void validateTraining_whenTrainingTypeIsNull_throwsWithCorrectMessage() {
        Training training = validTraining().toBuilder()
                .trainingType(null)
                .build();

        EntityValidationException ex = assertThrows(EntityValidationException.class,
                () -> validator.validateTraining(training));

        assertEquals("Training type cannot be null", ex.getMessage());
    }

    @Test
    void requireValidId_whenPositive_doesNotThrow() {
        assertDoesNotThrow(() -> validator.requireValidId(1L));
    }

    @Test
    void requireValidId_whenNull_throwsValidationException() {
        EntityValidationException ex = assertThrows(EntityValidationException.class,
                () -> validator.requireValidId(null));

        assertEquals("Id must be a positive integer, but was null", ex.getMessage());
    }

    @Test
    void requireValidId_whenZero_throwsValidationException() {
        EntityValidationException ex = assertThrows(EntityValidationException.class,
                () -> validator.requireValidId(0L));

        assertEquals("Id must be a positive integer, but was 0", ex.getMessage());
    }

    @Test
    void requireValidId_whenNegative_throwsValidationException() {
        EntityValidationException ex = assertThrows(EntityValidationException.class,
                () -> validator.requireValidId(-5L));

        assertEquals("Id must be a positive integer, but was -5", ex.getMessage());
    }

    private User validUser() {
        return User.builder()
                .firstName("Abdul")
                .lastName("Hariton")
                .build();
    }

    private Trainee validTrainee() {
        return Trainee.builder()
                .user(validUser())
                .build();
    }

    private Trainer validTrainer() {
        return Trainer.builder()
                .user(validUser())
                .specialization(TrainingType.builder().trainingTypeName("BOXING").build())
                .build();
    }

    private Training validTraining() {
        return Training.builder()
                .name("Boxing basics")
                .trainee(validTrainee())
                .trainer(validTrainer())
                .trainingDate(LocalDate.now())
                .trainingType(TrainingType.builder().trainingTypeName("BOXING").build())
                .trainingDuration(java.math.BigDecimal.valueOf(60))
                .build();
    }
}