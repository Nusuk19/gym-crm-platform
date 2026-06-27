package com.gym.crm.workload.messaging;

import com.gym.crm.workload.exception.WorkloadMessageProcessingException;
import com.gym.crm.workload.logging.TransactionIdFilter;
import com.gym.crm.workload.openapi.ActionType;
import com.gym.crm.workload.openapi.TrainerWorkloadRequest;
import com.gym.crm.workload.service.TrainerWorkloadService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.MDC;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TrainerWorkloadMessageListenerTest {

    private static final String USERNAME = "abdul.hariton";
    private static final String FIRST_NAME = "Abdul";
    private static final String LAST_NAME = "Hariton";
    private static final int YEAR = 2026;
    private static final int MONTH = 6;
    private static final int DAY = 10;
    private static final int DURATION = 60;
    private static final String TRANSACTION_ID = "tx-123";
    private static final String VALIDATION_ERROR = "trainerUsername is missing";

    @Mock
    private TrainerWorkloadService service;
    @Mock
    private TrainerWorkloadMessageMapper mapper;
    @Mock
    private WorkloadMessageValidator validator;
    @Mock
    private DeadLetterQueueProducer dlqProducer;

    @InjectMocks
    private TrainerWorkloadMessageListener listener;

    @Test
    void handle_shouldProcessMessageAndCallService() {
        TrainerWorkloadMessage message = buildMessage();
        TrainerWorkloadRequest expected = new TrainerWorkloadRequest();
        when(validator.validate(message)).thenReturn(Optional.empty());
        when(mapper.toRequest(message)).thenReturn(expected);

        listener.handle(message, TRANSACTION_ID);

        verify(validator).validate(message);
        verify(mapper).toRequest(message);
        verify(service).updateTrainerWorkload(expected);
        verify(dlqProducer, never()).send(any(), anyString());
    }

    @Test
    void handle_shouldThrowInvalidMessageException_whenMessageIsInvalid() {
        TrainerWorkloadMessage message = buildMessage();
        when(validator.validate(message)).thenReturn(Optional.of(VALIDATION_ERROR));

        listener.handle(message, TRANSACTION_ID);

        verify(dlqProducer).send(message, VALIDATION_ERROR);
        verify(mapper, never()).toRequest(any());
        verify(service, never()).updateTrainerWorkload(any());
    }

    @Test
    void handle_shouldClearMdcAfterProcessing() {
        TrainerWorkloadMessage message = buildMessage();
        when(validator.validate(message)).thenReturn(Optional.empty());
        when(mapper.toRequest(message)).thenReturn(new TrainerWorkloadRequest());

        listener.handle(message, TRANSACTION_ID);

        assertThat(MDC.get(TransactionIdFilter.TRANSACTION_ID_KEY)).isNull();
    }

    @Test
    void handle_shouldClearMdc_afterDlqRouting() {
        TrainerWorkloadMessage message = buildMessage();
        when(validator.validate(message)).thenReturn(Optional.of(VALIDATION_ERROR));

        listener.handle(message, TRANSACTION_ID);

        assertThat(MDC.get(TransactionIdFilter.TRANSACTION_ID_KEY)).isNull();
    }

    @Test
    void handle_shouldUseUnknown_whenTransactionIdIsNull() {
        TrainerWorkloadMessage message = buildMessage();
        when(validator.validate(message)).thenReturn(Optional.empty());
        TrainerWorkloadRequest expected = new TrainerWorkloadRequest();
        when(mapper.toRequest(message)).thenReturn(expected);

        listener.handle(message, null);

        verify(service).updateTrainerWorkload(expected);
    }

    @Test
    void handle_shouldWrapAndPropagate_whenServiceThrowsRuntimeException() {
        TrainerWorkloadMessage message = buildMessage();
        RuntimeException cause = new RuntimeException("DB unavailable");
        when(validator.validate(message)).thenReturn(Optional.empty());
        when(mapper.toRequest(message)).thenReturn(new TrainerWorkloadRequest());
        doThrow(cause).when(service).updateTrainerWorkload(any());

        assertThatThrownBy(() -> listener.handle(message, TRANSACTION_ID))
                .isInstanceOf(WorkloadMessageProcessingException.class)
                .hasMessage("Failed to process workload message")
                .hasCause(cause);
        verify(dlqProducer, never()).send(any(), anyString());
    }

    private TrainerWorkloadMessage buildMessage() {
        return new TrainerWorkloadMessage(USERNAME, FIRST_NAME, LAST_NAME, true, LocalDate.of(YEAR, MONTH, DAY),
                DURATION, ActionType.ADD);
    }
}