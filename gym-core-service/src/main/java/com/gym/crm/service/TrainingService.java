package com.gym.crm.service;

import com.gym.crm.dao.search.filters.TraineeTrainingSearchFilter;
import com.gym.crm.dao.search.filters.TrainerTrainingSearchFilter;
import com.gym.crm.dto.request.CreateTrainingRequest;
import com.gym.crm.model.Training;

import java.util.List;

public interface TrainingService {
    Training create(CreateTrainingRequest request);

    List<Training> findAll();

    List<Training> findByTraineeCriteria(TraineeTrainingSearchFilter filter);

    List<Training> findByTrainerCriteria(TrainerTrainingSearchFilter filter);
}