package com.gym.crm.workload.messaging;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.support.converter.MessageConverter;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Slf4j
@Component
@RequiredArgsConstructor
public class DeadLetterQueueProducer {

    private final JmsTemplate jmsTemplate;
    private final MessageConverter messageConverter;

    @Value("${app.jms.queue.dlq}")
    private String dlqQueue;

    public void send(TrainerWorkloadMessage originalMessage, String reason) {
        DeadLetterMessage deadLetterMessage = new DeadLetterMessage(originalMessage, reason, LocalDateTime.now());

        jmsTemplate.send(dlqQueue, session -> messageConverter.toMessage(deadLetterMessage, session));
        log.warn("Message sent to DLQ. queue={}, reason={}, trainer={}", dlqQueue, reason,
                originalMessage != null ? originalMessage.trainerUsername() : "unknown");
    }
}