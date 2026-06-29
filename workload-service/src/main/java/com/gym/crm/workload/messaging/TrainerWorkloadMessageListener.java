package com.gym.crm.workload.messaging;

import com.gym.crm.workload.exception.InvalidMessageException;
import com.gym.crm.workload.exception.WorkloadMessageProcessingException;
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
    private final WorkloadMessageValidator validator;
    private final DeadLetterQueueProducer dlqProducer;

    @JmsListener(destination = "${app.jms.queue.workload}")
    public void handle(TrainerWorkloadMessage message,
                       @Header(value = TransactionIdFilter.TRANSACTION_ID_HEADER, required = false) String transactionId) {
        String txId = transactionId != null ? transactionId : "unknown";
        MDC.put(TransactionIdFilter.TRANSACTION_ID_KEY, txId);

        try {
            log.info("Workload message received. trainer={}, action={}, transactionId={}",
                    message.trainerUsername(), message.actionType(), txId);
            validator.validate(message).ifPresent(reason -> {
                log.warn("Invalid workload message, routing to DLQ. reason={}, transactionId={}", reason, txId);

                throw new InvalidMessageException(reason);
            });

            processMessage(message, txId);

            log.info("Workload message processed. trainer={}, action={}, transactionId={}",
                    message.trainerUsername(), message.actionType(), txId);
        } catch (InvalidMessageException e) {
            dlqProducer.send(message, e.getMessage());
        } finally {
            MDC.remove(TransactionIdFilter.TRANSACTION_ID_KEY);
        }
    }

    private void processMessage(TrainerWorkloadMessage message, String txId) {
        try {
            service.updateTrainerWorkload(mapper.toRequest(message));
        } catch (RuntimeException e) {
            log.error("Failed to process workload message. trainer={}, action={}, transactionId={}",
                    message.trainerUsername(), message.actionType(), txId, e);

            throw new WorkloadMessageProcessingException("Failed to process workload message", e);
        }
    }
}