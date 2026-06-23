package com.gym.crm.core.client.workload;

import com.gym.crm.core.client.workload.model.TrainerWorkloadRequest;
import com.gym.crm.core.exception.ServiceConnectionException;
import com.gym.crm.core.exception.ServiceException;
import com.gym.crm.core.exception.ServiceTimeoutException;
import com.gym.crm.core.logging.TransactionIdFilter;
import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.net.ConnectException;
import java.net.SocketTimeoutException;

@Slf4j
@Component
@RequiredArgsConstructor
public class WorkloadServiceClient {

    private static final String WORKLOAD_CB = "workload-service";
    private static final String TRAINER_WORKLOADS_PATH = "/trainer-workloads";
    private static final String CANNOT_CONNECT_MSG = "Cannot connect to ";

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

        throw mapToServiceException(ex);
    }

    private RuntimeException mapToServiceException(Throwable throwable) {
        if (throwable instanceof CallNotPermittedException) {
            return new ServiceConnectionException(CANNOT_CONNECT_MSG + WORKLOAD_CB + " (circuit breaker open)");
        }

        Throwable cause = getRootCause(throwable);

        if (cause instanceof ServiceException serviceException) {
            return serviceException;
        }
        if (cause instanceof SocketTimeoutException) {
            return new ServiceTimeoutException(WORKLOAD_CB + " did not respond within 3s");
        }
        if (cause instanceof ConnectException) {
            return new ServiceConnectionException(CANNOT_CONNECT_MSG + WORKLOAD_CB);
        }
        if (throwable instanceof ResourceAccessException) {
            return new ServiceConnectionException(CANNOT_CONNECT_MSG + WORKLOAD_CB);
        }

        return new RuntimeException("Unexpected error while communicating with " + WORKLOAD_CB);
    }

    private Throwable getRootCause(Throwable throwable) {
        Throwable current = throwable;
        while (current.getCause() != null && current.getCause() != current) {
            current = current.getCause();
        }

        return current;
    }
}