package com.gym.crm.workload.repository;

import com.gym.crm.workload.config.AbstractMongoIntegrationTest;
import com.gym.crm.workload.config.MongoContainerTestConfig;
import com.gym.crm.workload.model.MonthSummary;
import com.gym.crm.workload.model.TrainerWorkload;
import com.gym.crm.workload.model.YearSummary;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataMongoTest
@ActiveProfiles("test")
class TrainerWorkloadRepositoryTest extends AbstractMongoIntegrationTest {

    private static final String USERNAME = "abdul.hariton";
    private static final String FIRST_NAME = "Abdul";
    private static final String LAST_NAME = "Hariton";
    private static final int YEAR = 2026;
    private static final int MONTH = 6;
    private static final int DURATION = 60;

    @Test
    void save_shouldPersistTrainerWorkload() {
        TrainerWorkload workload = buildWorkload();

        repository.save(workload);

        Optional<TrainerWorkload> actual = repository.findById(USERNAME);
        assertThat(actual).isPresent();
        assertThat(actual.get().getUsername()).isEqualTo(USERNAME);
        assertThat(actual.get().getTrainerFirstName()).isEqualTo(FIRST_NAME);
        assertThat(actual.get().getTrainerLastName()).isEqualTo(LAST_NAME);
        assertThat(actual.get().getIsActive()).isTrue();
    }

    @Test
    void findById_shouldReturnWorkload_whenExists() {
        repository.save(buildWorkload());

        Optional<TrainerWorkload> actual = repository.findById(USERNAME);

        assertThat(actual).isPresent();
        assertThat(actual.get().getUsername()).isEqualTo(USERNAME);
    }

    @Test
    void findById_shouldReturnEmpty_whenNotExists() {
        Optional<TrainerWorkload> actual = repository.findById("unknown.user");

        assertThat(actual).isEmpty();
    }

    @Test
    void save_shouldUpdateExistingWorkload_whenCalledTwice() {
        repository.save(buildWorkload());
        TrainerWorkload updated = buildWorkload();
        updated.setTrainerFirstName("UpdatedName");

        repository.save(updated);

        Optional<TrainerWorkload> actual = repository.findById(USERNAME);
        assertThat(actual).isPresent();
        assertThat(actual.get().getTrainerFirstName()).isEqualTo("UpdatedName");
    }

    @Test
    void save_shouldPersistYearAndMonthSummaries() {
        TrainerWorkload workload = buildWorkload();

        repository.save(workload);

        Optional<TrainerWorkload> actual = repository.findById(USERNAME);
        assertThat(actual).isPresent();
        assertThat(actual.get().getYears()).hasSize(1);
        assertThat(actual.get().getYears().get(0).getYear()).isEqualTo(YEAR);
        assertThat(actual.get().getYears().get(0).getMonths()).hasSize(1);
        assertThat(actual.get().getYears().get(0).getMonths().get(0).getMonth()).isEqualTo(MONTH);
        assertThat(actual.get().getYears().get(0).getMonths().get(0).getTrainingSummaryDuration()).isEqualTo(DURATION);
    }

    @Test
    void existsById_shouldReturnTrue_whenExists() {
        repository.save(buildWorkload());

        boolean actual = repository.existsById(USERNAME);

        assertThat(actual).isTrue();
    }

    @Test
    void existsById_shouldReturnFalse_whenNotExists() {
        boolean actual = repository.existsById("unknown.user");

        assertThat(actual).isFalse();
    }

    @Test
    void deleteAll_shouldRemoveAllWorkloads() {
        repository.save(buildWorkload());

        repository.deleteAll();

        assertThat(repository.findById(USERNAME)).isEmpty();
    }

    private TrainerWorkload buildWorkload() {
        MonthSummary monthSummary = MonthSummary.builder()
                .month(MONTH)
                .trainingSummaryDuration(DURATION)
                .build();

        YearSummary yearSummary = YearSummary.builder()
                .year(YEAR)
                .months(new ArrayList<>(List.of(monthSummary)))
                .build();

        return TrainerWorkload.builder()
                .username(USERNAME)
                .trainerFirstName(FIRST_NAME)
                .trainerLastName(LAST_NAME)
                .isActive(true)
                .years(new ArrayList<>(List.of(yearSummary)))
                .build();
    }
}