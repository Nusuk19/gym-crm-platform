package com.gym.crm.core.repository;

import com.gym.crm.core.model.User;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.jdbc.Sql;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.BEFORE_TEST_METHOD;

@Sql(scripts = "/datasets/user-insert.sql", executionPhase = BEFORE_TEST_METHOD)
class UserRepositoryTest extends AbstractRepositoryTest<UserRepository> {

    @Test
    void findByUsername_existingUser_returnsUserWithAllFields() {
        Optional<User> result = repository.findByUsername("Abdul.Hariton");

        assertThat(result).isPresent();
        User user = result.get();
        assertThat(user.getId()).isNotNull();
        assertThat(user.getFirstName()).isEqualTo("Abdul");
        assertThat(user.getLastName()).isEqualTo("Hariton");
        assertThat(user.getUsername()).isEqualTo("Abdul.Hariton");
        assertThat(user.getPassword()).isEqualTo("hashedPassword");
        assertThat(user.getIsActive()).isTrue();
    }

    @Test
    void findByUsername_inactiveUser_returnsUserWithCorrectActiveFlag() {
        Optional<User> result = repository.findByUsername("Mike.Tyson");

        assertThat(result).isPresent();
        User user = result.get();
        assertThat(user.getIsActive()).isFalse();
        assertThat(user.getFirstName()).isEqualTo("Mike");
    }

    @Test
    void findByUsername_nonExistingUser_returnsEmpty() {
        Optional<User> result = repository.findByUsername("ghost.user");

        assertThat(result).isEmpty();
    }

    @Test
    void save_update_firstName_updatesInDB() {
        User user = repository.findByUsername("Abdul.Hariton").orElseThrow();
        User updated = user.toBuilder().firstName("UpdatedName").build();

        repository.save(updated);

        flushAndClear();
        User result = repository.findByUsername("Abdul.Hariton").orElseThrow();
        assertThat(result.getFirstName()).isEqualTo("UpdatedName");
        assertThat(result.getLastName()).isEqualTo("Hariton");
        assertThat(result.getUsername()).isEqualTo("Abdul.Hariton");
        assertThat(result.getIsActive()).isTrue();
    }

    @Test
    void save_update_allFields_updatesAllInDB() {
        User user = repository.findByUsername("Abdul.Hariton").orElseThrow();
        User updated = user.toBuilder()
                .firstName("NewFirst")
                .lastName("NewLast")
                .password("newPassword")
                .isActive(false)
                .build();

        User result = repository.save(updated);

        flushAndClear();
        assertThat(result.getFirstName()).isEqualTo("NewFirst");
        assertThat(result.getLastName()).isEqualTo("NewLast");
        assertThat(result.getPassword()).isEqualTo("newPassword");
        assertThat(result.getIsActive()).isFalse();
        assertThat(result.getUsername()).isEqualTo("Abdul.Hariton");

        User fromDb = repository.findByUsername("Abdul.Hariton").orElseThrow();
        assertThat(fromDb.getFirstName()).isEqualTo("NewFirst");
        assertThat(fromDb.getPassword()).isEqualTo("newPassword");
        assertThat(fromDb.getIsActive()).isFalse();
    }

    @Test
    void save_update_isActive_toFalse_updatesInDB() {
        User user = repository.findByUsername("Abdul.Hariton").orElseThrow();
        User updated = user.toBuilder().isActive(false).build();

        repository.save(updated);

        flushAndClear();
        User result = repository.findByUsername("Abdul.Hariton").orElseThrow();
        assertThat(result.getIsActive()).isFalse();
    }
}