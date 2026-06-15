package com.gym.crm.core.service.impl;

import com.gym.crm.core.actuator.metrics.GymMetrics;
import com.gym.crm.core.dto.request.ActivationRequest;
import com.gym.crm.core.dto.request.ChangePasswordRequest;
import com.gym.crm.core.exception.EntityNotFoundException;
import com.gym.crm.core.exception.EntityValidationException;
import com.gym.crm.core.model.Trainee;
import com.gym.crm.core.model.Trainer;
import com.gym.crm.core.model.TrainingType;
import com.gym.crm.core.model.User;
import com.gym.crm.core.repository.TraineeRepository;
import com.gym.crm.core.repository.TrainerRepository;
import com.gym.crm.core.repository.TrainingTypeRepository;
import com.gym.crm.core.service.UserProfileService;
import com.gym.crm.core.service.UserService;
import com.gym.crm.core.service.common.EntityValidator;
import com.gym.crm.core.service.impl.TrainerServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TrainerServiceImplTest {

    private static final Long ID = 1L;
    private static final String USERNAME = "Mike.Tyson";
    private static final String SPECIALIZATION = "BOXING";

    private final Trainer trainer = buildTrainer();

    @Mock
    private TrainerRepository trainerRepository;
    @Mock
    private TraineeRepository traineeRepository;
    @Mock
    private TrainingTypeRepository trainingTypeRepository;
    @Mock
    private EntityValidator validator;
    @Mock
    private UserProfileService userProfileService;
    @Mock
    private UserService userService;
    @InjectMocks
    private TrainerServiceImpl service;
    @Mock
    private GymMetrics gymMetrics;

    @Test
    void create_whenValidTrainer_savesWithGeneratedProfile() {
        TrainingType specialization = TrainingType.builder().trainingTypeName(SPECIALIZATION).build();
        when(trainingTypeRepository.findByTrainingTypeName(SPECIALIZATION)).thenReturn(Optional.of(specialization));
        when(userProfileService.generateUsername("Mike", "Tyson")).thenReturn("Mike.Tyson");
        when(userProfileService.generatePassword()).thenReturn("rawPass123");
        when(userProfileService.hashPassword("rawPass123")).thenReturn("hashedPass");
        when(trainerRepository.save(any(Trainer.class))).thenAnswer(inv -> inv.getArgument(0));

        Trainer actual = service.create(trainer, SPECIALIZATION);

        assertThat(actual.getUser().getUsername()).isEqualTo("Mike.Tyson");
        assertThat(actual.getUser().getPassword()).isEqualTo("hashedPass");
        assertThat(actual.getUser().getPassword()).isNotEqualTo("rawPass123");
        assertThat(actual.getSpecialization()).isEqualTo(specialization);
        verify(validator, times(2)).validateTrainer(any(Trainer.class));
        verify(trainingTypeRepository).findByTrainingTypeName(SPECIALIZATION);
        verify(userProfileService).generateUsername("Mike", "Tyson");
        verify(userProfileService).generatePassword();
        verify(userProfileService).hashPassword("rawPass123");
        verify(trainerRepository).save(any(Trainer.class));
        verify(gymMetrics).incrementTrainerRegistrations();
    }

    @Test
    void create_whenValidationFails_throwsExceptionAndRepositoryNotCalled() {
        doThrow(new EntityValidationException("Trainer cannot be null")).when(validator).validateTrainer(trainer);

        assertThrows(EntityValidationException.class, () -> service.create(trainer, SPECIALIZATION));

        verify(trainerRepository, never()).save(any());
        verify(gymMetrics, never()).incrementTrainerRegistrations();
    }

    @Test
    void create_whenSpecializationNotFound_throwsEntityNotFoundException() {
        when(trainingTypeRepository.findByTrainingTypeName("UNKNOWN")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.create(trainer, "UNKNOWN"))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Specialization not found")
                .hasMessageContaining("UNKNOWN");

        verify(trainerRepository, never()).save(any());
        verify(gymMetrics, never()).incrementTrainerRegistrations();
    }

    @Test
    void create_whenBlankSpecializationName_throwsAndRepositoryNotCalled() {
        doThrow(new EntityValidationException("Specialization name cannot be blank"))
                .when(validator).requireNonBlank("", "Specialization name cannot be blank");

        assertThatThrownBy(() -> service.create(trainer, ""))
                .isInstanceOf(EntityValidationException.class)
                .hasMessageContaining("Specialization name cannot be blank");

        verify(trainerRepository, never()).save(any());
    }

    @Test
    void update_whenValidTrainer_updatesSuccessfully() {
        Trainer existing = trainer.toBuilder()
                .user(trainer.getUser().toBuilder()
                        .id(ID)
                        .username(USERNAME)
                        .password("existingHash")
                        .build())
                .specialization(trainer.getSpecialization())
                .build();

        when(trainerRepository.findByUserUsername(USERNAME)).thenReturn(Optional.of(existing));
        when(trainerRepository.save(any(Trainer.class))).thenAnswer(inv -> inv.getArgument(0));

        Trainer actual = service.update(trainer);

        assertThat(actual.getUser().getUsername()).isEqualTo(USERNAME);
        assertThat(actual.getUser().getPassword()).isEqualTo("existingHash");
        assertThat(actual.getSpecialization()).isEqualTo(existing.getSpecialization());
        verify(validator).validateTrainer(trainer);
        verify(trainerRepository).findByUserUsername(USERNAME);
        verify(trainerRepository).save(any(Trainer.class));
    }

    @Test
    void update_whenTrainerNotFound_throwsEntityNotFoundException() {
        when(trainerRepository.findByUserUsername(USERNAME)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.update(trainer))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Trainer not found")
                .hasMessageContaining(USERNAME);

        verify(trainerRepository, never()).save(any());
    }

    @Test
    void findByUsername_whenTrainerExists_returnsTrainer() {
        when(trainerRepository.findByUserUsername(USERNAME)).thenReturn(Optional.of(trainer));

        Optional<Trainer> actual = service.findByUsername(USERNAME);

        assertThat(actual).isPresent();
        assertThat(actual.get()).isEqualTo(trainer);
        verify(validator).requireNonBlank(USERNAME, "Username cannot be blank");
        verify(trainerRepository).findByUserUsername(USERNAME);
    }

    @Test
    void findByUsername_whenTrainerNotExists_returnsEmpty() {
        when(trainerRepository.findByUserUsername(USERNAME)).thenReturn(Optional.empty());

        Optional<Trainer> actual = service.findByUsername(USERNAME);

        assertThat(actual).isEmpty();
        verify(trainerRepository).findByUserUsername(USERNAME);
    }

    @Test
    void findByUsername_whenBlankUsername_throwsAndRepositoryNotCalled() {
        doThrow(new EntityValidationException("Username cannot be blank"))
                .when(validator).requireNonBlank("", "Username cannot be blank");

        assertThrows(EntityValidationException.class, () -> service.findByUsername(""));

        verify(trainerRepository, never()).findByUserUsername(any());
    }

    @Test
    void findAll_whenTrainersExist_returnsAllTrainers() {
        when(trainerRepository.findAll()).thenReturn(List.of(trainer));

        List<Trainer> actual = service.findAll();

        assertThat(actual).containsExactly(trainer);
        verify(trainerRepository).findAll();
    }

    @Test
    void findAll_whenNoTrainersExist_returnsEmptyList() {
        when(trainerRepository.findAll()).thenReturn(List.of());

        List<Trainer> actual = service.findAll();

        assertThat(actual).isEmpty();
        verify(trainerRepository).findAll();
    }

    @Test
    void findAllNotAssignedToTrainee_whenTraineeExists_returnsUnassignedTrainers() {
        Trainee trainee = buildTrainee();
        when(traineeRepository.findByUserUsername("Abdul.Hariton")).thenReturn(Optional.of(trainee));
        when(trainerRepository.findAllActiveNotAssignedToTrainee("Abdul.Hariton")).thenReturn(List.of(trainer));

        List<Trainer> actual = service.findAllNotAssignedToTrainee("Abdul.Hariton");

        assertThat(actual.iterator().next()).isEqualTo(trainer);
        verify(validator).requireNonBlank("Abdul.Hariton", "Username cannot be blank");
        verify(traineeRepository).findByUserUsername("Abdul.Hariton");
        verify(trainerRepository).findAllActiveNotAssignedToTrainee("Abdul.Hariton");
    }

    @Test
    void findAllNotAssignedToTrainee_whenTraineeNotFound_throwsEntityNotFoundException() {
        when(traineeRepository.findByUserUsername("ghost.user")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.findAllNotAssignedToTrainee("ghost.user"))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Trainee not found")
                .hasMessageContaining("ghost.user");

        verify(trainerRepository, never()).findAllActiveNotAssignedToTrainee(anyString());
    }

    @Test
    void findAllNotAssignedToTrainee_whenBlankUsername_throwsAndRepositoriesNotCalled() {
        doThrow(new EntityValidationException("Username cannot be blank"))
                .when(validator).requireNonBlank("", "Username cannot be blank");

        assertThatThrownBy(() -> service.findAllNotAssignedToTrainee(""))
                .isInstanceOf(EntityValidationException.class)
                .hasMessageContaining("Username cannot be blank");

        verify(traineeRepository, never()).findByUserUsername(anyString());
        verify(trainerRepository, never()).findAllActiveNotAssignedToTrainee(anyString());
    }

    @Test
    void changePassword_whenTrainerExists_delegatesToUserService() {
        ChangePasswordRequest request = ChangePasswordRequest.builder()
                .username(USERNAME)
                .oldPassword("oldPassword")
                .newPassword("newPassword123")
                .build();

        when(trainerRepository.findByUserUsername(USERNAME)).thenReturn(Optional.of(trainer));
        doNothing().when(userService).changePassword(request);

        service.changePassword(request);

        verify(trainerRepository).findByUserUsername(USERNAME);
        verify(userService).changePassword(request);
    }

    @Test
    void changePassword_whenTrainerNotFound_throwsEntityNotFoundException() {
        ChangePasswordRequest request = ChangePasswordRequest.builder()
                .username("ghost.user")
                .oldPassword("oldPassword")
                .newPassword("newPassword123")
                .build();

        when(trainerRepository.findByUserUsername("ghost.user")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.changePassword(request))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Trainer not found")
                .hasMessageContaining("ghost.user");

        verify(userService, never()).changePassword(any());
    }

    @Test
    void activate_whenTrainerExists_delegatesToUserService() {
        ActivationRequest request = ActivationRequest.builder()
                .username(USERNAME)
                .build();

        when(trainerRepository.findByUserUsername(USERNAME)).thenReturn(Optional.of(trainer));
        doNothing().when(userService).activate(request);

        service.activate(request);

        verify(trainerRepository).findByUserUsername(USERNAME);
        verify(userService).activate(request);
    }

    @Test
    void activate_whenTrainerNotFound_throwsEntityNotFoundException() {
        ActivationRequest request = ActivationRequest.builder()
                .username("ghost.user")
                .build();

        when(trainerRepository.findByUserUsername("ghost.user")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.activate(request))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Trainer not found")
                .hasMessageContaining("ghost.user");

        verify(userService, never()).activate(any());
    }

    @Test
    void deactivate_whenTrainerExists_delegatesToUserService() {
        ActivationRequest request = ActivationRequest.builder()
                .username(USERNAME)
                .build();

        when(trainerRepository.findByUserUsername(USERNAME)).thenReturn(Optional.of(trainer));
        doNothing().when(userService).deactivate(request);

        service.deactivate(request);

        verify(trainerRepository).findByUserUsername(USERNAME);
        verify(userService).deactivate(request);
    }

    @Test
    void deactivate_whenTrainerNotFound_throwsEntityNotFoundException() {
        ActivationRequest request = ActivationRequest.builder()
                .username("ghost.user")
                .build();

        when(trainerRepository.findByUserUsername("ghost.user")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.deactivate(request))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Trainer not found")
                .hasMessageContaining("ghost.user");

        verify(userService, never()).deactivate(any());
    }

    private Trainer buildTrainer() {
        User user = User.builder()
                .id(ID)
                .firstName("Mike")
                .lastName("Tyson")
                .username(USERNAME)
                .isActive(true)
                .build();

        return Trainer.builder()
                .id(ID)
                .user(user)
                .specialization(TrainingType.builder()
                        .trainingTypeName(SPECIALIZATION)
                        .build())
                .build();
    }

    private Trainee buildTrainee() {
        User user = User.builder()
                .id(2L)
                .firstName("Abdul")
                .lastName("Hariton")
                .username("Abdul.Hariton")
                .isActive(true)
                .build();

        return Trainee.builder()
                .id(2L)
                .user(user)
                .build();
    }
}