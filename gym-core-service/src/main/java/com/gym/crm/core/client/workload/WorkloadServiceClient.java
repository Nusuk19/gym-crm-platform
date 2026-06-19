package com.gym.crm.core.client.workload;

import com.gym.crm.core.client.workload.model.TrainerWorkloadRequest;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Slf4j
@Component
@RequiredArgsConstructor
public class WorkloadServiceClient {

    private static final String WORKLOAD_CB = "workload";

    private final RestClient workloadRestClient;

    @CircuitBreaker(name = WORKLOAD_CB, fallbackMethod = "fallback")
    public void updateTrainerWorkload(TrainerWorkloadRequest request) {
        workloadRestClient.put()
                .uri("/trainer-workloads")
                .body(request)
                .retrieve()
                .toBodilessEntity();
    }

    private void fallback(TrainerWorkloadRequest request, Exception ex) {
        log.error("Circuit breaker triggered for workload update: trainer={}, error={}",
                request.getTrainerUsername(), ex.getMessage());
    }
}