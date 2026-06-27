package com.gym.crm.workload.messaging;

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

import static org.assertj.core.api.Assertions.assertThat;
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

    @Mock
    private TrainerWorkloadService service;
    @Mock
    private TrainerWorkloadMessageMapper mapper;

    @InjectMocks
    private TrainerWorkloadMessageListener listener;

    @Test
    void handle_shouldProcessMessageAndCallService() {
        TrainerWorkloadMessage message = buildMessage();
        TrainerWorkloadRequest expected = new TrainerWorkloadRequest();
        when(mapper.toRequest(message)).thenReturn(expected);

        listener.handle(message, TRANSACTION_ID);

        verify(mapper).toRequest(message);
        verify(service).updateTrainerWorkload(expected);
    }

    @Test
    void handle_shouldClearMdcAfterProcessing() {
        TrainerWorkloadMessage message = buildMessage();
        when(mapper.toRequest(message)).thenReturn(new TrainerWorkloadRequest());

        listener.handle(message, TRANSACTION_ID);

        assertThat(MDC.get(TransactionIdFilter.TRANSACTION_ID_KEY)).isNull();
    }

    @Test
    void handle_shouldUseUnknown_whenTransactionIdIsNull() {
        TrainerWorkloadMessage message = buildMessage();
        TrainerWorkloadRequest expected = new TrainerWorkloadRequest();
        when(mapper.toRequest(message)).thenReturn(expected);

        listener.handle(message, null);

        verify(service).updateTrainerWorkload(expected);
    }

    private TrainerWorkloadMessage buildMessage() {
        return new TrainerWorkloadMessage(USERNAME, FIRST_NAME, LAST_NAME, true, LocalDate.of(YEAR, MONTH, DAY),
                DURATION, ActionType.ADD);
    }
}