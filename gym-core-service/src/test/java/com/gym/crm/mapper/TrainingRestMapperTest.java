package com.gym.crm.mapper;

import com.gia.openapi.model.TrainingCreateRequest;
import com.gia.openapi.model.TrainingTypeResponse;
import com.gym.crm.dto.request.CreateTrainingRequest;
import com.gym.crm.model.TrainingType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class TrainingRestMapperTest {

    private TrainingRestMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = Mappers.getMapper(TrainingRestMapper.class);
    }

    @Test
    void toCreateRequest_mapsAllFields() {
        TrainingCreateRequest request = buildCreateRequest(60);

        CreateTrainingRequest actual = mapper.toCreateRequest(request);

        assertEquals("Abdul.Hariton", actual.getTraineeUsername());
        assertEquals("Mike.Tyson", actual.getTrainerUsername());
        assertEquals("Boxing basics", actual.getTrainingName());
        assertEquals(LocalDate.of(2024, 5, 1), actual.getTrainingDate());
        assertEquals(BigDecimal.valueOf(60), actual.getTrainingDuration());
    }

    @Test
    void toCreateRequest_nullDuration_mapsToNull() {
        TrainingCreateRequest request = buildCreateRequest(null);

        CreateTrainingRequest actual = mapper.toCreateRequest(request);

        assertNull(actual.getTrainingDuration());
    }

    @Test
    void toTrainingTypeResponse_mapsIdAndName() {
        TrainingType type = buildTrainingType(1L, "BOXING");

        TrainingTypeResponse actual = mapper.toTrainingTypeResponse(type);

        assertEquals(1, actual.getId());
        assertEquals("BOXING", actual.getName());
    }

    @Test
    void toTrainingTypeResponse_nullId_mapsToNull() {
        TrainingType type = buildTrainingType(null, "BOXING");

        TrainingTypeResponse actual = mapper.toTrainingTypeResponse(type);

        assertNull(actual.getId());
        assertEquals("BOXING", actual.getName());
    }

    @Test
    void toTrainingTypeResponseList_mapsAllElements() {
        List<TrainingType> types = List.of(
                buildTrainingType(1L, "BOXING"),
                buildTrainingType(2L, "CARDIO"));

        List<TrainingTypeResponse> actual = mapper.toTrainingTypeResponseList(types);

        assertEquals(2, actual.size());
        assertEquals(1, actual.get(0).getId());
        assertEquals("BOXING", actual.get(0).getName());
        assertEquals(2, actual.get(1).getId());
        assertEquals("CARDIO", actual.get(1).getName());
    }

    @Test
    void toTrainingTypeResponseList_emptyList_returnsEmptyList() {
        List<TrainingTypeResponse> actual = mapper.toTrainingTypeResponseList(List.of());

        assertEquals(0, actual.size());
    }

    @Test
    void toBigDecimal_nullInput_returnsNull() {
        assertNull(mapper.toBigDecimal(null));
    }

    @Test
    void toBigDecimal_validInput_convertsCorrectly() {
        assertEquals(BigDecimal.valueOf(90), mapper.toBigDecimal(90));
    }

    private TrainingCreateRequest buildCreateRequest(Integer duration) {
        return new TrainingCreateRequest()
                .traineeUsername("Abdul.Hariton")
                .trainerUsername("Mike.Tyson")
                .trainingName("Boxing basics")
                .trainingDate(LocalDate.of(2024, 5, 1))
                .trainingDuration(duration);
    }

    private TrainingType buildTrainingType(Long id, String name) {
        return TrainingType.builder()
                .id(id)
                .trainingTypeName(name)
                .build();
    }
}