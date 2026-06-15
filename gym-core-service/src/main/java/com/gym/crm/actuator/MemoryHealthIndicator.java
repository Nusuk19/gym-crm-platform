package com.gym.crm.actuator;

import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

import java.util.Locale;

@Component
public class MemoryHealthIndicator implements HealthIndicator {

    private static final double MEMORY_THRESHOLD_PERCENT = 0.9;

    @Override
    public Health health() {
        long maxMemory = getMaxMemory();
        long allocatedMemory = getTotalMemory();
        long freeMemory = getFreeMemory();

        if (maxMemory <= 0) {
            return Health.unknown()
                    .withDetail("reason", "Max memory undefined")
                    .build();
        }

        long usedMemory = allocatedMemory - freeMemory;
        double memoryUsagePercentage = (double) usedMemory / maxMemory;

        if (memoryUsagePercentage >= MEMORY_THRESHOLD_PERCENT) {
            return Health.down()
                    .withDetail("used_memory_bytes", usedMemory)
                    .withDetail("max_memory_bytes", maxMemory)
                    .withDetail("usage_percentage", memoryUsagePercentage * 100)
                    .withDetail("message", "Memory usage exceeded threshold")
                    .build();
        }

        return Health.up()
                .withDetail("free_memory_bytes", freeMemory)
                .withDetail("allocated_memory_bytes", allocatedMemory)
                .withDetail("max_memory_bytes", maxMemory)
                .withDetail("used_memory_bytes", usedMemory)
                .withDetail("usage_percentage", String.format(Locale.US, "%.2f%%", memoryUsagePercentage * 100))
                .build();
    }

    protected long getMaxMemory() {
        return Runtime.getRuntime().maxMemory();
    }

    protected long getTotalMemory() {
        return Runtime.getRuntime().totalMemory();
    }

    protected long getFreeMemory() {
        return Runtime.getRuntime().freeMemory();
    }
}