package com.gym.crm.core.actuator;

import com.gym.crm.core.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

@Slf4j
@Component("activeUsers")
@RequiredArgsConstructor
public class ActiveUsersHealthIndicator implements HealthIndicator {

    private final UserRepository userRepository;

    @Override
    public Health health() {
        try {
            long totalUsers = userRepository.count();

            if (totalUsers == 0) {
                return noUsersHealth();
            }

            return Health.up()
                    .withDetail("totalUsers", totalUsers)
                    .build();
        } catch (Exception e) {
            log.error("Health check failed: cannot query users table", e);

            return Health.down()
                    .withDetail("reason", "Users table unreachable")
                    .withDetail("error", e.getClass().getSimpleName())
                    .build();
        }
    }

    private Health noUsersHealth() {
        log.warn("Health check: no users found in the system");
        return Health.up()
                .withDetail("warning", "No users found in the system")
                .withDetail("totalUsers", 0)
                .build();
    }
}