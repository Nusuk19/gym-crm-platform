package com.gym.crm.core.client.workload;

import com.gym.crm.core.client.workload.model.ActionType;
import com.gym.crm.core.client.workload.model.TrainerWorkloadRequest;
import com.gym.crm.core.config.TestRestClientConfig;
import com.gym.crm.core.logging.TransactionIdFilter;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.client.RestClientTest;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClientException;

import java.time.LocalDate;
import java.time.Month;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.content;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.jsonPath;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

@RestClientTest
@ContextConfiguration(classes = {WorkloadServiceClient.class, TestRestClientConfig.class})
class WorkloadServiceClientTest {

    private static final String USERNAME = "abdul.hariton";
    private static final String FIRST_NAME = "Abdul";
    private static final String LAST_NAME = "Hariton";
    private static final String TRANSACTION_ID = "tx-abc-123";

    @Autowired
    private WorkloadServiceClient client;

    @Autowired
    private MockRestServiceServer server;

    @AfterEach
    void tearDown() {
        MDC.clear();
    }

    @Test
    void updateTrainerWorkload_shouldSendPutRequest() {
        TrainerWorkloadRequest request = buildRequest();

        server.expect(requestTo("http://localhost:8082/workload-service/api/v1/trainer-workloads"))
                .andExpect(method(HttpMethod.PUT))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.trainerUsername").value(USERNAME))
                .andExpect(jsonPath("$.trainingDuration").value(60))
                .andExpect(jsonPath("$.actionType").value("ADD"))
                .andRespond(withSuccess());

        client.updateTrainerWorkload(request);

        server.verify();
    }

    @Test
    void updateTrainerWorkload_shouldPropagateTransactionIdHeader_whenPresentInMdc() {
        MDC.put(TransactionIdFilter.TRANSACTION_ID_KEY, TRANSACTION_ID);
        TrainerWorkloadRequest request = buildRequest();

        server.expect(requestTo("http://localhost:8082/workload-service/api/v1/trainer-workloads"))
                .andExpect(method(HttpMethod.PUT))
                .andExpect(header(TransactionIdFilter.TRANSACTION_ID_HEADER, TRANSACTION_ID))
                .andRespond(withSuccess());

        client.updateTrainerWorkload(request);

        server.verify();
    }

    @Test
    void updateTrainerWorkload_shouldThrow_whenServerReturnsError() {
        TrainerWorkloadRequest request = buildRequest();

        server.expect(requestTo("http://localhost:8082/workload-service/api/v1/trainer-workloads"))
                .andExpect(method(HttpMethod.PUT))
                .andRespond(withServerError());

        assertThatThrownBy(() -> client.updateTrainerWorkload(request)).isInstanceOf(RestClientException.class);

        server.verify();
    }

    private TrainerWorkloadRequest buildRequest() {
        return new TrainerWorkloadRequest()
                .trainerUsername(USERNAME)
                .trainerFirstName(FIRST_NAME)
                .trainerLastName(LAST_NAME)
                .isActive(true)
                .trainingDate(LocalDate.of(2026, Month.JUNE, 10))
                .trainingDuration(60)
                .actionType(ActionType.ADD);
    }
}
