package com.gym.crm.core.client.workload;

import com.gym.crm.core.client.workload.model.TrainerWorkloadRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class WorkloadUpdateListener {

    private final WorkloadServiceClient client;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handle(WorkloadUpdateEvent event) {
        try {
            restoreSecurityContext(event.jwtToken());
            event.requests().forEach(this::updateTrainerWorkload);
        } finally {
            SecurityContextHolder.clearContext();
        }
    }

    private void restoreSecurityContext(String jwtToken) {
        if (jwtToken == null) {
            log.warn("No JWT token available for workload update, request will be unauthenticated");
            return;
        }

        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(jwtToken, jwtToken, List.of());
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    private void updateTrainerWorkload(TrainerWorkloadRequest request) {
        try {
            client.updateTrainerWorkload(request);
        } catch (Exception ex) {
            log.error("Failed to update trainer workload for username={}", request.getTrainerUsername(), ex);
        }
    }
}