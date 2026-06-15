package com.gym.crm.repository;

import com.gym.crm.model.Trainer;
import com.gym.crm.model.TrainingType;
import com.gym.crm.model.User;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.jdbc.Sql;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.BEFORE_TEST_METHOD;

@Sql(scripts = {"/datasets/cleanup.sql", "/datasets/trainer-insert.sql"}, executionPhase = BEFORE_TEST_METHOD)
class TrainerRepositoryTest extends AbstractRepositoryTest<TrainerRepository> {

    @Test
    void findByUserUsername_existingTrainer_returnsFullTrainer() {
        Trainer actual = repository.findByUserUsername("Mike.Tyson").orElseThrow();

        assertThat(actual.getId()).isNotNull();
        assertThat(actual.getUser().getFirstName()).isEqualTo("Mike");
        assertThat(actual.getUser().getLastName()).isEqualTo("Tyson");
        assertThat(actual.getUser().getUsername()).isEqualTo("Mike.Tyson");
        assertThat(actual.getUser().getIsActive()).isTrue();
        assertThat(actual.getSpecialization()).isNotNull();
        assertThat(actual.getSpecialization().getTrainingTypeName()).isEqualTo("Boxing");
    }

    @Test
    void findByUserUsername_nonExisting_returnsEmpty() {
        Optional<Trainer> actual = repository.findByUserUsername("ghost.user");

        assertThat(actual).isEmpty();
    }

    @Test
    void findAllWithUserAndSpecialization_returnsAll() {
        List<Trainer> actual = repository.findAllWithUserAndSpecialization();

        List<String> usernames = actual.stream()
                .map(t -> t.getUser().getUsername())
                .toList();

        assertThat(actual).isNotEmpty();
        assertThat(usernames).contains("Mike.Tyson", "Adam.Future");
    }

    @Test
    void save_newTrainer_persistsAllFields() {
        Trainer trainer = buildTrainer("Bruce", "Lee", "Bruce.Lee");
        Trainer saved = repository.save(trainer);
        Long expectedId = saved.getId();
        flushAndClear();

        Trainer actual = repository.findById(expectedId).orElseThrow();

        assertThat(actual.getId()).isEqualTo(expectedId);
        assertThat(actual.getUser().getFirstName()).isEqualTo("Bruce");
        assertThat(actual.getUser().getLastName()).isEqualTo("Lee");
        assertThat(actual.getUser().getUsername()).isEqualTo("Bruce.Lee");
        assertThat(actual.getUser().getPassword()).isEqualTo("pass123");
        assertThat(actual.getUser().getIsActive()).isTrue();
        assertThat(actual.getSpecialization().getTrainingTypeName()).isEqualTo("Boxing");
    }

    @Test
    void save_update_existingTrainer_persistsAllFields() {
        Trainer trainer = repository.findByUserUsername("Mike.Tyson").orElseThrow();
        Long expectedId = trainer.getId();

        repository.save(trainer.toBuilder()
                .user(trainer.getUser().toBuilder().firstName("Michael").build())
                .build());
        flushAndClear();

        Trainer actual = repository.findById(expectedId).orElseThrow();

        assertThat(actual.getId()).isEqualTo(expectedId);
        assertThat(actual.getUser().getFirstName()).isEqualTo("Michael");
        assertThat(actual.getUser().getLastName()).isEqualTo("Tyson");
        assertThat(actual.getUser().getUsername()).isEqualTo("Mike.Tyson");
        assertThat(actual.getUser().getIsActive()).isTrue();
        assertThat(actual.getSpecialization().getTrainingTypeName()).isEqualTo("Boxing");
    }

    @Test
    void findAllActiveNotAssignedToTrainee_returnsOnlyUnassigned() {
        List<Trainer> actual = repository.findAllActiveNotAssignedToTrainee("Abdul.Hariton");

        List<String> usernames = actual.stream()
                .map(t -> t.getUser().getUsername())
                .toList();

        assertThat(usernames).isNotEmpty();
        assertThat(usernames).doesNotContain("Mike.Tyson");
    }

    @Test
    void findAllActiveNotAssignedToTrainee_nonExistingTrainee_returnsAll() {
        List<Trainer> actual = repository.findAllActiveNotAssignedToTrainee("ghost.user");

        List<String> usernames = actual.stream()
                .map(t -> t.getUser().getUsername())
                .toList();

        assertThat(usernames).contains("Mike.Tyson", "Adam.Future");
    }

    @Test
    void existsByUserUsername_existing_returnsTrue() {
        boolean result = repository.existsByUserUsername("Mike.Tyson");

        assertThat(result).isTrue();
    }

    @Test
    void existsByUserUsername_nonExisting_returnsFalse() {
        boolean result = repository.existsByUserUsername("ghost.user");

        assertThat(result).isFalse();
    }

    private Trainer buildTrainer(String firstName, String lastName, String username) {
        TrainingType boxing = em.getEntityManager()
                .createQuery("FROM TrainingType WHERE trainingTypeName = :name", TrainingType.class)
                .setParameter("name", "Boxing")
                .getSingleResult();

        User user = User.builder()
                .firstName(firstName)
                .lastName(lastName)
                .username(username)
                .password("pass123")
                .isActive(true)
                .build();

        return Trainer.builder()
                .user(user)
                .specialization(boxing)
                .build();
    }
}