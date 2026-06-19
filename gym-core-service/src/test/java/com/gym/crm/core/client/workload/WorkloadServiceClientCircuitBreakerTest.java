package com.gym.crm.core.client.workload;

import com.gym.crm.core.client.workload.model.ActionType;
import com.gym.crm.core.client.workload.model.TrainerWorkloadRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.client.RestClient;

import java.time.LocalDate;
import java.time.Month;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ActiveProfiles("test")
class WorkloadServiceClientCircuitBreakerTest {

    @MockBean
    private RestClient workloadRestClient;

    @Autowired
    private WorkloadServiceClient client;

    @BeforeEach
    void setUp() {
        var putSpec = mock(RestClient.RequestBodyUriSpec.class);
        var bodySpec = mock(RestClient.RequestBodySpec.class);
        var responseSpec = mock(RestClient.ResponseSpec.class);

        when(workloadRestClient.put()).thenReturn(putSpec);
        when(putSpec.uri("/trainer-workloads")).thenReturn(bodySpec);
        when(bodySpec.body(any())).thenReturn(bodySpec);
        when(bodySpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.toBodilessEntity()).thenThrow(new RuntimeException("Service unavailable"));
    }

    @Test
    void updateTrainerWorkload_shouldNotThrow_whenServiceFails() {
        assertDoesNotThrow(() -> client.updateTrainerWorkload(buildRequest()));
    }

    private TrainerWorkloadRequest buildRequest() {
        return new TrainerWorkloadRequest()
                .trainerUsername("abdul.hariton")
                .trainerFirstName("Abdul")
                .trainerLastName("Hariton")
                .isActive(true)
                .trainingDate(LocalDate.of(2026, Month.JUNE, 10))
                .trainingDuration(60)
                .actionType(ActionType.ADD);
    }
}