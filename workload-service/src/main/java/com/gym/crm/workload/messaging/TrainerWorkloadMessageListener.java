package com.gym.crm.workload.messaging;

import com.gym.crm.workload.logging.TransactionIdFilter;
import com.gym.crm.workload.service.TrainerWorkloadService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class TrainerWorkloadMessageListener {

    private final TrainerWorkloadService service;
    private final TrainerWorkloadMessageMapper mapper;

    @JmsListener(destination = "${app.jms.queue.workload}")
    public void handle(TrainerWorkloadMessage message,
                       @Header(value = TransactionIdFilter.TRANSACTION_ID_HEADER, required = false) String transactionId) {
        String txId = transactionId != null ? transactionId : "unknown";
        MDC.put(TransactionIdFilter.TRANSACTION_ID_KEY, txId);

        try {
            log.info("Workload message received. trainer={}, action={}, transactionId={}",
                    message.trainerUsername(), message.actionType(), txId);

            service.updateTrainerWorkload(mapper.toRequest(message));

            log.info("Workload message processed. trainer={}, action={}, transactionId={}",
                    message.trainerUsername(), message.actionType(), txId);
        } finally {
            MDC.remove(TransactionIdFilter.TRANSACTION_ID_KEY);
        }
    }
}