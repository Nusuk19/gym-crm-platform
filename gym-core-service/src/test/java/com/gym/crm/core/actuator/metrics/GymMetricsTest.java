package com.gym.crm.core.actuator.metrics;

import com.gym.crm.core.actuator.metrics.GymMetrics;
import com.gym.crm.core.repository.TraineeRepository;
import com.gym.crm.core.repository.TrainerRepository;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GymMetricsTest {

    @Mock
    private TraineeRepository traineeRepository;

    @Mock
    private TrainerRepository trainerRepository;

    private MeterRegistry registry;
    private GymMetrics gymMetrics;

    @BeforeEach
    void setUp() {
        registry = new SimpleMeterRegistry();
        gymMetrics = new GymMetrics(registry, traineeRepository, trainerRepository);
    }

    @Test
    void incrementTraineeRegistrations_incrementsCounter() {
        gymMetrics.incrementTraineeRegistrations();
        gymMetrics.incrementTraineeRegistrations();

        Counter counter = registry.find("gym_registrations_total").tag("role", "trainee").counter();
        double actual = counter.count();

        assertNotNull(counter);
        assertEquals(2.0, actual);
    }

    @Test
    void incrementTrainerRegistrations_incrementsCounter() {
        gymMetrics.incrementTrainerRegistrations();

        Counter counter = registry.find("gym_registrations_total").tag("role", "trainer").counter();
        double actual = counter.count();

        assertNotNull(counter);
        assertEquals(1.0, actual);
    }

    @Test
    void incrementTrainingsCreated_incrementsCounter() {
        gymMetrics.incrementTrainingsCreated();
        gymMetrics.incrementTrainingsCreated();
        gymMetrics.incrementTrainingsCreated();

        Counter counter = registry.find("gym_trainings_created_total").counter();
        double actual = counter.count();

        assertNotNull(counter);
        assertEquals(3.0, actual);
    }

    @Test
    void allCounters_startAtZero() {
        Counter traineeCounter = registry.find("gym_registrations_total").tag("role", "trainee").counter();
        Counter trainerCounter = registry.find("gym_registrations_total").tag("role", "trainer").counter();
        Counter trainingsCounter = registry.find("gym_trainings_created_total").counter();

        double traineeActual = traineeCounter.count();
        double trainerActual = trainerCounter.count();
        double trainingsActual = trainingsCounter.count();

        assertNotNull(traineeCounter);
        assertNotNull(trainerCounter);
        assertNotNull(trainingsCounter);
        assertEquals(0.0, traineeActual);
        assertEquals(0.0, trainerActual);
        assertEquals(0.0, trainingsActual);
    }

    @Test
    void activeTraineesGauge_returnsRepositoryCount() {
        when(traineeRepository.countByUserIsActiveTrue()).thenReturn(5L);

        double actual = registry.get("gym_active_users").tag("role", "trainee").gauge().value();

        assertEquals(5.0, actual);
    }

    @Test
    void activeTrainersGauge_returnsRepositoryCount() {
        when(trainerRepository.countByUserIsActiveTrue()).thenReturn(3L);

        double actual = registry.get("gym_active_users").tag("role", "trainer").gauge().value();

        assertEquals(3.0, actual);
    }

    @Test
    void traineeAndTrainerCounters_areIndependent() {
        gymMetrics.incrementTraineeRegistrations();
        gymMetrics.incrementTraineeRegistrations();
        gymMetrics.incrementTrainerRegistrations();

        double traineeActual = registry.find("gym_registrations_total").tag("role", "trainee").counter().count();
        double trainerActual = registry.find("gym_registrations_total").tag("role", "trainer").counter().count();

        assertEquals(2.0, traineeActual);
        assertEquals(1.0, trainerActual);
    }
}
