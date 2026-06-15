package com.gym.crm.mapper;

import com.gia.openapi.model.TrainingCreateRequest;
import com.gia.openapi.model.TrainingTypeResponse;
import com.gym.crm.dto.request.CreateTrainingRequest;
import com.gym.crm.model.TrainingType;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.math.BigDecimal;
import java.util.List;

@Mapper(componentModel = "spring")
public interface TrainingRestMapper {

    @Mapping(target = "traineeUsername", source = "traineeUsername")
    @Mapping(target = "trainerUsername", source = "trainerUsername")
    @Mapping(target = "trainingName", source = "trainingName")
    @Mapping(target = "trainingDate", source = "trainingDate")
    @Mapping(target = "trainingDuration", source = "trainingDuration")
    CreateTrainingRequest toCreateRequest(TrainingCreateRequest request);

    @Mapping(target = "id", source = "id")
    @Mapping(target = "name", source = "trainingTypeName")
    TrainingTypeResponse toTrainingTypeResponse(TrainingType type);

    List<TrainingTypeResponse> toTrainingTypeResponseList(List<TrainingType> types);

    default BigDecimal toBigDecimal(Integer minutes) {
        return minutes == null ? null : BigDecimal.valueOf(minutes);
    }
}