package com.gym.crm.core.messaging.workload;

import com.gym.crm.core.logging.TransactionIdFilter;
import jakarta.jms.Message;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.support.converter.MessageConverter;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class WorkloadMessageProducer {

    private final JmsTemplate jmsTemplate;
    private final MessageConverter messageConverter;

    @Value("${app.jms.queue.workload}")
    private String workloadQueue;

    public void send(TrainerWorkloadMessage message) {
        String transactionId = MDC.get(TransactionIdFilter.TRANSACTION_ID_KEY);

        jmsTemplate.send(workloadQueue, session -> {
            Message jmsMessage = messageConverter.toMessage(message, session);

            if (transactionId != null) {
                jmsMessage.setStringProperty(TransactionIdFilter.TRANSACTION_ID_HEADER, transactionId);
            }

            return jmsMessage;
        });

        log.info("Workload message sent. queue={}, trainer={}, action={}, transactionId={}",
                workloadQueue, message.trainerUsername(), message.actionType(), transactionId);
    }
}