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
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.math.BigDecimal;

@Mapper(componentModel = "spring")
public interface TrainerRestMapper {

    @Mapping(target = "specializationName", source = "specialization")
    CreateTrainerRequest toCreateRequest(TrainerCreateRequest request);

    @Mapping(target = "username", source = "username")
    @Mapping(target = "firstName", source = "request.firstName")
    @Mapping(target = "lastName", source = "request.lastName")
    @Mapping(target = "isActive", source = "request.isActive")
    UpdateTrainerRequest toUpdateRequest(String username, TrainerUpdateRequest request);

    @Mapping(target = "username", source = "username")
    @Mapping(target = "isActive", source = "request.isActive")
    ActivationRequest toActivationRequest(String username, ActivationStatusRequest request);

    TrainerCreateResponse toCreateResponse(TrainerCreatedResponse response);

    @Mapping(target = "firstName", source = "firstName")
    @Mapping(target = "lastName", source = "lastName")
    @Mapping(target = "specialization", source = "specialization.trainingTypeName")
    @Mapping(target = "isActive", source = "isActive")
    @Mapping(target = "trainees", source = "trainees")
    TrainerGetResponse toGetResponse(TrainerProfileResponse response);

    @Mapping(target = "username", source = "username")
    @Mapping(target = "firstName", source = "firstName")
    @Mapping(target = "lastName", source = "lastName")
    @Mapping(target = "specialization", source = "specialization.trainingTypeName")
    @Mapping(target = "isActive", source = "isActive")
    @Mapping(target = "trainees", source = "trainees")
    TrainerUpdateResponse toUpdateResponse(TrainerProfileResponse response);

    AssignedTraineeResponse toAssignedTraineeResponse(AssignedTraineeInfo info);

    @Mapping(target = "trainingName", source = "trainingName")
    @Mapping(target = "trainingDate", source = "trainingDate")
    @Mapping(target = "trainingType", source = "trainingTypeName")
    @Mapping(target = "traineeName", source = "traineeUsername")
    @Mapping(target = "trainingDuration", source = "trainingDuration")
    GetTrainerTrainingResponse toTrainerTrainingResponse(TrainingResponse response);

    default Integer toMinutes(BigDecimal value) {
        return value == null ? null : value.intValue();
    }
}