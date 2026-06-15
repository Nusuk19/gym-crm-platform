package com.gym.crm.mapper;

import com.gia.openapi.model.ActivationStatusRequest;
import com.gia.openapi.model.AssignedTrainerResponse;
import com.gia.openapi.model.GetTraineeTrainingResponse;
import com.gia.openapi.model.TraineeAssignedTrainersUpdateResponse;
import com.gia.openapi.model.TraineeCreateRequest;
import com.gia.openapi.model.TraineeCreateResponse;
import com.gia.openapi.model.TraineeGetResponse;
import com.gia.openapi.model.TraineeUpdateRequest;
import com.gia.openapi.model.TraineeUpdateResponse;
import com.gym.crm.dto.request.ActivationRequest;
import com.gym.crm.dto.request.CreateTraineeRequest;
import com.gym.crm.dto.request.UpdateTraineeRequest;
import com.gym.crm.dto.response.AssignedTrainerInfo;
import com.gym.crm.dto.response.TraineeCreatedResponse;
import com.gym.crm.dto.response.TraineeProfileResponse;
import com.gym.crm.dto.response.TrainingResponse;
import com.gym.crm.model.TrainingType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TraineeRestMapperTest {

    private static final String USERNAME = "Abdul.Hariton";

    private TraineeRestMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = Mappers.getMapper(TraineeRestMapper.class);
    }

    @Test
    void toCreateRequest_mapsAllOpenApiFields() {
        TraineeCreateRequest openApi = buildCreateTraineeRequest();

        CreateTraineeRequest actual = mapper.toCreateRequest(openApi);

        assertEquals("Abdul", actual.getFirstName());
        assertEquals("Hariton", actual.getLastName());
        assertEquals(LocalDate.of(1990, 1, 1), actual.getDateOfBirth());
        assertEquals("Kyiv", actual.getAddress());
    }

    @Test
    void toUpdateRequest_combinesPathUsernameAndBodyFields() {
        TraineeUpdateRequest body = new TraineeUpdateRequest()
                .firstName("Abdul")
                .lastName("Hariton")
                .dateOfBirth(LocalDate.of(1990, 1, 1))
                .address("Lviv")
                .isActive(false);

        UpdateTraineeRequest actual = mapper.toUpdateRequest(USERNAME, body);

        assertEquals(USERNAME, actual.getUsername());
        assertEquals("Abdul", actual.getFirstName());
        assertEquals("Hariton", actual.getLastName());
        assertEquals(LocalDate.of(1990, 1, 1), actual.getDateOfBirth());
        assertEquals("Lviv", actual.getAddress());
        assertEquals(Boolean.FALSE, actual.getIsActive());
    }

    @Test
    void toActivationRequest_combinesPathUsernameAndBodyIsActive() {
        ActivationStatusRequest body = new ActivationStatusRequest(true);

        ActivationRequest actual = mapper.toActivationRequest(USERNAME, body);

        assertEquals(USERNAME, actual.getUsername());
        assertTrue(actual.getIsActive());
    }

    @Test
    void toCreateResponse_carriesUsernameAndPassword() {
        TraineeCreatedResponse created = TraineeCreatedResponse.builder()
                .username(USERNAME)
                .password("rawPass123")
                .build();

        TraineeCreateResponse actual = mapper.toCreateResponse(created);

        assertEquals(USERNAME, actual.getUsername());
        assertEquals("rawPass123", actual.getPassword());
    }

    @Test
    void toGetResponse_mapsAllProfileFieldsAndTrainers() {
        TraineeProfileResponse profile = buildProfile();

        TraineeGetResponse actual = mapper.toGetResponse(profile);

        assertEquals("Abdul", actual.getFirstName());
        assertEquals("Hariton", actual.getLastName());
        assertEquals(LocalDate.of(1990, 1, 1), actual.getDateOfBirth());
        assertEquals("Kyiv", actual.getAddress());
        assertEquals(Boolean.TRUE, actual.getIsActive());
        assertEquals(1, actual.getTrainers().size());
        assertEquals("Mike.Tyson", actual.getTrainers().get(0).getUsername());
        assertEquals("BOXING", actual.getTrainers().get(0).getSpecialization());
    }

    @Test
    void toUpdateResponse_mapsAllProfileFieldsAndTrainers() {
        TraineeProfileResponse profile = buildProfile();

        TraineeUpdateResponse actual = mapper.toUpdateResponse(profile);

        assertEquals(USERNAME, actual.getUsername());
        assertEquals("Abdul", actual.getFirstName());
        assertEquals("Hariton", actual.getLastName());
        assertEquals(LocalDate.of(1990, 1, 1), actual.getDateOfBirth());
        assertEquals("Kyiv", actual.getAddress());
        assertEquals(Boolean.TRUE, actual.getIsActive());
        assertEquals(1, actual.getTrainers().size());
    }

    @Test
    void toAssignedTrainerResponse_flattensSpecializationName() {
        AssignedTrainerInfo info = buildAssignedTrainerInfo();

        AssignedTrainerResponse actual = mapper.toAssignedTrainerResponse(info);

        assertEquals("Mike.Tyson", actual.getUsername());
        assertEquals("Mike", actual.getFirstName());
        assertEquals("Tyson", actual.getLastName());
        assertEquals("BOXING", actual.getSpecialization());
    }

    @Test
    void toAssignedTrainerResponse_nullSpecialization_resultsInNullSpecialization() {
        AssignedTrainerInfo info = buildTrainerInfoWithNullSpecialization();

        AssignedTrainerResponse actual = mapper.toAssignedTrainerResponse(info);

        assertNull(actual.getSpecialization());
    }

    @Test
    void toAssignedTrainersUpdateResponse_wrapsListInResponse() {
        AssignedTrainerInfo info = buildAssignedTrainerInfo();

        TraineeAssignedTrainersUpdateResponse actual =
                mapper.toAssignedTrainersUpdateResponse(List.of(info));

        assertEquals(1, actual.getTrainers().size());
        assertEquals("Mike.Tyson", actual.getTrainers().get(0).getUsername());
    }

    @Test
    void toAssignedTrainersUpdateResponse_emptyList_returnsResponseWithEmptyList() {
        TraineeAssignedTrainersUpdateResponse actual =
                mapper.toAssignedTrainersUpdateResponse(List.of());

        assertEquals(0, actual.getTrainers().size());
    }

    @Test
    void toTraineeTrainingResponse_mapsAllFieldsAndConvertsDuration() {
        TrainingResponse training = buildTrainingResponse();

        GetTraineeTrainingResponse actual = mapper.toTraineeTrainingResponse(training);

        assertEquals("Boxing basics", actual.getTrainingName());
        assertEquals(LocalDate.of(2024, 5, 1), actual.getTrainingDate());
        assertEquals("BOXING", actual.getTrainingType());
        assertEquals("Mike.Tyson", actual.getTrainerName());
        assertEquals(60, actual.getTrainingDuration());
    }

    @Test
    void toMinutes_nullInput_returnsNull() {
        assertNull(mapper.toMinutes(null));
    }

    @Test
    void toMinutes_truncatesFractionalPart() {
        assertEquals(60, mapper.toMinutes(BigDecimal.valueOf(60.9)));
    }

    private TraineeCreateRequest buildCreateTraineeRequest() {
        return new TraineeCreateRequest()
                .firstName("Abdul")
                .lastName("Hariton")
                .dateOfBirth(LocalDate.of(1990, 1, 1))
                .address("Kyiv");
    }

    private TraineeProfileResponse buildProfile() {
        AssignedTrainerInfo trainer = AssignedTrainerInfo.builder()
                .username("Mike.Tyson")
                .firstName("Mike")
                .lastName("Tyson")
                .specialization(TrainingType.builder().trainingTypeName("BOXING").build())
                .build();

        return TraineeProfileResponse.builder()
                .id(1L)
                .username(USERNAME)
                .firstName("Abdul")
                .lastName("Hariton")
                .dateOfBirth(LocalDate.of(1990, 1, 1))
                .address("Kyiv")
                .isActive(true)
                .trainers(List.of(trainer))
                .build();
    }

    private AssignedTrainerInfo buildTrainerInfoWithNullSpecialization() {
        return AssignedTrainerInfo.builder()
                .username("Mike.Tyson")
                .firstName("Mike")
                .lastName("Tyson")
                .specialization(null)
                .build();
    }

    private AssignedTrainerInfo buildAssignedTrainerInfo() {
        return AssignedTrainerInfo.builder()
                .username("Mike.Tyson")
                .firstName("Mike")
                .lastName("Tyson")
                .specialization(buildBoxingType())
                .build();
    }

    private TrainingType buildBoxingType() {
        return TrainingType.builder()
                .trainingTypeName("BOXING")
                .build();
    }

    private TrainingResponse buildTrainingResponse() {
        return TrainingResponse.builder()
                .trainingName("Boxing basics")
                .trainingDate(LocalDate.of(2024, 5, 1))
                .trainingTypeName("BOXING")
                .trainerUsername("Mike.Tyson")
                .trainingDuration(BigDecimal.valueOf(60))
                .build();
    }

}