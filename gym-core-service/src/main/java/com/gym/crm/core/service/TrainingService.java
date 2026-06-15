package com.gym.crm.core.service;

import com.gym.crm.core.dao.search.filters.TraineeTrainingSearchFilter;
import com.gym.crm.core.dao.search.filters.TrainerTrainingSearchFilter;
import com.gym.crm.core.dto.request.CreateTrainingRequest;
import com.gym.crm.core.model.Training;

import java.util.List;

public interface TrainingService {
    Training create(CreateTrainingRequest request);

    List<Training> findAll();

    List<Training> findByTraineeCriteria(TraineeTrainingSearchFilter filter);

    List<Training> findByTrainerCriteria(TrainerTrainingSearchFilter filter);
}