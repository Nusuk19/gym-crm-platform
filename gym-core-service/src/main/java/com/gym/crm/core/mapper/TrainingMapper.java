package com.gym.crm.core.mapper;

import com.gym.crm.core.dto.request.CreateTrainingRequest;
import com.gym.crm.core.dto.response.TrainingResponse;
import com.gym.crm.core.model.Training;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface TrainingMapper {

    @Mapping(target = "name", source = "trainingName")
    @Mapping(target = "trainingDate", source = "trainingDate")
    @Mapping(target = "trainingDuration", source = "trainingDuration")
    @Mapping(target = "id", ignore = true)
    Training toEntity(CreateTrainingRequest request);

    @Mapping(target = "id", source = "id")
    @Mapping(target = "traineeId", source = "trainee.id")
    @Mapping(target = "trainerId", source = "trainer.id")
    @Mapping(target = "trainerUsername", source = "trainer.user.username")
    @Mapping(target = "traineeUsername", source = "trainee.user.username")
    @Mapping(target = "trainingName", source = "name")
    @Mapping(target = "trainingType", source = "trainingType")
    @Mapping(target = "trainingTypeName", source = "trainingType.trainingTypeName")
    @Mapping(target = "trainingDate", source = "trainingDate")
    @Mapping(target = "trainingDuration", source = "trainingDuration")
    TrainingResponse toResponse(Training training);
}