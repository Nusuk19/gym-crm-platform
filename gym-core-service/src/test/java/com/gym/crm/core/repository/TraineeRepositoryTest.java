package com.gym.crm.core.repository;

import com.gym.crm.core.model.Trainee;
import com.gym.crm.core.model.User;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.jdbc.Sql;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.BEFORE_TEST_METHOD;

@Sql(scripts = {"/datasets/cleanup.sql", "/datasets/trainee-insert.sql"}, executionPhase = BEFORE_TEST_METHOD)
class TraineeRepositoryTest extends AbstractRepositoryTest<TraineeRepository> {

    @Test
    void findByUserUsername_existingUser_returnsTrainee() {
        Optional<Trainee> actual = repository.findByUserUsername("Abdul.Hariton");

        assertThat(actual).isPresent();
        assertThat(actual.get().getUser().getFirstName()).isEqualTo("Abdul");
        assertThat(actual.get().getUser().getLastName()).isEqualTo("Hariton");
        assertThat(actual.get().getAddress()).isEqualTo("123 Wolfs St");
        assertThat(actual.get().getDateOfBirth()).isEqualTo(LocalDate.of(1994, 5, 6));
    }

    @Test
    void findByUserUsername_nonExistingUser_returnsEmpty() {
        Optional<Trainee> actual = repository.findByUserUsername("ghost.user");

        assertThat(actual).isEmpty();
    }

    @Test
    void findAllWithUser_returnsAllTrainees() {
        List<Trainee> actual = repository.findAllWithUser();

        assertThat(actual).hasSize(1);
        assertThat(actual.iterator().next().getUser().getUsername()).isEqualTo("Abdul.Hariton");
    }

    @Test
    void save_newTrainee_persistsAllFields() {
        Trainee newTrainee = buildTrainee("Anna", "Koval", "Anna.Koval");
        Trainee saved = repository.save(newTrainee);
        Long expectedId = saved.getId();
        flushAndClear();

        Trainee actual = repository.findById(expectedId).orElseThrow();

        assertThat(actual.getId()).isEqualTo(expectedId);
        assertThat(actual.getUser().getFirstName()).isEqualTo("Anna");
        assertThat(actual.getUser().getLastName()).isEqualTo("Koval");
        assertThat(actual.getUser().getUsername()).isEqualTo("Anna.Koval");
        assertThat(actual.getUser().getPassword()).isEqualTo("pass123");
        assertThat(actual.getUser().getIsActive()).isTrue();
        assertThat(actual.getAddress()).isEqualTo("Lviv, Ukraine");
        assertThat(actual.getDateOfBirth()).isEqualTo(LocalDate.of(1997, 3, 22));
    }

    @Test
    void save_update_existingTrainee_persistsAllFields() {
        Trainee trainee = repository.findByUserUsername("Abdul.Hariton").orElseThrow();
        Long expectedId = trainee.getId();

        repository.save(trainee.toBuilder().address("New Address 456").build());
        flushAndClear();

        Trainee actual = repository.findById(expectedId).orElseThrow();

        assertThat(actual.getId()).isEqualTo(expectedId);
        assertThat(actual.getUser().getFirstName()).isEqualTo("Abdul");
        assertThat(actual.getUser().getLastName()).isEqualTo("Hariton");
        assertThat(actual.getUser().getUsername()).isEqualTo("Abdul.Hariton");
        assertThat(actual.getUser().getIsActive()).isTrue();
        assertThat(actual.getAddress()).isEqualTo("New Address 456");
        assertThat(actual.getDateOfBirth()).isEqualTo(LocalDate.of(1994, 5, 6));
    }

    @Test
    void deleteById_existingId_removesTrainee() {
        Long id = repository.findByUserUsername("Abdul.Hariton").orElseThrow().getId();

        repository.deleteById(id);
        flushAndClear();

        Optional<Trainee> deleted = repository.findById(id);
        List<Trainee> all = repository.findAll();

        assertThat(deleted).isEmpty();
        assertThat(all).isEmpty();
    }

    @Test
    void findByUserUsername_traineeHasAssignedTrainer() {
        Trainee trainee = repository.findByUserUsername("Abdul.Hariton").orElseThrow();

        assertThat(trainee.getTrainers()).hasSize(1);
        assertThat(trainee.getTrainers().iterator().next().getUser().getUsername()).isEqualTo("Mike.Tyson");
    }

    @Test
    void existsByUserUsername_existingUser_returnsTrue() {
        boolean result = repository.existsByUserUsername("Abdul.Hariton");

        assertThat(result).isTrue();
    }

    @Test
    void existsByUserUsername_nonExistingUser_returnsFalse() {
        boolean result = repository.existsByUserUsername("ghost.user");

        assertThat(result).isFalse();
    }

    private Trainee buildTrainee(String firstName, String lastName, String username) {
        User user = User.builder()
                .firstName(firstName)
                .lastName(lastName)
                .username(username)
                .password("pass123")
                .isActive(true)
                .build();

        return Trainee.builder()
                .user(user)
                .address("Lviv, Ukraine")
                .dateOfBirth(LocalDate.of(1997, 3, 22))
                .build();
    }
}