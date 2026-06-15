package com.gym.crm.core.mapper;

import com.gym.crm.core.dto.request.CreateTrainerRequest;
import com.gym.crm.core.dto.request.UpdateTrainerRequest;
import com.gym.crm.core.dto.response.AssignedTraineeInfo;
import com.gym.crm.core.dto.response.TrainerCreatedResponse;
import com.gym.crm.core.dto.response.TrainerProfileResponse;
import com.gym.crm.core.mapper.TrainerMapper;
import com.gym.crm.core.model.Trainee;
import com.gym.crm.core.model.Trainer;
import com.gym.crm.core.model.TrainingType;
import com.gym.crm.core.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TrainerMapperTest {

    private static final Long EXISTING_ID = 1L;

    private TrainerMapper trainerMapper;

    @BeforeEach
    void setUp() {
        trainerMapper = Mappers.getMapper(TrainerMapper.class);
    }

    @Test
    void toEntity_fromCreateRequest_mapsAllFieldsCorrectly() {
        CreateTrainerRequest request = buildCreateRequest();

        Trainer actual = trainerMapper.toEntity(request);

        assertEquals("Mike", actual.getUser().getFirstName());
        assertEquals("Tyson", actual.getUser().getLastName());
        assertNull(actual.getSpecialization());
    }

    @Test
    void toEntity_fromCreateRequest_doesNotSetIdUsernameOrPassword() {
        CreateTrainerRequest request = CreateTrainerRequest.builder()
                .firstName("Mike")
                .lastName("Tyson")
                .build();

        Trainer actual = trainerMapper.toEntity(request);

        assertNull(actual.getId());
        assertNull(actual.getUser().getUsername());
        assertNull(actual.getUser().getPassword());
        assertNull(actual.getUser().getIsActive());
    }

    @Test
    void toEntity_fromUpdateRequest_mapsAllFieldsCorrectly() {
        UpdateTrainerRequest request = buildUpdateRequest();

        Trainer actual = trainerMapper.toEntity(request);

        assertEquals("Mike.Tyson", actual.getUser().getUsername());
        assertEquals("Mike", actual.getUser().getFirstName());
        assertEquals("Tyson", actual.getUser().getLastName());
        assertNull(actual.getSpecialization());
    }

    @Test
    void toCreatedResponse_includesUsernameAndRawPassword() {
        Trainer trainer = buildTrainerWithRawPassword();

        TrainerCreatedResponse actual = trainerMapper.toCreatedResponse(trainer);

        assertEquals("Mike.Tyson", actual.getUsername());
        assertEquals("rawPass123", actual.getPassword());
    }

    @Test
    void toProfileResponse_mapsAllFieldsCorrectly() {
        Trainer trainer = buildTrainer();

        TrainerProfileResponse actual = trainerMapper.toProfileResponse(trainer);

        assertEquals(EXISTING_ID, actual.getId());
        assertEquals("Mike", actual.getFirstName());
        assertEquals("Tyson", actual.getLastName());
        assertEquals("Mike.Tyson", actual.getUsername());
        assertEquals("BOXING", actual.getSpecialization().getTrainingTypeName());
        assertTrue(actual.getIsActive());
    }

    @Test
    void toResponse_passwordFieldNotPresentInResponse() {
        assertThrows(NoSuchFieldException.class, () -> TrainerProfileResponse.class.getDeclaredField("password"));
    }

    @Test
    void toAssignedTraineeInfo_mapsUserFields() {
        Trainee trainee = buildTrainee();

        AssignedTraineeInfo actual = trainerMapper.toAssignedTraineeInfo(trainee);

        assertEquals("Abdul.Hariton", actual.getUsername());
        assertEquals("Abdul", actual.getFirstName());
        assertEquals("Hariton", actual.getLastName());
    }

    @Test
    void toAssignedTraineeInfoList_mapsEachElement() {
        List<AssignedTraineeInfo> actual = trainerMapper.toAssignedTraineeInfoList(List.of(buildTrainee()));

        assertEquals(1, actual.size());
        assertEquals("Abdul.Hariton", actual.get(0).getUsername());
    }

    private CreateTrainerRequest buildCreateRequest() {
        return CreateTrainerRequest.builder()
                .firstName("Mike")
                .lastName("Tyson")
                .build();
    }

    private UpdateTrainerRequest buildUpdateRequest() {
        return UpdateTrainerRequest.builder()
                .username("Mike.Tyson")
                .firstName("Mike")
                .lastName("Tyson")
                .isActive(false)
                .build();
    }

    private Trainer buildTrainer() {
        User user = User.builder()
                .id(EXISTING_ID)
                .firstName("Mike")
                .lastName("Tyson")
                .username("Mike.Tyson")
                .password("hashedPassword")
                .isActive(true)
                .build();

        return Trainer.builder()
                .id(EXISTING_ID)
                .user(user)
                .specialization(TrainingType.builder().trainingTypeName("BOXING").build())
                .build();
    }

    private Trainer buildTrainerWithRawPassword() {
        User user = User.builder()
                .id(EXISTING_ID)
                .firstName("Mike")
                .lastName("Tyson")
                .username("Mike.Tyson")
                .password("hashedPassword")
                .rawPassword("rawPass123")
                .isActive(true)
                .build();

        return Trainer.builder()
                .id(EXISTING_ID)
                .user(user)
                .specialization(TrainingType.builder().trainingTypeName("BOXING").build())
                .build();
    }

    private Trainee buildTrainee() {
        User user = User.builder()
                .id(2L)
                .firstName("Abdul")
                .lastName("Hariton")
                .username("Abdul.Hariton")
                .isActive(true)
                .build();

        return Trainee.builder()
                .id(2L)
                .user(user)
                .dateOfBirth(LocalDate.of(1990, 1, 1))
                .address("Kyiv")
                .build();
    }
}