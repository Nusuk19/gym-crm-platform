package com.gym.crm.core.client.workload;

import com.gym.crm.core.client.workload.model.ActionType;
import com.gym.crm.core.client.workload.model.TrainerWorkloadRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class WorkloadUpdateListenerTest {

    private static final String JWT_TOKEN = "jwt-token";

    @Mock
    private WorkloadServiceClient client;

    @InjectMocks
    private WorkloadUpdateListener listener;

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void handle_shouldCallClientForEachRequest() {
        TrainerWorkloadRequest request = buildRequest();
        WorkloadUpdateEvent event = new WorkloadUpdateEvent(List.of(request), JWT_TOKEN);

        listener.handle(event);

        verify(client).updateTrainerWorkload(request);
    }

    @Test
    void handle_shouldNotThrow_whenClientFails() {
        TrainerWorkloadRequest request = buildRequest();
        WorkloadUpdateEvent event = new WorkloadUpdateEvent(List.of(request), JWT_TOKEN);

        doThrow(new RuntimeException("Connection failed")).when(client).updateTrainerWorkload(request);

        listener.handle(event);

        verify(client).updateTrainerWorkload(request);
    }

    @Test
    void handle_shouldClearSecurityContext_afterProcessing() {
        TrainerWorkloadRequest request = buildRequest();
        WorkloadUpdateEvent event = new WorkloadUpdateEvent(List.of(request), JWT_TOKEN);

        listener.handle(event);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }

    @Test
    void handle_shouldNotThrow_whenJwtTokenIsNull() {
        TrainerWorkloadRequest request = buildRequest();
        WorkloadUpdateEvent event = new WorkloadUpdateEvent(List.of(request), null);

        listener.handle(event);

        verify(client).updateTrainerWorkload(request);
    }

    private TrainerWorkloadRequest buildRequest() {
        return new TrainerWorkloadRequest()
                .trainerUsername("abdul.hariton")
                .trainerFirstName("Abdul")
                .trainerLastName("Hariton")
                .isActive(true)
                .trainingDate(LocalDate.of(2026, 6, 10))
                .trainingDuration(60)
                .actionType(ActionType.ADD);
    }
}