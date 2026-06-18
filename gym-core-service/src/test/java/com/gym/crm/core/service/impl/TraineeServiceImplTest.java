package com.gym.crm.core.service.impl;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import com.gym.crm.core.actuator.metrics.GymMetrics;
import com.gym.crm.core.client.workload.WorkloadRequestMapper;
import com.gym.crm.core.client.workload.WorkloadUpdateEvent;
import com.gym.crm.core.dto.request.ActivationRequest;
import com.gym.crm.core.dto.request.ChangePasswordRequest;
import com.gym.crm.core.exception.EntityNotFoundException;
import com.gym.crm.core.exception.EntityValidationException;
import com.gym.crm.core.model.Trainee;
import com.gym.crm.core.model.Trainer;
import com.gym.crm.core.model.User;
import com.gym.crm.core.repository.TraineeRepository;
import com.gym.crm.core.repository.TrainerRepository;
import com.gym.crm.core.service.UserProfileService;
import com.gym.crm.core.service.UserService;
import com.gym.crm.core.service.common.EntityValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TraineeServiceImplTest {

    private static final Long ID = 1L;
    private static final String USERNAME = "Abdul.Hariton";

    private final Trainee trainee = buildTrainee();

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
    @InjectMocks
    private TraineeServiceImpl service;
    @Mock
    private GymMetrics gymMetrics;
    @Mock
    private WorkloadRequestMapper workloadRequestMapper;
    @Mock
    private ApplicationEventPublisher publisher;

    private ListAppender<ILoggingEvent> listAppender;

    @BeforeEach
    void setUp() {
        Logger logger = (Logger) LoggerFactory.getLogger(TraineeServiceImpl.class);
        listAppender = new ListAppender<>();
        listAppender.start();
        logger.addAppender(listAppender);
    }

    @Test
    void create_whenValidTrainee_savesWithGeneratedProfile() {
        when(userProfileService.generateUsername("Abdul", "Hariton")).thenReturn("Abdul.Hariton");
        when(userProfileService.generatePassword()).thenReturn("rawPass123");
        when(userProfileService.hashPassword("rawPass123")).thenReturn("hashedPass");
        when(traineeRepository.save(any(Trainee.class))).thenAnswer(inv -> inv.getArgument(0));

        Trainee actual = service.create(trainee);

        assertEquals("Abdul.Hariton", actual.getUser().getUsername());
        assertEquals("hashedPass", actual.getUser().getPassword());
        assertNotEquals("rawPass123", actual.getUser().getPassword());
        verify(validator).validateTrainee(trainee);
        verify(userProfileService).generateUsername("Abdul", "Hariton");
        verify(userProfileService).generatePassword();
        verify(userProfileService).hashPassword("rawPass123");
        verify(traineeRepository).save(any(Trainee.class));
        verify(gymMetrics).incrementTraineeRegistrations();
    }

    @Test
    void create_whenValidationFails_throwsException() {
        doThrow(new EntityValidationException("Trainee cannot be null"))
                .when(validator).validateTrainee(any());

        assertThrows(EntityValidationException.class, () -> service.create(null));

        verify(traineeRepository, never()).save(any());
        verify(gymMetrics, never()).incrementTraineeRegistrations();
    }

    @Test
    void update_whenValidTrainee_updatesSuccessfully() {
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

        assertEquals(trainee, actual);
        verify(validator).validateTrainee(trainee);
        verify(traineeRepository).findByUserUsername(USERNAME);
        verify(traineeRepository).save(any(Trainee.class));
    }

    @Test
    void update_whenTraineeNotFound_throwsEntityNotFoundException() {
        when(traineeRepository.findByUserUsername(USERNAME)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> service.update(trainee));

        verify(traineeRepository, never()).save(any());
    }

    @Test
    void deleteByUsername_whenValidUsername_deletesSuccessfully() {
        Trainee traineeWithTrainings = trainee.toBuilder()
                .trainings(new ArrayList<>())
                .build();
        when(traineeRepository.findByUserUsername(USERNAME)).thenReturn(Optional.of(traineeWithTrainings));

        service.deleteByUsername(USERNAME);

        verify(validator).requireNonBlank(USERNAME, "Username cannot be blank");
        verify(traineeRepository).findByUserUsername(USERNAME);
        verify(traineeRepository).delete(traineeWithTrainings);
        verify(publisher).publishEvent(new WorkloadUpdateEvent(List.of()));
    }

    @Test
    void deleteByUsername_whenBlankUsername_throwsException() {
        doThrow(new EntityValidationException("Username cannot be blank"))
                .when(validator).requireNonBlank(any(), any());

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
        when(traineeRepository.findByUserUsername(USERNAME)).thenReturn(Optional.of(trainee));

        Optional<Trainee> actual = service.findByUsername(USERNAME);

        assertTrue(actual.isPresent());
        assertEquals(trainee, actual.get());
        verify(validator).requireNonBlank(USERNAME, "Username cannot be blank");
    }

    @Test
    void findByUsername_whenNotExists_returnsEmpty() {
        when(traineeRepository.findByUserUsername(USERNAME)).thenReturn(Optional.empty());

        Optional<Trainee> actual = service.findByUsername(USERNAME);

        assertFalse(actual.isPresent());
    }

    @Test
    void findByUsername_whenBlankUsername_throwsException() {
        doThrow(new EntityValidationException("Username cannot be blank"))
                .when(validator).requireNonBlank(any(), any());

        assertThrows(EntityValidationException.class, () -> service.findByUsername(" "));

        verify(traineeRepository, never()).findByUserUsername(any());
    }


    @Test
    void findAll_returnsAllTrainees() {
        List<Trainee> trainees = List.of(trainee);
        when(traineeRepository.findAll()).thenReturn(trainees);

        List<Trainee> actual = service.findAll();

        assertEquals(1, actual.size());
        assertTrue(actual.contains(trainee));
    }

    @Test
    void findAll_whenEmpty_returnsEmptyList() {
        when(traineeRepository.findAll()).thenReturn(List.of());

        List<Trainee> actual = service.findAll();

        assertTrue(actual.isEmpty());
    }

    @Test
    void updateTrainers_whenValidRequest_updatesAndReturnsTrainerList() {
        List<String> trainerUsernames = List.of("Mike.Tyson", "John.Doe");
        Trainer trainer1 = Trainer.builder().build();
        Trainer trainer2 = Trainer.builder().build();
        List<Trainer> foundTrainers = new ArrayList<>(List.of(trainer1, trainer2));

        Trainee mutableTrainee = trainee.toBuilder().trainers(new ArrayList<>()).build();
        when(traineeRepository.findByUserUsername(USERNAME)).thenReturn(Optional.of(mutableTrainee));
        when(trainerRepository.findAllByUserUsernameIn(trainerUsernames)).thenReturn(foundTrainers);

        List<Trainer> result = service.updateTrainers(USERNAME, trainerUsernames);

        assertEquals(2, result.size());
        assertTrue(result.containsAll(foundTrainers));
        verify(validator).requireNonBlank(USERNAME, "Username cannot be blank");
        verify(validator).requireNonNull(trainerUsernames, "Trainer usernames list cannot be null");
        verify(traineeRepository).findByUserUsername(USERNAME);
        verify(trainerRepository).findAllByUserUsernameIn(trainerUsernames);
    }

    @Test
    void updateTrainers_whenTraineeNotFound_throwsEntityNotFoundException() {
        List<String> trainerUsernames = List.of("Mike.Tyson");
        when(traineeRepository.findByUserUsername(USERNAME)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> service.updateTrainers(USERNAME, trainerUsernames));

        verify(trainerRepository, never()).findAllByUserUsernameIn(any());
    }

    @Test
    void updateTrainers_whenNullList_throwsEntityValidationException() {
        doThrow(new EntityValidationException("Trainer usernames list cannot be null"))
                .when(validator).requireNonNull(any(), any());

        assertThrows(EntityValidationException.class, () -> service.updateTrainers(USERNAME, null));

        verify(traineeRepository, never()).findByUserUsername(any());
        verify(trainerRepository, never()).findAllByUserUsernameIn(any());
    }

    @Test
    void changePassword_whenTraineeExists_delegatesToUserService() {
        ChangePasswordRequest request = ChangePasswordRequest.builder()
                .username(USERNAME)
                .oldPassword("oldPass123")
                .newPassword("newPass456")
                .build();
        when(traineeRepository.findByUserUsername(USERNAME)).thenReturn(Optional.of(trainee));

        service.changePassword(request);

        verify(userService).changePassword(request);
    }

    @Test
    void changePassword_whenTraineeNotFound_throwsEntityNotFoundException() {
        ChangePasswordRequest request = ChangePasswordRequest.builder()
                .username(USERNAME)
                .oldPassword("oldPass123")
                .newPassword("newPass456")
                .build();
        when(traineeRepository.findByUserUsername(USERNAME)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> service.changePassword(request));

        verify(userService, never()).changePassword(any());
    }

    @Test
    void activate_whenTraineeExists_delegatesToUserService() {
        ActivationRequest request = ActivationRequest.builder()
                .username(USERNAME)
                .build();
        when(traineeRepository.findByUserUsername(USERNAME)).thenReturn(Optional.of(trainee));

        service.activate(request);

        verify(userService).activate(request);
    }

    @Test
    void activate_whenTraineeNotFound_throwsEntityNotFoundException() {
        ActivationRequest request = ActivationRequest.builder()
                .username(USERNAME)
                .build();
        when(traineeRepository.findByUserUsername(USERNAME)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> service.activate(request));

        verify(userService, never()).activate(any());
    }

    @Test
    void deactivate_whenTraineeExists_delegatesToUserService() {
        ActivationRequest request = ActivationRequest.builder()
                .username(USERNAME)
                .build();
        when(traineeRepository.findByUserUsername(USERNAME)).thenReturn(Optional.of(trainee));

        service.deactivate(request);

        verify(userService).deactivate(request);
    }

    @Test
    void deactivate_whenTraineeNotFound_throwsEntityNotFoundException() {
        ActivationRequest request = ActivationRequest.builder()
                .username(USERNAME)
                .build();
        when(traineeRepository.findByUserUsername(USERNAME)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> service.deactivate(request));

        verify(userService, never()).deactivate(any());
    }

    private Trainee buildTrainee() {
        User user = User.builder()
                .id(ID)
                .firstName("Abdul")
                .lastName("Hariton")
                .username("Abdul.Hariton")
                .isActive(true)
                .build();

        return Trainee.builder()
                .id(ID)
                .user(user)
                .dateOfBirth(LocalDate.of(1990, 1, 1))
                .address("Kyiv")
                .build();
    }
}