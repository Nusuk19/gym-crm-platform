package com.gym.crm.core.mapper;

import com.gym.crm.core.dto.request.CreateTrainingRequest;
import com.gym.crm.core.dto.response.TrainingResponse;
import com.gym.crm.core.mapper.TrainingMapper;
import com.gym.crm.core.model.Trainee;
import com.gym.crm.core.model.Trainer;
import com.gym.crm.core.model.Training;
import com.gym.crm.core.model.TrainingType;
import com.gym.crm.core.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class TrainingMapperTest {

    private static final Long EXISTING_ID = 1L;
    private static final Long TRAINEE_ID = 2L;
    private static final Long TRAINER_ID = 3L;

    private TrainingMapper trainingMapper;

    @BeforeEach
    void setUp() {
        trainingMapper = Mappers.getMapper(TrainingMapper.class);
    }

    @Test
    void toEntity_fromCreateRequest_mapsAllFieldsCorrectly() {
        CreateTrainingRequest request = buildCreateRequest();

        Training actual = trainingMapper.toEntity(request);

        assertEquals("Boxing basics", actual.getName());
        assertEquals(LocalDate.of(2024, 5, 1), actual.getTrainingDate());
        assertEquals(BigDecimal.valueOf(60), actual.getTrainingDuration());
    }

    @Test
    void toEntity_fromCreateRequest_doesNotSetTrainingId() {
        CreateTrainingRequest request = buildCreateRequest();

        Training actual = trainingMapper.toEntity(request);

        assertNull(actual.getId());
    }

    @Test
    void toResponse_fromTraining_mapsAllFieldsCorrectly() {
        Training training = buildTraining();

        TrainingResponse actual = trainingMapper.toResponse(training);

        assertEquals(1L, actual.getId());
        assertEquals(TRAINEE_ID, actual.getTraineeId());
        assertEquals(TRAINER_ID, actual.getTrainerId());
        assertEquals("Boxing basics", actual.getTrainingName());
        assertEquals("BOXING", actual.getTrainingType().getTrainingTypeName());
        assertEquals(LocalDate.of(2024, 5, 1), actual.getTrainingDate());
        assertEquals(BigDecimal.valueOf(60), actual.getTrainingDuration());
    }

    @Test
    void toResponse_populatesDenormalizedFields() {
        Training training = buildTraining();

        TrainingResponse actual = trainingMapper.toResponse(training);

        assertEquals("BOXING", actual.getTrainingTypeName());
        assertEquals("trainer.user", actual.getTrainerUsername());
        assertEquals("trainee.user", actual.getTraineeUsername());
    }

    private CreateTrainingRequest buildCreateRequest() {
        return CreateTrainingRequest.builder()
                .traineeUsername("John.Doe")
                .trainerUsername("Mike.Tyson")
                .trainingName("Boxing basics")
                .trainingDate(LocalDate.of(2024, 5, 1))
                .trainingDuration(BigDecimal.valueOf(60))
                .build();
    }

    private Training buildTraining() {
        User traineeUser = User.builder()
                .username("trainee.user")
                .build();

        User trainerUser = User.builder()
                .username("trainer.user")
                .build();

        Trainee trainee = Trainee.builder()
                .id(TRAINEE_ID)
                .user(traineeUser)
                .build();

        Trainer trainer = Trainer.builder()
                .id(TRAINER_ID)
                .user(trainerUser)
                .specialization(TrainingType.builder().trainingTypeName("BOXING").build())
                .build();

        return Training.builder()
                .id(EXISTING_ID)
                .name("Boxing basics")
                .trainee(trainee)
                .trainer(trainer)
                .trainingType(TrainingType.builder().trainingTypeName("BOXING").build())
                .trainingDate(LocalDate.of(2024, 5, 1))
                .trainingDuration(BigDecimal.valueOf(60))
                .build();
    }
}