package com.gym.crm.core.actuator;

import com.gym.crm.core.actuator.ActiveUsersHealthIndicator;
import com.gym.crm.core.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.Status;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ActiveUsersHealthIndicatorTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private ActiveUsersHealthIndicator indicator;

    @Test
    void health_shouldReturnUp_whenUsersExist() {
        when(userRepository.count()).thenReturn(42L);

        Health health = indicator.health();

        assertThat(health.getStatus()).isEqualTo(Status.UP);
        assertThat(health.getDetails()).containsEntry("totalUsers", 42L);
    }

    @Test
    void health_shouldReturnDown_whenNoUsersExist() {
        when(userRepository.count()).thenReturn(0L);

        Health health = indicator.health();

        assertThat(health.getStatus()).isEqualTo(Status.UP);
        assertThat(health.getDetails()).containsEntry("warning", "No users found in the system");
        assertThat(health.getDetails()).containsEntry("totalUsers", 0);
    }

    @Test
    void health_shouldReturnDown_whenExceptionThrown() {
        when(userRepository.count()).thenThrow(new RuntimeException("DB error"));

        Health health = indicator.health();

        assertThat(health.getStatus()).isEqualTo(Status.DOWN);
        assertThat(health.getDetails()).containsEntry("reason", "Users table unreachable");
    }
}