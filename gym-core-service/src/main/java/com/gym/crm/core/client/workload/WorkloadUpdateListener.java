package com.gym.crm.core.client.workload;

import com.gym.crm.core.client.workload.model.TrainerWorkloadRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class WorkloadUpdateListener {

    private final WorkloadServiceClient client;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handle(WorkloadUpdateEvent event) {
        event.requests().forEach(this::updateTrainerWorkload);
    }

    private void updateTrainerWorkload(TrainerWorkloadRequest request) {
        try {
            client.updateTrainerWorkload(request);
        } catch (Exception ex) {
            log.error("Failed to update trainer workload for username={}", request.getTrainerUsername(), ex);
        }
    }
}