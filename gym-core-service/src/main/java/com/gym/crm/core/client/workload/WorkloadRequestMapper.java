package com.gym.crm.core.client.workload;

import com.gym.crm.core.client.workload.model.ActionType;
import com.gym.crm.core.client.workload.model.TrainerWorkloadRequest;
import com.gym.crm.core.model.Training;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.math.BigDecimal;

@Mapper(componentModel = "spring")
public interface WorkloadRequestMapper {

    @Mapping(target = "trainerUsername", source = "training.trainer.user.username")
    @Mapping(target = "trainerFirstName", source = "training.trainer.user.firstName")
    @Mapping(target = "trainerLastName", source = "training.trainer.user.lastName")
    @Mapping(target = "isActive", source = "training.trainer.user.isActive")
    @Mapping(target = "trainingDate", source = "training.trainingDate")
    @Mapping(target = "trainingDuration", source = "training.trainingDuration")
    @Mapping(target = "actionType", source = "actionType")
    TrainerWorkloadRequest toRequest(Training training, ActionType actionType);

    default Integer bigDecimalToInteger(BigDecimal value) {
        return value == null ? null : value.intValue();
    }
}