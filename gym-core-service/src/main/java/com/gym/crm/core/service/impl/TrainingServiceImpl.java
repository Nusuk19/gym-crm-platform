package com.gym.crm.core.service.impl;

import com.gym.crm.core.actuator.metrics.GymMetrics;
import com.gym.crm.core.dao.search.filters.TraineeTrainingSearchFilter;
import com.gym.crm.core.dao.search.filters.TrainerTrainingSearchFilter;
import com.gym.crm.core.dto.request.CreateTrainingRequest;
import com.gym.crm.core.exception.EntityNotFoundException;
import com.gym.crm.core.model.Trainee;
import com.gym.crm.core.model.Trainer;
import com.gym.crm.core.model.Training;
import com.gym.crm.core.repository.TraineeRepository;
import com.gym.crm.core.repository.TrainerRepository;
import com.gym.crm.core.repository.TrainingRepository;
import com.gym.crm.core.repository.specification.TrainingSpecifications;
import com.gym.crm.core.service.TrainingService;
import com.gym.crm.core.service.common.EntityValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Service
public class TrainingServiceImpl implements TrainingService {

    private final TrainingRepository trainingRepository;
    private final TraineeRepository traineeRepository;
    private final TrainerRepository trainerRepository;
    private final EntityValidator validator;
    private final GymMetrics gymMetrics;

    @Override
    @Transactional
    public Training create(CreateTrainingRequest request) {
        log.info("Creating training: name={}, traineeUsername={}, trainerUsername={}",
                request.getTrainingName(), request.getTraineeUsername(), request.getTrainerUsername());

        Trainee trainee = traineeRepository.findByUserUsername(request.getTraineeUsername())
                .orElseThrow(() -> new EntityNotFoundException("Trainee not found: " + request.getTraineeUsername()));
        Trainer trainer = trainerRepository.findByUserUsername(request.getTrainerUsername())
                .orElseThrow(() -> new EntityNotFoundException("Trainer not found: " + request.getTrainerUsername()));

        Training training = Training.builder()
                .name(request.getTrainingName())
                .trainee(trainee)
                .trainer(trainer)
                .trainingType(trainer.getSpecialization())
                .trainingDate(request.getTrainingDate())
                .trainingDuration(request.getTrainingDuration())
                .build();

        validator.validateTraining(training);

        Training saved = trainingRepository.save(training);
        gymMetrics.incrementTrainingsCreated();

        return saved;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Training> findAll() {
        return trainingRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Training> findByTraineeCriteria(TraineeTrainingSearchFilter filter) {
        validator.requireNonNull(filter, "Search filter cannot be null");
        log.debug("Searching trainings by trainee criteria: {}", filter);

        return trainingRepository.findAll(TrainingSpecifications.forTraineeCriteria(filter));
    }

    @Override
    @Transactional(readOnly = true)
    public List<Training> findByTrainerCriteria(TrainerTrainingSearchFilter filter) {
        validator.requireNonNull(filter, "Search filter cannot be null");
        log.debug("Searching trainings by trainer criteria: {}", filter);

        return trainingRepository.findAll(TrainingSpecifications.forTrainerCriteria(filter));
    }
}