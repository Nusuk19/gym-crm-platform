package com.gym.crm.core.messaging.workload;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class WorkloadUpdateListener {

    private final WorkloadMessageProducer producer;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handle(WorkloadUpdateEvent event) {
        log.debug("Publishing {} workload messages after commit", event.messages().size());

        event.messages().forEach(producer::send);
    }
}