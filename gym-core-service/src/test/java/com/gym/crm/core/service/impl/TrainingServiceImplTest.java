package com.gym.crm.core.service.impl;

import com.gym.crm.core.actuator.metrics.GymMetrics;
import com.gym.crm.core.client.workload.WorkloadRequestMapper;
import com.gym.crm.core.client.workload.WorkloadUpdateEvent;
import com.gym.crm.core.client.workload.model.ActionType;
import com.gym.crm.core.client.workload.model.TrainerWorkloadRequest;
import com.gym.crm.core.dao.search.filters.TraineeTrainingSearchFilter;
import com.gym.crm.core.dao.search.filters.TrainerTrainingSearchFilter;
import com.gym.crm.core.dto.request.CreateTrainingRequest;
import com.gym.crm.core.exception.EntityNotFoundException;
import com.gym.crm.core.exception.EntityValidationException;
import com.gym.crm.core.model.Trainee;
import com.gym.crm.core.model.Trainer;
import com.gym.crm.core.model.Training;
import com.gym.crm.core.model.TrainingType;
import com.gym.crm.core.model.User;
import com.gym.crm.core.repository.TraineeRepository;
import com.gym.crm.core.repository.TrainerRepository;
import com.gym.crm.core.repository.TrainingRepository;
import com.gym.crm.core.security.JwtTokenExtractor;
import com.gym.crm.core.service.common.EntityValidator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TrainingServiceImplTest {

    private static final Long ID = 1L;
    private static final String TRAINEE_USERNAME = "John.Doe";
    private static final String TRAINER_USERNAME = "Mike.Tyson";

    private final Training training = buildTraining();

    @Mock
    private TrainingRepository trainingRepository;
    @Mock
    private TraineeRepository traineeRepository;
    @Mock
    private TrainerRepository trainerRepository;
    @Mock
    private EntityValidator validator;
    @InjectMocks
    private TrainingServiceImpl service;
    @Mock
    private GymMetrics gymMetrics;
    @Mock
    private WorkloadRequestMapper workloadRequestMapper;
    @Mock
    private ApplicationEventPublisher publisher;
    @Mock
    private JwtTokenExtractor jwtTokenExtractor;

    @Test
    void create_whenValidRequest_buildsTrainingAndSaves() {
        Trainee trainee = buildTrainee();
        Trainer trainer = buildTrainer();
        CreateTrainingRequest request = buildCreateRequest();
        Training savedTraining = buildTraining();
        TrainerWorkloadRequest workloadRequest = new TrainerWorkloadRequest().trainerUsername(TRAINER_USERNAME);
        String jwtToken = "jwt-token";

        when(traineeRepository.findByUserUsername(TRAINEE_USERNAME)).thenReturn(Optional.of(trainee));
        when(trainerRepository.findByUserUsername(TRAINER_USERNAME)).thenReturn(Optional.of(trainer));
        when(trainingRepository.save(any(Training.class))).thenReturn(savedTraining);
        when(workloadRequestMapper.toRequest(savedTraining, ActionType.ADD)).thenReturn(workloadRequest);
        when(jwtTokenExtractor.extract()).thenReturn(jwtToken);

        Training actual = service.create(request);

        assertThat(actual.getName()).isEqualTo("Boxing basics");
        assertThat(actual.getTrainee()).isEqualTo(trainee);
        assertThat(actual.getTrainer()).isEqualTo(trainer);
        assertThat(actual.getTrainingType()).isEqualTo(trainer.getSpecialization());
        assertThat(actual.getTrainingDate()).isEqualTo(LocalDate.of(2024, 5, 1));
        assertThat(actual.getTrainingDuration()).isEqualByComparingTo(BigDecimal.valueOf(60));
        verify(traineeRepository).findByUserUsername(TRAINEE_USERNAME);
        verify(trainerRepository).findByUserUsername(TRAINER_USERNAME);
        verify(validator).validateTraining(any(Training.class));
        verify(trainingRepository).save(any(Training.class));
        verify(gymMetrics).incrementTrainingsCreated();
        verify(workloadRequestMapper).toRequest(savedTraining, ActionType.ADD);
        verify(publisher).publishEvent(new WorkloadUpdateEvent(List.of(workloadRequest), jwtToken));
    }

    @Test
    void create_whenTraineeNotFound_throwsEntityNotFoundException() {
        CreateTrainingRequest request = buildCreateRequest();

        when(traineeRepository.findByUserUsername(TRAINEE_USERNAME)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.create(request))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Trainee not found")
                .hasMessageContaining(TRAINEE_USERNAME);
        verify(trainerRepository, never()).findByUserUsername(any());
        verify(trainingRepository, never()).save(any());
        verify(gymMetrics, never()).incrementTrainingsCreated();
    }

    @Test
    void create_whenTrainerNotFound_throwsEntityNotFoundException() {
        CreateTrainingRequest request = buildCreateRequest();

        when(traineeRepository.findByUserUsername(TRAINEE_USERNAME)).thenReturn(Optional.of(buildTrainee()));
        when(trainerRepository.findByUserUsername(TRAINER_USERNAME)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.create(request))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Trainer not found")
                .hasMessageContaining(TRAINER_USERNAME);
        verify(trainingRepository, never()).save(any());
        verify(gymMetrics, never()).incrementTrainingsCreated();
    }

    @Test
    void create_usesTrainerSpecializationAsTrainingType() {
        Trainer trainer = buildTrainer();
        CreateTrainingRequest request = buildCreateRequest();
        TrainerWorkloadRequest workloadRequest = new TrainerWorkloadRequest().trainerUsername(TRAINER_USERNAME);

        when(traineeRepository.findByUserUsername(TRAINEE_USERNAME)).thenReturn(Optional.of(buildTrainee()));
        when(trainerRepository.findByUserUsername(TRAINER_USERNAME)).thenReturn(Optional.of(trainer));
        when(trainingRepository.save(any(Training.class))).thenAnswer(inv -> inv.getArgument(0));
        when(workloadRequestMapper.toRequest(any(Training.class), any(ActionType.class))).thenReturn(workloadRequest);

        Training actual = service.create(request);

        assertThat(actual.getTrainingType()).isEqualTo(trainer.getSpecialization());
        ArgumentCaptor<Training> captor = ArgumentCaptor.forClass(Training.class);
        verify(trainingRepository).save(captor.capture());
        assertThat(captor.getValue().getTrainingType().getTrainingTypeName()).isEqualTo("BOXING");
    }

    @Test
    void findAll_whenTrainingsExist_returnsAllTrainings() {
        when(trainingRepository.findAll()).thenReturn(List.of(training));

        List<Training> actual = service.findAll();

        assertThat(actual.size()).isEqualTo(1);
        assertThat(actual.iterator().next()).isEqualTo(training);
        verify(trainingRepository).findAll();
    }

    @Test
    void findAll_whenNoTrainingsExist_returnsEmptyList() {
        when(trainingRepository.findAll()).thenReturn(List.of());

        List<Training> actual = service.findAll();

        assertThat(actual.isEmpty()).isTrue();
        verify(trainingRepository).findAll();
    }

    @Test
    @SuppressWarnings("unchecked")
    void findByTraineeCriteria_whenFilterValid_returnsMatchingTrainings() {
        TraineeTrainingSearchFilter filter = TraineeTrainingSearchFilter.builder()
                .username(TRAINEE_USERNAME)
                .build();
        when(trainingRepository.findAll(any(Specification.class))).thenReturn(List.of(training));

        List<Training> actual = service.findByTraineeCriteria(filter);

        ArgumentCaptor<Specification<Training>> captor = ArgumentCaptor.forClass(Specification.class);
        verify(trainingRepository).findAll(captor.capture());
        verify(validator).requireNonNull(filter, "Search filter cannot be null");
        assertThat(captor.getValue()).isNotNull();
        assertThat(actual.size()).isEqualTo(1);
        assertThat(actual.iterator().next()).isEqualTo(training);
    }

    @Test
    @SuppressWarnings("unchecked")
    void findByTraineeCriteria_whenNoResults_returnsEmptyList() {
        TraineeTrainingSearchFilter filter = TraineeTrainingSearchFilter.builder()
                .username(TRAINEE_USERNAME)
                .fromDate(LocalDate.of(2030, 1, 1))
                .build();
        when(trainingRepository.findAll(any(Specification.class))).thenReturn(List.of());

        List<Training> actual = service.findByTraineeCriteria(filter);

        assertThat(actual.isEmpty()).isTrue();
        verify(trainingRepository).findAll(any(Specification.class));
    }

    @Test
    @SuppressWarnings("unchecked")
    void findByTrainerCriteria_whenFilterValid_returnsMatchingTrainings() {
        TrainerTrainingSearchFilter filter = TrainerTrainingSearchFilter.builder()
                .username(TRAINER_USERNAME)
                .build();
        when(trainingRepository.findAll(any(Specification.class))).thenReturn(List.of(training));

        List<Training> actual = service.findByTrainerCriteria(filter);

        ArgumentCaptor<Specification<Training>> captor = ArgumentCaptor.forClass(Specification.class);
        verify(trainingRepository).findAll(captor.capture());
        verify(validator).requireNonNull(filter, "Search filter cannot be null");
        assertThat(captor.getValue()).isNotNull();
        assertThat(actual.size()).isEqualTo(1);
        assertThat(actual.iterator().next()).isEqualTo(training);
    }

    @Test
    @SuppressWarnings("unchecked")
    void findByTrainerCriteria_whenNoResults_returnsEmptyList() {
        TrainerTrainingSearchFilter filter = TrainerTrainingSearchFilter.builder()
                .username(TRAINER_USERNAME)
                .fromDate(LocalDate.of(2030, 1, 1))
                .build();
        when(trainingRepository.findAll(any(Specification.class))).thenReturn(List.of());

        List<Training> actual = service.findByTrainerCriteria(filter);

        assertThat(actual.isEmpty()).isTrue();
        verify(trainingRepository).findAll(any(Specification.class));
    }

    @Test
    void findByTrainerCriteria_whenFilterNull_throwsEntityValidationException() {
        doThrow(new EntityValidationException("Search filter cannot be null"))
                .when(validator).requireNonNull(null, "Search filter cannot be null");

        assertThrows(EntityValidationException.class, () -> service.findByTrainerCriteria(null));

        verify(trainingRepository, never()).findAll(any(Specification.class));
    }

    @Test
    void findByTraineeCriteria_whenFilterNull_throwsEntityValidationException() {
        doThrow(new EntityValidationException("Search filter cannot be null"))
                .when(validator).requireNonNull(null, "Search filter cannot be null");

        assertThrows(EntityValidationException.class, () -> service.findByTraineeCriteria(null));

        verify(trainingRepository, never()).findAll(any(Specification.class));
    }

    private CreateTrainingRequest buildCreateRequest() {
        return CreateTrainingRequest.builder()
                .traineeUsername(TRAINEE_USERNAME)
                .trainerUsername(TRAINER_USERNAME)
                .trainingName("Boxing basics")
                .trainingDate(LocalDate.of(2024, 5, 1))
                .trainingDuration(BigDecimal.valueOf(60))
                .build();
    }

    private Trainee buildTrainee() {
        User user = User.builder()
                .firstName("John")
                .lastName("Doe")
                .username(TRAINEE_USERNAME)
                .build();
        return Trainee.builder()
                .user(user)
                .build();
    }

    private Trainer buildTrainer() {
        User user = User.builder()
                .firstName("Mike")
                .lastName("Tyson")
                .username(TRAINER_USERNAME)
                .build();
        return Trainer.builder()
                .user(user)
                .specialization(TrainingType.builder()
                        .trainingTypeName("BOXING")
                        .build())
                .build();
    }

    private Training buildTraining() {
        User user = User.builder()
                .firstName("John")
                .lastName("Doe")
                .username("John.Doe")
                .build();

        Trainee trainee = Trainee.builder()
                .user(user)
                .build();

        Trainer trainer = Trainer.builder()
                .user(user)
                .specialization(TrainingType.builder()
                        .trainingTypeName("BOXING")
                        .build())
                .build();

        return Training.builder()
                .id(ID)
                .name("Boxing basics")
                .trainee(trainee)
                .trainer(trainer)
                .trainingType(TrainingType.builder().trainingTypeName("BOXING").build())
                .trainingDate(LocalDate.of(2024, 5, 1))
                .trainingDuration(BigDecimal.valueOf(60))
                .build();
    }
}