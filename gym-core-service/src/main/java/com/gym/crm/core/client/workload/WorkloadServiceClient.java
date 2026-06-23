package com.gym.crm.core.client.workload;

import com.gym.crm.core.client.workload.model.TrainerWorkloadRequest;
import com.gym.crm.core.logging.TransactionIdFilter;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Slf4j
@Component
@RequiredArgsConstructor
public class WorkloadServiceClient {

    private static final String WORKLOAD_CB = "workload-service";
    private static final String TRAINER_WORKLOADS_PATH = "/trainer-workloads";

    private final RestClient workloadRestClient;

    @CircuitBreaker(name = WORKLOAD_CB, fallbackMethod = "fallback")
    public void updateTrainerWorkload(TrainerWorkloadRequest request) {
        String transactionId = MDC.get(TransactionIdFilter.TRANSACTION_ID_KEY);

        log.info("Calling workload-service [PUT {}] action={} trainer={} txId={}",
                TRAINER_WORKLOADS_PATH, request.getActionType(), request.getTrainerUsername(), transactionId);

        try {
            workloadRestClient.put()
                    .uri(TRAINER_WORKLOADS_PATH)
                    .body(request)
                    .retrieve()
                    .toBodilessEntity();

            log.info("workload-service responded 200 OK for trainer={} action={} txId={}",
                    request.getTrainerUsername(), request.getActionType(), transactionId);
        } catch (RestClientException ex) {
            log.error("workload-service call failed for trainer={} action={} txId={} message={}",
                    request.getTrainerUsername(), request.getActionType(), transactionId, ex.getMessage());
            throw ex;
        }
    }

    private void fallback(TrainerWorkloadRequest request, Exception ex) {
        log.error("Circuit breaker triggered for workload update: trainer={}, error={}",
                request.getTrainerUsername(), ex.getMessage());
    }
}