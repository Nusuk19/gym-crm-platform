package com.gym.crm.workload.messaging;

import java.time.LocalDateTime;

public record DeadLetterMessage(
        TrainerWorkloadMessage originalMessage,
        String reason,
        LocalDateTime timestamp) {
}