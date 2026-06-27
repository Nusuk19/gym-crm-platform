package com.gym.crm.core.service.impl;

import com.gym.crm.core.actuator.metrics.GymMetrics;
import com.gym.crm.core.dao.search.filters.TraineeTrainingSearchFilter;
import com.gym.crm.core.dao.search.filters.TrainerTrainingSearchFilter;
import com.gym.crm.core.dto.request.CreateTrainingRequest;
import com.gym.crm.core.exception.EntityNotFoundException;
import com.gym.crm.core.exception.EntityValidationException;
import com.gym.crm.core.messaging.workload.ActionType;
import com.gym.crm.core.messaging.workload.TrainerWorkloadMessage;
import com.gym.crm.core.messaging.workload.WorkloadMessageMapper;
import com.gym.crm.core.messaging.workload.WorkloadUpdateEvent;
import com.gym.crm.core.model.Trainee;
import com.gym.crm.core.model.Trainer;
import com.gym.crm.core.model.Training;
import com.gym.crm.core.model.TrainingType;
import com.gym.crm.core.model.User;
import com.gym.crm.core.repository.TraineeRepository;
import com.gym.crm.core.repository.TrainerRepository;
import com.gym.crm.core.repository.TrainingRepository;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TrainingServiceImplTest {

    private static final Long ID = 1L;
    private static final String TRAINEE_USERNAME = "Abdul.Hariton";
    private static final String TRAINER_USERNAME = "Mike.Tyson";
    private static final String TRAINING_NAME = "Boxing basics";
    private static final int YEAR = 2024;
    private static final int MONTH = 5;
    private static final int DAY = 1;
    private static final int DURATION = 60;

    @Mock
    private TrainingRepository trainingRepository;
    @Mock
    private TraineeRepository traineeRepository;
    @Mock
    private TrainerRepository trainerRepository;
    @Mock
    private EntityValidator validator;
    @Mock
    private GymMetrics gymMetrics;
    @Mock
    private WorkloadMessageMapper workloadMessageMapper;
    @Mock
    private ApplicationEventPublisher publisher;

    @InjectMocks
    private TrainingServiceImpl service;

    @Test
    void create_whenValidRequest_buildsTrainingAndSaves() {
        Trainee trainee = buildTrainee();
        Trainer trainer = buildTrainer();
        CreateTrainingRequest request = buildCreateRequest();
        Training saved = buildTraining();
        TrainerWorkloadMessage expected = buildWorkloadMessage();
        when(traineeRepository.findByUserUsername(TRAINEE_USERNAME)).thenReturn(Optional.of(trainee));
        when(trainerRepository.findByUserUsername(TRAINER_USERNAME)).thenReturn(Optional.of(trainer));
        when(trainingRepository.save(any(Training.class))).thenReturn(saved);
        when(workloadMessageMapper.toMessage(saved, ActionType.ADD)).thenReturn(expected);

        Training actual = service.create(request);

        assertThat(actual.getName()).isEqualTo(TRAINING_NAME);
        assertThat(actual.getTrainee()).isEqualTo(trainee);
        assertThat(actual.getTrainer()).isEqualTo(trainer);
        assertThat(actual.getTrainingDate()).isEqualTo(LocalDate.of(YEAR, MONTH, DAY));
        assertThat(actual.getTrainingDuration()).isEqualByComparingTo(BigDecimal.valueOf(DURATION));
        verify(trainingRepository).save(any(Training.class));
        verify(workloadMessageMapper).toMessage(saved, ActionType.ADD);
        verify(publisher).publishEvent(new WorkloadUpdateEvent(List.of(expected)));
        verify(gymMetrics).incrementTrainingsCreated();
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
        when(traineeRepository.findByUserUsername(TRAINEE_USERNAME)).thenReturn(Optional.of(buildTrainee()));
        when(trainerRepository.findByUserUsername(TRAINER_USERNAME)).thenReturn(Optional.of(trainer));
        when(trainingRepository.save(any(Training.class))).thenAnswer(inv -> inv.getArgument(0));
        when(workloadMessageMapper.toMessage(any(Training.class), any(ActionType.class))).thenReturn(buildWorkloadMessage());

        Training actual = service.create(request);

        assertThat(actual.getTrainingType()).isEqualTo(trainer.getSpecialization());
        ArgumentCaptor<Training> captor = ArgumentCaptor.forClass(Training.class);
        verify(trainingRepository).save(captor.capture());
        assertThat(captor.getValue().getTrainingType().getTrainingTypeName()).isEqualTo("BOXING");
    }

    @Test
    void findAll_whenTrainingsExist_returnsAllTrainings() {
        Training training = buildTraining();
        when(trainingRepository.findAll()).thenReturn(List.of(training));

        List<Training> actual = service.findAll();

        assertThat(actual).hasSize(1).contains(training);
        verify(trainingRepository).findAll();
    }

    @Test
    void findAll_whenNoTrainingsExist_returnsEmptyList() {
        when(trainingRepository.findAll()).thenReturn(List.of());

        List<Training> actual = service.findAll();

        assertThat(actual).isEmpty();
        verify(trainingRepository).findAll();
    }

    @Test
    @SuppressWarnings("unchecked")
    void findByTraineeCriteria_whenFilterValid_returnsMatchingTrainings() {
        Training training = buildTraining();
        TraineeTrainingSearchFilter filter = TraineeTrainingSearchFilter.builder()
                .username(TRAINEE_USERNAME)
                .build();
        when(trainingRepository.findAll(any(Specification.class))).thenReturn(List.of(training));

        List<Training> actual = service.findByTraineeCriteria(filter);

        assertThat(actual).hasSize(1).contains(training);
        verify(validator).requireNonNull(filter, "Search filter cannot be null");
    }

    @Test
    @SuppressWarnings("unchecked")
    void findByTraineeCriteria_whenNoResults_returnsEmptyList() {
        TraineeTrainingSearchFilter filter = TraineeTrainingSearchFilter.builder()
                .username(TRAINEE_USERNAME)
                .build();
        when(trainingRepository.findAll(any(Specification.class))).thenReturn(List.of());

        List<Training> actual = service.findByTraineeCriteria(filter);

        assertThat(actual).isEmpty();
    }

    @Test
    @SuppressWarnings("unchecked")
    void findByTrainerCriteria_whenFilterValid_returnsMatchingTrainings() {
        Training training = buildTraining();
        TrainerTrainingSearchFilter filter = TrainerTrainingSearchFilter.builder()
                .username(TRAINER_USERNAME)
                .build();
        when(trainingRepository.findAll(any(Specification.class))).thenReturn(List.of(training));

        List<Training> actual = service.findByTrainerCriteria(filter);

        assertThat(actual).hasSize(1).contains(training);
        verify(validator).requireNonNull(filter, "Search filter cannot be null");
    }

    @Test
    @SuppressWarnings("unchecked")
    void findByTrainerCriteria_whenNoResults_returnsEmptyList() {
        TrainerTrainingSearchFilter filter = TrainerTrainingSearchFilter.builder()
                .username(TRAINER_USERNAME)
                .build();
        when(trainingRepository.findAll(any(Specification.class))).thenReturn(List.of());

        List<Training> actual = service.findByTrainerCriteria(filter);

        assertThat(actual).isEmpty();
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
                .trainingName(TRAINING_NAME)
                .trainingDate(LocalDate.of(YEAR, MONTH, DAY))
                .trainingDuration(BigDecimal.valueOf(DURATION))
                .build();
    }

    private Trainee buildTrainee() {
        return Trainee.builder()
                .user(User.builder()
                        .firstName("Abdul")
                        .lastName("Hariton")
                        .username(TRAINEE_USERNAME)
                        .build())
                .build();
    }

    private Trainer buildTrainer() {
        return Trainer.builder()
                .user(User.builder()
                        .firstName("Mike")
                        .lastName("Tyson")
                        .username(TRAINER_USERNAME)
                        .build())
                .specialization(TrainingType.builder()
                        .trainingTypeName("BOXING")
                        .build())
                .build();
    }

    private Training buildTraining() {
        return Training.builder()
                .id(ID)
                .name(TRAINING_NAME)
                .trainee(buildTrainee())
                .trainer(buildTrainer())
                .trainingType(TrainingType.builder().trainingTypeName("BOXING").build())
                .trainingDate(LocalDate.of(YEAR, MONTH, DAY))
                .trainingDuration(BigDecimal.valueOf(DURATION))
                .build();
    }

    private TrainerWorkloadMessage buildWorkloadMessage() {
        return new TrainerWorkloadMessage(TRAINER_USERNAME, "Mike", "Tyson", true,
                LocalDate.of(YEAR, MONTH, DAY), DURATION, ActionType.ADD);
    }
}