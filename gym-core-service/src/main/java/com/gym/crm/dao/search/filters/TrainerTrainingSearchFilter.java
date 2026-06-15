package com.gym.crm.dao.search.filters;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
public class TrainerTrainingSearchFilter extends TrainingSearchFilter {
    private String traineeFullName;
}