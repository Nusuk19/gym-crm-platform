package com.gym.crm.core.mapper;

import com.gia.openapi.model.ActivationStatusRequest;
import com.gia.openapi.model.AssignedTrainerResponse;
import com.gia.openapi.model.GetTraineeTrainingResponse;
import com.gia.openapi.model.TraineeAssignedTrainersUpdateResponse;
import com.gia.openapi.model.TraineeCreateRequest;
import com.gia.openapi.model.TraineeCreateResponse;
import com.gia.openapi.model.TraineeGetResponse;
import com.gia.openapi.model.TraineeUpdateRequest;
import com.gia.openapi.model.TraineeUpdateResponse;
import com.gym.crm.core.dto.request.ActivationRequest;
import com.gym.crm.core.dto.request.CreateTraineeRequest;
import com.gym.crm.core.dto.request.UpdateTraineeRequest;
import com.gym.crm.core.dto.response.AssignedTrainerInfo;
import com.gym.crm.core.dto.response.TraineeCreatedResponse;
import com.gym.crm.core.dto.response.TraineeProfileResponse;
import com.gym.crm.core.dto.response.TrainingResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.math.BigDecimal;
import java.util.List;

@Mapper(componentModel = "spring")
public interface TraineeRestMapper {

    CreateTraineeRequest toCreateRequest(TraineeCreateRequest request);

    @Mapping(target = "username", source = "username")
    @Mapping(target = "firstName", source = "request.firstName")
    @Mapping(target = "lastName", source = "request.lastName")
    @Mapping(target = "dateOfBirth", source = "request.dateOfBirth")
    @Mapping(target = "address", source = "request.address")
    @Mapping(target = "isActive", source = "request.isActive")
    UpdateTraineeRequest toUpdateRequest(String username, TraineeUpdateRequest request);

    @Mapping(target = "username", source = "username")
    @Mapping(target = "isActive", source = "request.isActive")
    ActivationRequest toActivationRequest(String username, ActivationStatusRequest request);

    @Mapping(target = "username", source = "username")
    @Mapping(target = "password", source = "password")
    TraineeCreateResponse toCreateResponse(TraineeCreatedResponse response);

    @Mapping(target = "firstName", source = "firstName")
    @Mapping(target = "lastName", source = "lastName")
    @Mapping(target = "dateOfBirth", source = "dateOfBirth")
    @Mapping(target = "address", source = "address")
    @Mapping(target = "isActive", source = "isActive")
    @Mapping(target = "trainers", source = "trainers")
    TraineeGetResponse toGetResponse(TraineeProfileResponse response);

    @Mapping(target = "firstName", source = "firstName")
    @Mapping(target = "lastName", source = "lastName")
    @Mapping(target = "username", source = "username")
    @Mapping(target = "dateOfBirth", source = "dateOfBirth")
    @Mapping(target = "address", source = "address")
    @Mapping(target = "isActive", source = "isActive")
    @Mapping(target = "trainers", source = "trainers")
    TraineeUpdateResponse toUpdateResponse(TraineeProfileResponse response);

    @Mapping(target = "username", source = "username")
    @Mapping(target = "firstName", source = "firstName")
    @Mapping(target = "lastName", source = "lastName")
    @Mapping(target = "specialization", source = "specialization.trainingTypeName")
    AssignedTrainerResponse toAssignedTrainerResponse(AssignedTrainerInfo trainer);

    List<AssignedTrainerResponse> toAssignedTrainerResponseList(List<AssignedTrainerInfo> trainers);

    default TraineeAssignedTrainersUpdateResponse toAssignedTrainersUpdateResponse(List<AssignedTrainerInfo> trainers) {
        return new TraineeAssignedTrainersUpdateResponse()
                .trainers(toAssignedTrainerResponseList(trainers));
    }

    @Mapping(target = "trainingName", source = "trainingName")
    @Mapping(target = "trainingDate", source = "trainingDate")
    @Mapping(target = "trainingType", source = "trainingTypeName")
    @Mapping(target = "trainerName", source = "trainerUsername")
    @Mapping(target = "trainingDuration", source = "trainingDuration")
    GetTraineeTrainingResponse toTraineeTrainingResponse(TrainingResponse response);

    default Integer toMinutes(BigDecimal value) {
        return value == null ? null : value.intValue();
    }
}