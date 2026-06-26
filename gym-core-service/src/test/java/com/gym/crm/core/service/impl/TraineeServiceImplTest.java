package com.gym.crm.core.service.impl;

import com.gym.crm.core.actuator.metrics.GymMetrics;
import com.gym.crm.core.dto.request.ActivationRequest;
import com.gym.crm.core.dto.request.ChangePasswordRequest;
import com.gym.crm.core.exception.EntityNotFoundException;
import com.gym.crm.core.exception.EntityValidationException;
import com.gym.crm.core.messaging.workload.ActionType;
import com.gym.crm.core.messaging.workload.TrainerWorkloadMessage;
import com.gym.crm.core.messaging.workload.WorkloadMessageMapper;
import com.gym.crm.core.messaging.workload.WorkloadUpdateEvent;
import com.gym.crm.core.model.Trainee;
import com.gym.crm.core.model.Trainer;
import com.gym.crm.core.model.Training;
import com.gym.crm.core.model.User;
import com.gym.crm.core.repository.TraineeRepository;
import com.gym.crm.core.repository.TrainerRepository;
import com.gym.crm.core.service.UserProfileService;
import com.gym.crm.core.service.UserService;
import com.gym.crm.core.service.common.EntityValidator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TraineeServiceImplTest {

    private static final Long ID = 1L;
    private static final String USERNAME = "Abdul.Hariton";
    private static final String FIRST_NAME = "Abdul";
    private static final String LAST_NAME = "Hariton";
    private static final int YEAR = 1990;
    private static final int MONTH = 1;
    private static final int DAY = 1;

    @Mock
    private TraineeRepository traineeRepository;
    @Mock
    private TrainerRepository trainerRepository;
    @Mock
    private EntityValidator validator;
    @Mock
    private UserProfileService userProfileService;
    @Mock
    private UserService userService;
    @Mock
    private GymMetrics gymMetrics;
    @Mock
    private WorkloadMessageMapper workloadMessageMapper;
    @Mock
    private ApplicationEventPublisher publisher;

    @InjectMocks
    private TraineeServiceImpl service;

    @Test
    void create_whenValidTrainee_savesWithGeneratedProfile() {
        Trainee trainee = buildTrainee();
        when(userProfileService.generateUsername(FIRST_NAME, LAST_NAME)).thenReturn(USERNAME);
        when(userProfileService.generatePassword()).thenReturn("rawPass123");
        when(userProfileService.hashPassword("rawPass123")).thenReturn("hashedPass");
        when(traineeRepository.save(any(Trainee.class))).thenAnswer(inv -> inv.getArgument(0));

        Trainee actual = service.create(trainee);

        assertThat(actual.getUser().getUsername()).isEqualTo(USERNAME);
        assertThat(actual.getUser().getPassword()).isEqualTo("hashedPass");
        verify(validator).validateTrainee(trainee);
        verify(userProfileService).generateUsername(FIRST_NAME, LAST_NAME);
        verify(userProfileService).generatePassword();
        verify(userProfileService).hashPassword("rawPass123");
        verify(traineeRepository).save(any(Trainee.class));
        verify(gymMetrics).incrementTraineeRegistrations();
    }

    @Test
    void create_whenValidationFails_throwsException() {
        doThrow(new EntityValidationException("Trainee cannot be null")).when(validator).validateTrainee(any());

        assertThrows(EntityValidationException.class, () -> service.create(null));
        verify(traineeRepository, never()).save(any());
        verify(gymMetrics, never()).incrementTraineeRegistrations();
    }

    @Test
    void update_whenValidTrainee_updatesSuccessfully() {
        Trainee trainee = buildTrainee();
        Trainee existing = trainee.toBuilder()
                .user(trainee.getUser().toBuilder()
                        .id(ID)
                        .username(USERNAME)
                        .password("existingHash")
                        .build())
                .build();
        when(traineeRepository.findByUserUsername(USERNAME)).thenReturn(Optional.of(existing));
        when(traineeRepository.save(any(Trainee.class))).thenAnswer(inv -> inv.getArgument(0));

        Trainee actual = service.update(trainee);

        assertThat(actual).isEqualTo(trainee);
        verify(validator).validateTrainee(trainee);
        verify(traineeRepository).findByUserUsername(USERNAME);
        verify(traineeRepository).save(any(Trainee.class));
    }

    @Test
    void update_whenTraineeNotFound_throwsEntityNotFoundException() {
        Trainee trainee = buildTrainee();
        when(traineeRepository.findByUserUsername(USERNAME)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> service.update(trainee));
        verify(traineeRepository, never()).save(any());
    }

    @Test
    void deleteByUsername_whenTraineeHasTrainings_publishesWorkloadEvents() {
        Training training = buildTraining();
        Trainee trainee = buildTrainee().toBuilder()
                .trainings(new ArrayList<>(List.of(training)))
                .build();
        TrainerWorkloadMessage expected = buildWorkloadMessage();
        when(traineeRepository.findByUserUsername(USERNAME)).thenReturn(Optional.of(trainee));
        when(workloadMessageMapper.toMessage(training, ActionType.DELETE)).thenReturn(expected);

        service.deleteByUsername(USERNAME);

        verify(validator).requireNonBlank(USERNAME, "Username cannot be blank");
        verify(traineeRepository).findByUserUsername(USERNAME);
        verify(traineeRepository).delete(trainee);
        verify(publisher).publishEvent(new WorkloadUpdateEvent(List.of(expected)));
    }

    @Test
    void deleteByUsername_whenTraineeHasNoTrainings_publishesEmptyEvent() {
        Trainee trainee = buildTrainee().toBuilder()
                .trainings(new ArrayList<>())
                .build();
        when(traineeRepository.findByUserUsername(USERNAME)).thenReturn(Optional.of(trainee));

        service.deleteByUsername(USERNAME);

        verify(traineeRepository).delete(trainee);
        verify(publisher).publishEvent(new WorkloadUpdateEvent(List.of()));
    }

    @Test
    void deleteByUsername_whenBlankUsername_throwsException() {
        doThrow(new EntityValidationException("Username cannot be blank")).when(validator).requireNonBlank(any(), any());

        assertThrows(EntityValidationException.class, () -> service.deleteByUsername(" "));
        verify(traineeRepository, never()).delete(any());
    }

    @Test
    void deleteByUsername_whenTraineeNotFound_throwsEntityNotFoundException() {
        when(traineeRepository.findByUserUsername(USERNAME)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> service.deleteByUsername(USERNAME));
        verify(traineeRepository, never()).delete(any());
    }

    @Test
    void findByUsername_whenExists_returnsTrainee() {
        Trainee trainee = buildTrainee();
        when(traineeRepository.findByUserUsername(USERNAME)).thenReturn(Optional.of(trainee));

        Optional<Trainee> actual = service.findByUsername(USERNAME);

        assertThat(actual).isPresent();
        assertThat(actual.get()).isEqualTo(trainee);
        verify(validator).requireNonBlank(USERNAME, "Username cannot be blank");
    }

    @Test
    void findByUsername_whenNotExists_returnsEmpty() {
        when(traineeRepository.findByUserUsername(USERNAME)).thenReturn(Optional.empty());

        Optional<Trainee> actual = service.findByUsername(USERNAME);

        assertThat(actual).isEmpty();
    }

    @Test
    void findByUsername_whenBlankUsername_throwsException() {
        doThrow(new EntityValidationException("Username cannot be blank")).when(validator).requireNonBlank(any(), any());

        assertThrows(EntityValidationException.class, () -> service.findByUsername(" "));
        verify(traineeRepository, never()).findByUserUsername(any());
    }

    @Test
    void findAll_returnsAllTrainees() {
        Trainee trainee = buildTrainee();
        when(traineeRepository.findAll()).thenReturn(List.of(trainee));

        List<Trainee> actual = service.findAll();

        assertThat(actual).hasSize(1).contains(trainee);
    }

    @Test
    void findAll_whenEmpty_returnsEmptyList() {
        when(traineeRepository.findAll()).thenReturn(List.of());

        List<Trainee> actual = service.findAll();

        assertThat(actual).isEmpty();
    }

    @Test
    void updateTrainers_whenValidRequest_updatesAndReturnsTrainerList() {
        List<String> trainerUsernames = List.of("Mike.Tyson", "John.Doe");
        Trainer trainer1 = Trainer.builder().build();
        Trainer trainer2 = Trainer.builder().build();
        Trainee trainee = buildTrainee().toBuilder().trainers(new ArrayList<>()).build();
        when(traineeRepository.findByUserUsername(USERNAME)).thenReturn(Optional.of(trainee));
        when(trainerRepository.findAllByUserUsernameIn(trainerUsernames)).thenReturn(new ArrayList<>(List.of(trainer1, trainer2)));

        List<Trainer> actual = service.updateTrainers(USERNAME, trainerUsernames);

        assertThat(actual).hasSize(2).containsAll(List.of(trainer1, trainer2));
        verify(validator).requireNonBlank(USERNAME, "Username cannot be blank");
        verify(validator).requireNonNull(trainerUsernames, "Trainer usernames list cannot be null");
    }

    @Test
    void updateTrainers_whenTraineeNotFound_throwsEntityNotFoundException() {
        List<String> trainerUsernames = List.of("Mike.Tyson");
        when(traineeRepository.findByUserUsername(USERNAME)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> service.updateTrainers(USERNAME, trainerUsernames));
        verify(trainerRepository, never()).findAllByUserUsernameIn(any());
    }

    @Test
    void changePassword_whenTraineeExists_delegatesToUserService() {
        ChangePasswordRequest request = buildChangePasswordRequest();
        when(traineeRepository.findByUserUsername(USERNAME)).thenReturn(Optional.of(buildTrainee()));

        service.changePassword(request);

        verify(userService).changePassword(request);
    }

    @Test
    void changePassword_whenTraineeNotFound_throwsEntityNotFoundException() {
        ChangePasswordRequest request = buildChangePasswordRequest();
        when(traineeRepository.findByUserUsername(USERNAME)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> service.changePassword(request));
        verify(userService, never()).changePassword(any());
    }

    @Test
    void activate_whenTraineeExists_delegatesToUserService() {
        ActivationRequest request = buildActivationRequest();
        when(traineeRepository.findByUserUsername(USERNAME)).thenReturn(Optional.of(buildTrainee()));

        service.activate(request);

        verify(userService).activate(request);
    }

    @Test
    void activate_whenTraineeNotFound_throwsEntityNotFoundException() {
        ActivationRequest request = buildActivationRequest();
        when(traineeRepository.findByUserUsername(USERNAME)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> service.activate(request));
        verify(userService, never()).activate(any());
    }

    @Test
    void deactivate_whenTraineeExists_delegatesToUserService() {
        ActivationRequest request = buildActivationRequest();
        when(traineeRepository.findByUserUsername(USERNAME)).thenReturn(Optional.of(buildTrainee()));

        service.deactivate(request);

        verify(userService).deactivate(request);
    }

    @Test
    void deactivate_whenTraineeNotFound_throwsEntityNotFoundException() {
        ActivationRequest request = buildActivationRequest();
        when(traineeRepository.findByUserUsername(USERNAME)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> service.deactivate(request));
        verify(userService, never()).deactivate(any());
    }

    private Trainee buildTrainee() {
        User user = User.builder()
                .id(ID)
                .firstName(FIRST_NAME)
                .lastName(LAST_NAME)
                .username(USERNAME)
                .isActive(true)
                .build();
        return Trainee.builder()
                .id(ID)
                .user(user)
                .dateOfBirth(LocalDate.of(YEAR, MONTH, DAY))
                .address("Kyiv")
                .build();
    }

    private Training buildTraining() {
        return Training.builder()
                .trainer(Trainer.builder()
                        .user(User.builder()
                                .username(USERNAME)
                                .firstName(FIRST_NAME)
                                .lastName(LAST_NAME)
                                .isActive(true)
                                .build())
                        .build())
                .trainingDate(LocalDate.of(YEAR, MONTH, DAY))
                .trainingDuration(new java.math.BigDecimal("60"))
                .build();
    }

    private TrainerWorkloadMessage buildWorkloadMessage() {
        return new TrainerWorkloadMessage(USERNAME, FIRST_NAME, LAST_NAME, true,
                LocalDate.of(YEAR, MONTH, DAY), 60, ActionType.DELETE);
    }

    private ChangePasswordRequest buildChangePasswordRequest() {
        return ChangePasswordRequest.builder()
                .username(USERNAME)
                .oldPassword("oldPass")
                .newPassword("newPass")
                .build();
    }

    private ActivationRequest buildActivationRequest() {
        return ActivationRequest.builder()
                .username(USERNAME)
                .build();
    }
}