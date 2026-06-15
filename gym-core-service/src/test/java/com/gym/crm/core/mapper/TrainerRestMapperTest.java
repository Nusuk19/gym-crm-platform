package com.gym.crm.core.mapper;

import com.gia.openapi.model.ActivationStatusRequest;
import com.gia.openapi.model.AssignedTraineeResponse;
import com.gia.openapi.model.GetTrainerTrainingResponse;
import com.gia.openapi.model.TrainerCreateRequest;
import com.gia.openapi.model.TrainerCreateResponse;
import com.gia.openapi.model.TrainerGetResponse;
import com.gia.openapi.model.TrainerUpdateRequest;
import com.gia.openapi.model.TrainerUpdateResponse;
import com.gym.crm.core.dto.request.ActivationRequest;
import com.gym.crm.core.dto.request.CreateTrainerRequest;
import com.gym.crm.core.dto.request.UpdateTrainerRequest;
import com.gym.crm.core.dto.response.AssignedTraineeInfo;
import com.gym.crm.core.dto.response.TrainerCreatedResponse;
import com.gym.crm.core.dto.response.TrainerProfileResponse;
import com.gym.crm.core.dto.response.TrainingResponse;
import com.gym.crm.core.mapper.TrainerRestMapper;
import com.gym.crm.core.model.TrainingType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class TrainerRestMapperTest {

    private static final String USERNAME = "Mike.Tyson";

    private TrainerRestMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = Mappers.getMapper(TrainerRestMapper.class);
    }

    @Test
    void toCreateRequest_mapsSpecializationField() {
        TrainerCreateRequest openApi = new TrainerCreateRequest()
                .firstName("Mike")
                .lastName("Tyson")
                .specialization("BOXING");

        CreateTrainerRequest actual = mapper.toCreateRequest(openApi);

        assertEquals("Mike", actual.getFirstName());
        assertEquals("Tyson", actual.getLastName());
        assertEquals("BOXING", actual.getSpecializationName());
    }

    @Test
    void toUpdateRequest_combinesPathUsernameAndBodyFields() {
        TrainerUpdateRequest body = new TrainerUpdateRequest()
                .firstName("Mike")
                .lastName("Tyson")
                .isActive(true);

        UpdateTrainerRequest actual = mapper.toUpdateRequest(USERNAME, body);

        assertEquals(USERNAME, actual.getUsername());
        assertEquals("Mike", actual.getFirstName());
        assertEquals("Tyson", actual.getLastName());
        assertEquals(Boolean.TRUE, actual.getIsActive());
    }

    @Test
    void toActivationRequest_combinesPathUsernameAndBodyIsActive() {
        ActivationStatusRequest body = new ActivationStatusRequest(false);

        ActivationRequest actual = mapper.toActivationRequest(USERNAME, body);

        assertEquals(USERNAME, actual.getUsername());
        assertEquals(Boolean.FALSE, actual.getIsActive());
    }

    @Test
    void toCreateResponse_carriesUsernameAndPassword() {
        TrainerCreatedResponse created = TrainerCreatedResponse.builder()
                .username(USERNAME)
                .password("rawPass123")
                .build();

        TrainerCreateResponse actual = mapper.toCreateResponse(created);

        assertEquals(USERNAME, actual.getUsername());
        assertEquals("rawPass123", actual.getPassword());
    }

    @Test
    void toGetResponse_mapsAllProfileFieldsAndTrainees() {
        TrainerProfileResponse profile = buildProfile();

        TrainerGetResponse actual = mapper.toGetResponse(profile);

        assertEquals("Mike", actual.getFirstName());
        assertEquals("Tyson", actual.getLastName());
        assertEquals("BOXING", actual.getSpecialization());
        assertEquals(Boolean.TRUE, actual.getIsActive());
        assertEquals(1, actual.getTrainees().size());
        assertEquals("Abdul.Hariton", actual.getTrainees().get(0).getUsername());
    }

    @Test
    void toUpdateResponse_mapsAllProfileFieldsAndTrainees() {
        TrainerProfileResponse profile = buildProfile();

        TrainerUpdateResponse actual = mapper.toUpdateResponse(profile);

        assertEquals(USERNAME, actual.getUsername());
        assertEquals("Mike", actual.getFirstName());
        assertEquals("Tyson", actual.getLastName());
        assertEquals("BOXING", actual.getSpecialization());
        assertEquals(Boolean.TRUE, actual.getIsActive());
        assertEquals(1, actual.getTrainees().size());
    }

    @Test
    void toAssignedTraineeResponse_mapsAllFields() {
        AssignedTraineeInfo info = buildAssignedTraineeInfo();

        AssignedTraineeResponse actual = mapper.toAssignedTraineeResponse(info);

        assertEquals("Abdul.Hariton", actual.getUsername());
        assertEquals("Abdul", actual.getFirstName());
        assertEquals("Hariton", actual.getLastName());
    }

    @Test
    void toTrainerTrainingResponse_mapsAllFieldsAndConvertsDuration() {
        TrainingResponse training = buildTrainingResponse();

        GetTrainerTrainingResponse actual = mapper.toTrainerTrainingResponse(training);

        assertEquals("Boxing basics", actual.getTrainingName());
        assertEquals(LocalDate.of(2024, 5, 1), actual.getTrainingDate());
        assertEquals("BOXING", actual.getTrainingType());
        assertEquals("Abdul.Hariton", actual.getTraineeName());
        assertEquals(90, actual.getTrainingDuration());
    }

    @Test
    void toMinutes_nullInput_returnsNull() {
        assertNull(mapper.toMinutes(null));
    }

    @Test
    void toMinutes_truncatesFractionalPart() {
        assertEquals(90, mapper.toMinutes(BigDecimal.valueOf(90.9)));
    }

    private TrainerProfileResponse buildProfile() {
        AssignedTraineeInfo trainee = AssignedTraineeInfo.builder()
                .username("Abdul.Hariton")
                .firstName("Abdul")
                .lastName("Hariton")
                .build();

        return TrainerProfileResponse.builder()
                .id(1L)
                .username(USERNAME)
                .firstName("Mike")
                .lastName("Tyson")
                .specialization(TrainingType.builder().trainingTypeName("BOXING").build())
                .isActive(true)
                .trainees(List.of(trainee))
                .build();
    }

    private AssignedTraineeInfo buildAssignedTraineeInfo() {
        return AssignedTraineeInfo.builder()
                .username("Abdul.Hariton")
                .firstName("Abdul")
                .lastName("Hariton")
                .build();
    }

    private TrainingResponse buildTrainingResponse() {
        return TrainingResponse.builder()
                .trainingName("Boxing basics")
                .trainingDate(LocalDate.of(2024, 5, 1))
                .trainingTypeName("BOXING")
                .traineeUsername("Abdul.Hariton")
                .trainingDuration(BigDecimal.valueOf(90))
                .build();
    }
}