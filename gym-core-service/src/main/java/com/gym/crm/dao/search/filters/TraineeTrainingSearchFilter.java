package com.gym.crm.dao.search.filters;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
public class TraineeTrainingSearchFilter extends TrainingSearchFilter {

    private String trainerFullName;

    private String trainingTypeName;
}