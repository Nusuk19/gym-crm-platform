package com.gym.crm.core.messaging.workload;

import java.util.List;

public record WorkloadUpdateEvent(List<TrainerWorkloadMessage> messages) {
}