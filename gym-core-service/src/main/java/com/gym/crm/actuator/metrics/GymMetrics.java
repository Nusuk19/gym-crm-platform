package com.gym.crm.actuator.metrics;

import com.gym.crm.repository.TraineeRepository;
import com.gym.crm.repository.TrainerRepository;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.stereotype.Component;

@Component
public class GymMetrics {

    private static final String METRIC_REGISTRATIONS = "gym_registrations_total";
    private static final String METRIC_TRAININGS = "gym_trainings_created_total";
    private static final String METRIC_ACTIVE_USERS = "gym_active_users";
    private static final String TAG_ROLE = "role";

    private final Counter traineeRegistrationsCounter;
    private final Counter trainerRegistrationsCounter;
    private final Counter trainingsCreatedCounter;

    public GymMetrics(MeterRegistry registry,
                      TraineeRepository traineeRepository,
                      TrainerRepository trainerRepository) {
        this.traineeRegistrationsCounter = Counter.builder(METRIC_REGISTRATIONS)
                .tag(TAG_ROLE, "trainee")
                .description("Total number of trainee registrations")
                .register(registry);

        this.trainerRegistrationsCounter = Counter.builder(METRIC_REGISTRATIONS)
                .tag(TAG_ROLE, "trainer")
                .description("Total number of trainer registrations")
                .register(registry);

        this.trainingsCreatedCounter = Counter.builder(METRIC_TRAININGS)
                .description("Total number of trainings created")
                .register(registry);

        Gauge.builder(METRIC_ACTIVE_USERS, traineeRepository, TraineeRepository::countByUserIsActiveTrue)
                .tag(TAG_ROLE, "trainee")
                .description("Current number of active trainees")
                .register(registry);

        Gauge.builder(METRIC_ACTIVE_USERS, trainerRepository, TrainerRepository::countByUserIsActiveTrue)
                .tag(TAG_ROLE, "trainer")
                .description("Current number of active trainers")
                .register(registry);
    }

    public void incrementTraineeRegistrations() {
        traineeRegistrationsCounter.increment();
    }

    public void incrementTrainerRegistrations() {
        trainerRegistrationsCounter.increment();
    }

    public void incrementTrainingsCreated() {
        trainingsCreatedCounter.increment();
    }
}