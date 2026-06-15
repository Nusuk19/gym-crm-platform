package com.gym.crm.mapper;

import com.gym.crm.dto.request.CreateTrainerRequest;
import com.gym.crm.dto.request.UpdateTrainerRequest;
import com.gym.crm.dto.response.AssignedTraineeInfo;
import com.gym.crm.dto.response.TrainerCreatedResponse;
import com.gym.crm.dto.response.TrainerProfileResponse;
import com.gym.crm.model.Trainee;
import com.gym.crm.model.Trainer;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface TrainerMapper {

    @Mapping(target = "user.firstName", source = "firstName")
    @Mapping(target = "user.lastName", source = "lastName")
    @Mapping(target = "specialization", ignore = true)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user.isActive", ignore = true)
    @Mapping(target = "trainings", ignore = true)
    @Mapping(target = "trainees", ignore = true)
    Trainer toEntity(CreateTrainerRequest request);

    @Mapping(target = "user.username", source = "username")
    @Mapping(target = "user.firstName", source = "firstName")
    @Mapping(target = "user.lastName", source = "lastName")
    @Mapping(target = "user.isActive", source = "isActive")
    @Mapping(target = "specialization", ignore = true)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "trainings", ignore = true)
    @Mapping(target = "trainees", ignore = true)
    Trainer toEntity(UpdateTrainerRequest request);

    @Mapping(target = "username", source = "user.username")
    @Mapping(target = "password", source = "user.rawPassword")
    TrainerCreatedResponse toCreatedResponse(Trainer trainer);

    @Mapping(target = "id", source = "id")
    @Mapping(target = "username", source = "user.username")
    @Mapping(target = "firstName", source = "user.firstName")
    @Mapping(target = "lastName", source = "user.lastName")
    @Mapping(target = "isActive", source = "user.isActive")
    @Mapping(target = "specialization", source = "specialization")
    @Mapping(target = "trainees", source = "trainees")
    TrainerProfileResponse toProfileResponse(Trainer trainer);

    @Mapping(target = "username", source = "user.username")
    @Mapping(target = "firstName", source = "user.firstName")
    @Mapping(target = "lastName", source = "user.lastName")
    AssignedTraineeInfo toAssignedTraineeInfo(Trainee trainee);

    List<AssignedTraineeInfo> toAssignedTraineeInfoList(List<Trainee> trainees);
}