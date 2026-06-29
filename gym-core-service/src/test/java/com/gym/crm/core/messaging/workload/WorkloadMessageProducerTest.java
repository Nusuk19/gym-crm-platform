package com.gym.crm.core.messaging.workload;

import com.gym.crm.core.logging.TransactionIdFilter;
import jakarta.jms.Message;
import jakarta.jms.Session;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.MDC;
import org.springframework.jms.JmsException;
import org.springframework.jms.UncategorizedJmsException;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;
import org.springframework.jms.support.converter.MessageConverter;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class WorkloadMessageProducerTest {

    private static final String QUEUE = "trainer-workload-queue";
    private static final String USERNAME = "abdul.hariton";
    private static final String FIRST_NAME = "Abdul";
    private static final String LAST_NAME = "Hariton";
    private static final int YEAR = 2026;
    private static final int MONTH = 6;
    private static final int DAY = 10;
    private static final int DURATION = 60;

    @Mock
    private JmsTemplate jmsTemplate;
    @Mock
    private MessageConverter messageConverter;

    private WorkloadMessageProducer producer;

    @BeforeEach
    void setUp() {
        producer = new WorkloadMessageProducer(jmsTemplate, messageConverter);
        ReflectionTestUtils.setField(producer, "workloadQueue", QUEUE);
    }

    @Test
    void send_shouldSendMessageToCorrectQueue() throws Exception {
        TrainerWorkloadMessage message = buildMessage();
        Session session = mock(Session.class);
        Message jmsMessage = mock(Message.class);
        when(messageConverter.toMessage(eq(message), any(Session.class))).thenReturn(jmsMessage);

        producer.send(message);

        ArgumentCaptor<MessageCreator> captor = ArgumentCaptor.forClass(MessageCreator.class);
        verify(jmsTemplate).send(eq(QUEUE), captor.capture());
        captor.getValue().createMessage(session);
        verify(messageConverter).toMessage(message, session);
    }

    @Test
    void send_shouldSetTransactionIdProperty_whenPresentInMdc() throws Exception {
        MDC.put(TransactionIdFilter.TRANSACTION_ID_KEY, "tx-123");
        TrainerWorkloadMessage message = buildMessage();
        Session session = mock(Session.class);
        Message jmsMessage = mock(Message.class);
        when(messageConverter.toMessage(eq(message), any(Session.class))).thenReturn(jmsMessage);

        producer.send(message);

        ArgumentCaptor<MessageCreator> captor = ArgumentCaptor.forClass(MessageCreator.class);
        verify(jmsTemplate).send(eq(QUEUE), captor.capture());
        captor.getValue().createMessage(session);
        verify(jmsMessage).setStringProperty(TransactionIdFilter.TRANSACTION_ID_HEADER, "tx-123");

        MDC.remove(TransactionIdFilter.TRANSACTION_ID_KEY);
    }

    @Test
    void send_shouldNotSetTransactionIdProperty_whenMdcIsEmpty() throws Exception {
        MDC.remove(TransactionIdFilter.TRANSACTION_ID_KEY);
        TrainerWorkloadMessage message = buildMessage();
        Session session = mock(Session.class);
        Message jmsMessage = mock(Message.class);
        when(messageConverter.toMessage(eq(message), any(Session.class))).thenReturn(jmsMessage);

        producer.send(message);

        ArgumentCaptor<MessageCreator> captor = ArgumentCaptor.forClass(MessageCreator.class);
        verify(jmsTemplate).send(eq(QUEUE), captor.capture());
        captor.getValue().createMessage(session);
        verify(jmsMessage, never()).setStringProperty(eq(TransactionIdFilter.TRANSACTION_ID_HEADER), any());
    }

    @Test
    void send_shouldNotThrow_whenJmsExceptionOccurs() {
        TrainerWorkloadMessage message = buildMessage();
        JmsException jmsException = new UncategorizedJmsException("broker unavailable");
        doThrow(jmsException).when(jmsTemplate).send(eq(QUEUE), any(MessageCreator.class));

        assertThatCode(() -> producer.send(message)).doesNotThrowAnyException();
    }

    private TrainerWorkloadMessage buildMessage() {
        return new TrainerWorkloadMessage(USERNAME, FIRST_NAME, LAST_NAME, true, LocalDate.of(YEAR, MONTH, DAY),
                DURATION, ActionType.ADD);
    }
}