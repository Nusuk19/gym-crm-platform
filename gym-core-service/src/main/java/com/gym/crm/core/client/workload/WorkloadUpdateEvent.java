package com.gym.crm.core.client.workload;

import com.gym.crm.core.client.workload.model.TrainerWorkloadRequest;

import java.util.List;

public record WorkloadUpdateEvent(List<TrainerWorkloadRequest> requests, String jwtToken, String transactionId) {
}
