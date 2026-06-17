package com.gym.crm.workload.repository;

import com.gym.crm.workload.model.TrainerWorkload;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class TrainerWorkloadRepositoryTest {

    private static final String USERNAME = "abdul.hariton";
    private static final String UNKNOWN_USERNAME = "unknown.user";

    private final TrainerWorkloadRepositoryImpl repository = new TrainerWorkloadRepositoryImpl();

    @Test
    void save_shouldStoreTrainerWorkload() {
        TrainerWorkload expected = buildTrainer();

        TrainerWorkload actual = repository.save(expected);

        assertThat(actual).isSameAs(expected);
        assertThat(repository.existsByUsername(USERNAME)).isTrue();
    }

    @Test
    void findByUsername_shouldReturnTrainerWorkload_whenExists() {
        TrainerWorkload workload = buildTrainer();
        repository.save(workload);

        Optional<TrainerWorkload> actual = repository.findByUsername(USERNAME);

        assertThat(actual).isPresent();
        assertThat(actual.get().getTrainerUsername()).isEqualTo(USERNAME);
    }

    @Test
    void findByUsername_shouldReturnEmpty_whenNotExists() {
        Optional<TrainerWorkload> actual = repository.findByUsername(UNKNOWN_USERNAME);

        assertThat(actual).isEmpty();
    }

    @Test
    void existsByUsername_shouldReturnFalse_whenNotExists() {
        boolean actual = repository.existsByUsername(UNKNOWN_USERNAME);

        assertThat(actual).isFalse();
    }

    private TrainerWorkload buildTrainer() {
        return new TrainerWorkload(USERNAME, "Abdul", "Hariton", true, new ArrayList<>());
    }
}