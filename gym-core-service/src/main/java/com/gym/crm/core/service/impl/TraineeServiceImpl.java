package com.gym.crm.core.service.impl;

import com.gym.crm.core.actuator.metrics.GymMetrics;
import com.gym.crm.core.dto.request.ActivationRequest;
import com.gym.crm.core.dto.request.ChangePasswordRequest;
import com.gym.crm.core.exception.EntityNotFoundException;
import com.gym.crm.core.messaging.workload.ActionType;
import com.gym.crm.core.messaging.workload.TrainerWorkloadMessage;
import com.gym.crm.core.messaging.workload.WorkloadMessageMapper;
import com.gym.crm.core.messaging.workload.WorkloadUpdateEvent;
import com.gym.crm.core.model.Trainee;
import com.gym.crm.core.model.Trainer;
import com.gym.crm.core.model.User;
import com.gym.crm.core.repository.TraineeRepository;
import com.gym.crm.core.repository.TrainerRepository;
import com.gym.crm.core.service.TraineeService;
import com.gym.crm.core.service.UserProfileService;
import com.gym.crm.core.service.UserService;
import com.gym.crm.core.service.common.EntityValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Slf4j
@RequiredArgsConstructor
@Service
public class TraineeServiceImpl implements TraineeService {

    private static final String USERNAME_BLANK_MSG = "Username cannot be blank";
    private static final String TRAINEE_NOT_FOUND = "Trainee not found: ";

    private final TraineeRepository traineeRepository;
    private final TrainerRepository trainerRepository;
    private final EntityValidator validator;
    private final UserProfileService userProfileService;
    private final UserService userService;
    private final GymMetrics gymMetrics;
    private final ApplicationEventPublisher publisher;
    private final WorkloadMessageMapper workloadMessageMapper;

    @Override
    @Transactional
    public Trainee create(Trainee trainee) {
        validator.validateTrainee(trainee);
        log.info("Creating trainee: firstName={}, lastName={}",
                trainee.getUser().getFirstName(), trainee.getUser().getLastName());

        String username = userProfileService.generateUsername(trainee.getUser().getFirstName(), trainee.getUser().getLastName());
        String rawPassword = userProfileService.generatePassword();
        String hashedPassword = userProfileService.hashPassword(rawPassword);

        User userWithProfile = trainee.getUser().toBuilder()
                .username(username)
                .password(hashedPassword)
                .rawPassword(rawPassword)
                .isActive(Boolean.TRUE)
                .build();
        Trainee traineeWithProfile = trainee.toBuilder()
                .user(userWithProfile)
                .build();

        Trainee saved = traineeRepository.save(traineeWithProfile);
        gymMetrics.incrementTraineeRegistrations();

        return saved;
    }

    @Override
    @Transactional
    public Trainee update(Trainee trainee) {
        String username = trainee.getUser().getUsername();
        log.info("Updating trainee: username={}", username);
        validator.validateTrainee(trainee);

        Trainee existing = traineeRepository.findByUserUsername(username)
                .orElseThrow(() -> new EntityNotFoundException(TRAINEE_NOT_FOUND + username));

        User mergedUser = trainee.getUser().toBuilder()
                .id(existing.getUser().getId())
                .username(existing.getUser().getUsername())
                .password(existing.getUser().getPassword())
                .build();

        Trainee merged = trainee.toBuilder()
                .id(existing.getId())
                .user(mergedUser)
                .build();

        return traineeRepository.save(merged);
    }

    @Override
    @Transactional
    public List<Trainer> updateTrainers(String traineeUsername, List<String> trainerUsernames) {
        validator.requireNonBlank(traineeUsername, USERNAME_BLANK_MSG);
        validator.requireNonNull(trainerUsernames, "Trainer usernames list cannot be null");

        log.info("Updating trainers list for trainee");

        Trainee trainee = traineeRepository.findByUserUsername(traineeUsername)
                .orElseThrow(() -> new EntityNotFoundException(TRAINEE_NOT_FOUND + traineeUsername));
        List<Trainer> newTrainers = trainerRepository.findAllByUserUsernameIn(trainerUsernames);

        trainee.getTrainers().clear();
        trainee.getTrainers().addAll(newTrainers);

        log.info("Updated trainers list for trainee: username={}, trainers={}", traineeUsername, trainerUsernames);

        return trainee.getTrainers();
    }

    @Override
    @Transactional
    public void deleteByUsername(String username) {
        log.info("Deleting trainee by username");
        validator.requireNonBlank(username, USERNAME_BLANK_MSG);

        Trainee trainee = traineeRepository.findByUserUsername(username)
                .orElseThrow(() -> new EntityNotFoundException(TRAINEE_NOT_FOUND + username));
        List<TrainerWorkloadMessage> messages = trainee.getTrainings().stream()
                .map(training -> workloadMessageMapper.toMessage(training, ActionType.DELETE))
                .toList();

        traineeRepository.delete(trainee);
        publisher.publishEvent(new WorkloadUpdateEvent(messages));

        log.info("Trainee deleted: username={}, workload events published: {}", username, messages.size());
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Trainee> findByUsername(String username) {
        validator.requireNonBlank(username, USERNAME_BLANK_MSG);
        log.debug("Looking up trainee by username={}", username);

        return traineeRepository.findByUserUsername(username);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Trainee> findAll() {
        return traineeRepository.findAll();
    }

    @Override
    @Transactional
    public void changePassword(ChangePasswordRequest request) {
        requireTraineeByUsername(request.getUsername());

        userService.changePassword(request);
    }

    @Override
    @Transactional
    public void activate(ActivationRequest request) {
        requireTraineeByUsername(request.getUsername());

        userService.activate(request);
    }

    @Override
    @Transactional
    public void deactivate(ActivationRequest request) {
        requireTraineeByUsername(request.getUsername());

        userService.deactivate(request);
    }

    private Trainee requireTraineeByUsername(String username) {
        validator.requireNonBlank(username, USERNAME_BLANK_MSG);

        return traineeRepository.findByUserUsername(username)
                .orElseThrow(() -> new EntityNotFoundException(TRAINEE_NOT_FOUND + username));
    }
}