package com.gym.crm.mapper;

import com.gym.crm.dto.request.CreateTraineeRequest;
import com.gym.crm.dto.request.UpdateTraineeRequest;
import com.gym.crm.dto.response.AssignedTrainerInfo;
import com.gym.crm.dto.response.TraineeCreatedResponse;
import com.gym.crm.dto.response.TraineeProfileResponse;
import com.gym.crm.model.Trainee;
import com.gym.crm.model.Trainer;
import com.gym.crm.model.TrainingType;
import com.gym.crm.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TraineeMapperTest {

    private static final Long EXISTING_ID = 1L;

    private TraineeMapper traineeMapper;

    @BeforeEach
    void setUp() {
        traineeMapper = Mappers.getMapper(TraineeMapper.class);
    }

    @Test
    void toEntity_fromCreateRequest_mapsAllFieldsCorrectly() {
        CreateTraineeRequest request = buildCreateRequest();

        Trainee actual = traineeMapper.toEntity(request);

        assertEquals("Abdul", actual.getUser().getFirstName());
        assertEquals("Hariton", actual.getUser().getLastName());
        assertEquals(LocalDate.of(1990, 1, 1), actual.getDateOfBirth());
        assertEquals("Kyiv", actual.getAddress());
    }

    @Test
    void toEntity_fromCreateRequest_doesNotSetIdUsernameOrPassword() {
        CreateTraineeRequest request = buildCreateRequest();

        Trainee actual = traineeMapper.toEntity(request);

        assertNull(actual.getId());
        assertNull(actual.getUser().getUsername());
        assertNull(actual.getUser().getPassword());
        assertNull(actual.getUser().getIsActive());
    }

    @Test
    void toEntity_fromUpdateRequest_mapsAllFieldsCorrectly() {
        UpdateTraineeRequest request = buildUpdateRequest();

        Trainee actual = traineeMapper.toEntity(request);

        assertEquals("Abdul.Hariton", actual.getUser().getUsername());
        assertEquals("Abdul", actual.getUser().getFirstName());
        assertEquals("Hariton", actual.getUser().getLastName());
        assertEquals(LocalDate.of(1990, 1, 1), actual.getDateOfBirth());
        assertEquals("Lviv", actual.getAddress());
        assertFalse(actual.getUser().getIsActive());
    }

    @Test
    void toProfileResponse_mapsAllFieldsCorrectly() {
        Trainee trainee = buildTrainee();

        TraineeProfileResponse actual = traineeMapper.toProfileResponse(trainee);

        assertEquals(EXISTING_ID, actual.getId());
        assertEquals("Abdul", actual.getFirstName());
        assertEquals("Hariton", actual.getLastName());
        assertEquals("Abdul.Hariton", actual.getUsername());
        assertEquals(LocalDate.of(1990, 1, 1), actual.getDateOfBirth());
        assertEquals("Kyiv", actual.getAddress());
        assertTrue(actual.getIsActive());
    }

    @Test
    void toCreatedResponse_includesUsernameAndRawPassword() {
        Trainee trainee = buildTraineeWithRawPassword();

        TraineeCreatedResponse actual = traineeMapper.toCreatedResponse(trainee);

        assertEquals("Abdul.Hariton", actual.getUsername());
        assertEquals("rawPass123", actual.getPassword());
    }

    @Test
    void toAssignedTrainerInfo_mapsUserFieldsAndSpecialization() {
        Trainer trainer = buildTrainer();

        AssignedTrainerInfo actual = traineeMapper.toAssignedTrainerInfo(trainer);

        assertEquals("Mike.Tyson", actual.getUsername());
        assertEquals("Mike", actual.getFirstName());
        assertEquals("Tyson", actual.getLastName());
        assertEquals("BOXING", actual.getSpecialization().getTrainingTypeName());
    }

    @Test
    void toAssignedTrainerInfoList_mapsEachElement() {
        List<AssignedTrainerInfo> actual = traineeMapper.toAssignedTrainerInfoList(List.of(buildTrainer()));

        assertEquals(1, actual.size());
        assertEquals("Mike.Tyson", actual.get(0).getUsername());
    }

    @Test
    void toProfileResponse_neverExposesPassword() {
        assertThrows(NoSuchFieldException.class, () -> TraineeProfileResponse.class.getDeclaredField("password"));
    }

    @Test
    void toAssignedTrainerInfoList_nullInput_returnsNull() {
        assertNull(traineeMapper.toAssignedTrainerInfoList(null));
    }

    private CreateTraineeRequest buildCreateRequest() {
        return CreateTraineeRequest.builder()
                .firstName("Abdul")
                .lastName("Hariton")
                .dateOfBirth(LocalDate.of(1990, 1, 1))
                .address("Kyiv")
                .build();
    }

    private UpdateTraineeRequest buildUpdateRequest() {
        return UpdateTraineeRequest.builder()
                .firstName("Abdul")
                .lastName("Hariton")
                .dateOfBirth(LocalDate.of(1990, 1, 1))
                .address("Lviv")
                .isActive(false)
                .username("Abdul.Hariton")
                .build();
    }

    private Trainee buildTrainee() {
        User user = User.builder()
                .id(EXISTING_ID)
                .firstName("Abdul")
                .lastName("Hariton")
                .username("Abdul.Hariton")
                .password("hashedPassword")
                .isActive(true)
                .build();

        return Trainee.builder()
                .id(EXISTING_ID)
                .user(user)
                .dateOfBirth(LocalDate.of(1990, 1, 1))
                .address("Kyiv")
                .build();
    }

    private Trainee buildTraineeWithRawPassword() {
        User user = User.builder()
                .id(EXISTING_ID)
                .firstName("Abdul")
                .lastName("Hariton")
                .username("Abdul.Hariton")
                .password("hashedPassword")
                .rawPassword("rawPass123")
                .isActive(true)
                .build();

        return Trainee.builder()
                .id(EXISTING_ID)
                .user(user)
                .build();
    }

    private Trainer buildTrainer() {
        User user = User.builder()
                .id(2L)
                .firstName("Mike")
                .lastName("Tyson")
                .username("Mike.Tyson")
                .isActive(true)
                .build();

        return Trainer.builder()
                .id(2L)
                .user(user)
                .specialization(TrainingType.builder().trainingTypeName("BOXING").build())
                .build();
    }
}