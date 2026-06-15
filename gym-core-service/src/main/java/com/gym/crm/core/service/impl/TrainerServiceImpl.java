package com.gym.crm.core.service.impl;

import com.gym.crm.core.actuator.metrics.GymMetrics;
import com.gym.crm.core.dto.request.ActivationRequest;
import com.gym.crm.core.dto.request.ChangePasswordRequest;
import com.gym.crm.core.exception.EntityNotFoundException;
import com.gym.crm.core.model.Trainer;
import com.gym.crm.core.model.TrainingType;
import com.gym.crm.core.model.User;
import com.gym.crm.core.repository.TraineeRepository;
import com.gym.crm.core.repository.TrainerRepository;
import com.gym.crm.core.repository.TrainingTypeRepository;
import com.gym.crm.core.service.TrainerService;
import com.gym.crm.core.service.UserProfileService;
import com.gym.crm.core.service.UserService;
import com.gym.crm.core.service.common.EntityValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Slf4j
@RequiredArgsConstructor
@Service
public class TrainerServiceImpl implements TrainerService {

    private static final String USERNAME_BLANK_MSG = "Username cannot be blank";

    private final TrainerRepository trainerRepository;
    private final TraineeRepository traineeRepository;
    private final TrainingTypeRepository trainingTypeRepository;
    private final EntityValidator validator;
    private final UserProfileService userProfileService;
    private final UserService userService;
    private final GymMetrics gymMetrics;

    @Override
    @Transactional
    public Trainer create(Trainer trainer, String specializationName) {
        validator.validateTrainer(trainer);
        log.info("Creating trainer: firstName={}, lastName={}",
                trainer.getUser().getFirstName(), trainer.getUser().getLastName());

        TrainingType specialization = resolveSpecialization(specializationName);

        String username = userProfileService.generateUsername(trainer.getUser().getFirstName(), trainer.getUser().getLastName());
        String rawPassword = userProfileService.generatePassword();
        String hashedPassword = userProfileService.hashPassword(rawPassword);

        User userWithProfile = trainer.getUser().toBuilder()
                .username(username)
                .password(hashedPassword)
                .rawPassword(rawPassword)
                .isActive(Boolean.TRUE)
                .build();
        Trainer trainerWithProfile = trainer.toBuilder()
                .user(userWithProfile)
                .specialization(specialization)
                .build();

        validator.validateTrainer(trainerWithProfile);

        Trainer saved = trainerRepository.save(trainerWithProfile);
        gymMetrics.incrementTrainerRegistrations();

        return saved;
    }

    @Override
    @Transactional
    public Trainer update(Trainer trainer) {
        String username = trainer.getUser().getUsername();
        log.info("Updating trainer: username={}", username);
        validator.validateTrainer(trainer);

        Trainer existing = trainerRepository.findByUserUsername(username)
                .orElseThrow(() -> new EntityNotFoundException("Trainer not found: " + username));

        User mergedUser = trainer.getUser().toBuilder()
                .id(existing.getUser().getId())
                .username(existing.getUser().getUsername())
                .password(existing.getUser().getPassword())
                .build();

        Trainer merged = trainer.toBuilder()
                .id(existing.getId())
                .user(mergedUser)
                .specialization(existing.getSpecialization())
                .build();

        return trainerRepository.save(merged);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Trainer> findByUsername(String username) {
        validator.requireNonBlank(username, USERNAME_BLANK_MSG);
        log.debug("Looking up trainer by username={}", username);

        return trainerRepository.findByUserUsername(username);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Trainer> findAll() {
        return trainerRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Trainer> findAllNotAssignedToTrainee(String traineeUsername) {
        validator.requireNonBlank(traineeUsername, USERNAME_BLANK_MSG);

        traineeRepository.findByUserUsername(traineeUsername)
                .orElseThrow(() -> new EntityNotFoundException("Trainee not found: " + traineeUsername));

        return trainerRepository.findAllActiveNotAssignedToTrainee(traineeUsername);
    }

    @Override
    @Transactional
    public void changePassword(ChangePasswordRequest request) {
        requireTrainerByUsername(request.getUsername());

        userService.changePassword(request);
    }

    @Override
    @Transactional
    public void activate(ActivationRequest request) {
        requireTrainerByUsername(request.getUsername());

        userService.activate(request);
    }

    @Override
    @Transactional
    public void deactivate(ActivationRequest request) {
        requireTrainerByUsername(request.getUsername());

        userService.deactivate(request);
    }

    private TrainingType resolveSpecialization(String name) {
        validator.requireNonBlank(name, "Specialization name cannot be blank");

        return trainingTypeRepository.findByTrainingTypeName(name)
                .orElseThrow(() -> new EntityNotFoundException("Specialization not found: " + name));
    }

    private Trainer requireTrainerByUsername(String username) {
        validator.requireNonBlank(username, USERNAME_BLANK_MSG);

        return trainerRepository.findByUserUsername(username)
                .orElseThrow(() -> new EntityNotFoundException("Trainer not found: " + username));
    }
}
