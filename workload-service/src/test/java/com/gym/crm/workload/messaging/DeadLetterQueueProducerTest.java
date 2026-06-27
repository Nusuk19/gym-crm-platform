package com.gym.crm.workload.messaging;

import com.gym.crm.workload.openapi.ActionType;
import jakarta.jms.Message;
import jakarta.jms.Session;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;
import org.springframework.jms.support.converter.MessageConverter;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DeadLetterQueueProducerTest {

    private static final String DLQ = "ActiveMQ.DLQ";
    private static final String USERNAME = "abdul.hariton";
    private static final String FIRST_NAME = "Abdul";
    private static final String LAST_NAME = "Hariton";
    private static final int YEAR = 2026;
    private static final int MONTH = 6;
    private static final int DAY = 10;
    private static final int DURATION = 60;
    private static final String REASON = "trainerUsername is missing";

    @Mock
    private JmsTemplate jmsTemplate;
    @Mock
    private MessageConverter messageConverter;

    private DeadLetterQueueProducer producer;

    @BeforeEach
    void setUp() {
        producer = new DeadLetterQueueProducer(jmsTemplate, messageConverter);
        ReflectionTestUtils.setField(producer, "dlqQueue", DLQ);
    }

    @Test
    void send_shouldSendDeadLetterMessageToCorrectQueue() throws Exception {
        TrainerWorkloadMessage originalMessage = buildMessage();
        Session session = mock(Session.class);
        Message jmsMessage = mock(Message.class);
        when(messageConverter.toMessage(any(DeadLetterMessage.class), any(Session.class))).thenReturn(jmsMessage);

        producer.send(originalMessage, REASON);

        ArgumentCaptor<MessageCreator> creatorCaptor = ArgumentCaptor.forClass(MessageCreator.class);
        verify(jmsTemplate).send(eq(DLQ), creatorCaptor.capture());
        creatorCaptor.getValue().createMessage(session);

        ArgumentCaptor<DeadLetterMessage> dlqCaptor = ArgumentCaptor.forClass(DeadLetterMessage.class);
        verify(messageConverter).toMessage(dlqCaptor.capture(), eq(session));
        assertThat(dlqCaptor.getValue().originalMessage()).isEqualTo(originalMessage);
        assertThat(dlqCaptor.getValue().reason()).isEqualTo(REASON);
        assertThat(dlqCaptor.getValue().timestamp()).isNotNull();
    }

    @Test
    void send_shouldHandleNullOriginalMessage() throws Exception {
        Session session = mock(Session.class);
        Message jmsMessage = mock(Message.class);
        when(messageConverter.toMessage(any(DeadLetterMessage.class), any(Session.class))).thenReturn(jmsMessage);

        producer.send(null, REASON);

        ArgumentCaptor<MessageCreator> creatorCaptor = ArgumentCaptor.forClass(MessageCreator.class);
        verify(jmsTemplate).send(eq(DLQ), creatorCaptor.capture());
        creatorCaptor.getValue().createMessage(session);

        ArgumentCaptor<DeadLetterMessage> dlqCaptor = ArgumentCaptor.forClass(DeadLetterMessage.class);
        verify(messageConverter).toMessage(dlqCaptor.capture(), eq(session));
        assertThat(dlqCaptor.getValue().originalMessage()).isNull();
        assertThat(dlqCaptor.getValue().reason()).isEqualTo(REASON);
    }

    private TrainerWorkloadMessage buildMessage() {
        return new TrainerWorkloadMessage(USERNAME, FIRST_NAME, LAST_NAME, true, LocalDate.of(YEAR, MONTH, DAY),
                DURATION, ActionType.ADD);
    }
}