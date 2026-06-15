package com.gym.crm.core.mapper;

import com.gym.crm.core.dto.request.CreateTraineeRequest;
import com.gym.crm.core.dto.request.UpdateTraineeRequest;
import com.gym.crm.core.dto.response.AssignedTrainerInfo;
import com.gym.crm.core.dto.response.TraineeCreatedResponse;
import com.gym.crm.core.dto.response.TraineeProfileResponse;
import com.gym.crm.core.model.Trainee;
import com.gym.crm.core.model.Trainer;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface TraineeMapper {

    @Mapping(target = "user.firstName", source = "firstName")
    @Mapping(target = "user.lastName", source = "lastName")
    @Mapping(target = "dateOfBirth", source = "dateOfBirth")
    @Mapping(target = "address", source = "address")
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user.isActive", ignore = true)
    @Mapping(target = "trainings", ignore = true)
    @Mapping(target = "trainers", ignore = true)
    Trainee toEntity(CreateTraineeRequest request);

    @Mapping(target = "user.username", source = "username")
    @Mapping(target = "user.firstName", source = "firstName")
    @Mapping(target = "user.lastName", source = "lastName")
    @Mapping(target = "user.isActive", source = "isActive")
    @Mapping(target = "dateOfBirth", source = "dateOfBirth")
    @Mapping(target = "address", source = "address")
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "trainings", ignore = true)
    @Mapping(target = "trainers", ignore = true)
    Trainee toEntity(UpdateTraineeRequest request);

    @Mapping(target = "username", source = "user.username")
    @Mapping(target = "password", source = "user.rawPassword")
    TraineeCreatedResponse toCreatedResponse(Trainee trainee);

    @Mapping(target = "id", source = "id")
    @Mapping(target = "username", source = "user.username")
    @Mapping(target = "firstName", source = "user.firstName")
    @Mapping(target = "lastName", source = "user.lastName")
    @Mapping(target = "isActive", source = "user.isActive")
    @Mapping(target = "dateOfBirth", source = "dateOfBirth")
    @Mapping(target = "address", source = "address")
    @Mapping(target = "trainers", source = "trainers")
    TraineeProfileResponse toProfileResponse(Trainee trainee);

    @Mapping(target = "username", source = "user.username")
    @Mapping(target = "firstName", source = "user.firstName")
    @Mapping(target = "lastName", source = "user.lastName")
    @Mapping(target = "specialization", source = "specialization")
    AssignedTrainerInfo toAssignedTrainerInfo(Trainer trainer);

    List<AssignedTrainerInfo> toAssignedTrainerInfoList(List<Trainer> trainers);
}