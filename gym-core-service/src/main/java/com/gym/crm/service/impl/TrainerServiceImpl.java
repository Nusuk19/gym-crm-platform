package com.gym.crm.service.impl;

import com.gym.crm.actuator.metrics.GymMetrics;
import com.gym.crm.dto.request.ActivationRequest;
import com.gym.crm.dto.request.ChangePasswordRequest;
import com.gym.crm.exception.EntityNotFoundException;
import com.gym.crm.model.Trainer;
import com.gym.crm.model.TrainingType;
import com.gym.crm.model.User;
import com.gym.crm.repository.TraineeRepository;
import com.gym.crm.repository.TrainerRepository;
import com.gym.crm.repository.TrainingTypeRepository;
import com.gym.crm.service.TrainerService;
import com.gym.crm.service.UserProfileService;
import com.gym.crm.service.UserService;
import com.gym.crm.service.common.EntityValidator;
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
