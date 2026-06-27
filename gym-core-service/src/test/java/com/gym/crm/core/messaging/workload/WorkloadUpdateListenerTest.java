package com.gym.crm.core.messaging.workload;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
class WorkloadUpdateListenerTest {

    private static final String FIRST_USERNAME = "abdul.hariton";
    private static final String SECOND_USERNAME = "zaris.exut";
    private static final int YEAR = 2026;
    private static final int MONTH = 6;
    private static final int DAY = 10;
    private static final int DURATION = 60;

    @Mock
    private WorkloadMessageProducer producer;

    @InjectMocks
    private WorkloadUpdateListener listener;

    @Test
    void handle_shouldSendEachMessage_whenEventContainsMultipleMessages() {
        TrainerWorkloadMessage first = buildMessage(FIRST_USERNAME, ActionType.ADD);
        TrainerWorkloadMessage second = buildMessage(SECOND_USERNAME, ActionType.DELETE);
        WorkloadUpdateEvent event = new WorkloadUpdateEvent(List.of(first, second));

        listener.handle(event);

        verify(producer).send(first);
        verify(producer).send(second);
    }

    @Test
    void handle_shouldNotInteractWithProducer_whenMessagesListIsEmpty() {
        WorkloadUpdateEvent event = new WorkloadUpdateEvent(List.of());

        listener.handle(event);

        verifyNoInteractions(producer);
    }

    private TrainerWorkloadMessage buildMessage(String username, ActionType actionType) {
        return new TrainerWorkloadMessage(username, "Mike", "Tyson", true,
                LocalDate.of(YEAR, MONTH, DAY), DURATION, actionType);
    }
}