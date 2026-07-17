package com.gym.crm.automation.steps;

import com.fasterxml.jackson.databind.JsonNode;
import com.gym.crm.automation.client.ApiClient;
import com.gym.crm.automation.client.DeadLetterQueueClient;
import com.gym.crm.automation.client.WorkloadQueuePublisher;
import com.gym.crm.automation.config.TestProperties;
import com.gym.crm.automation.support.Payloads;
import com.gym.crm.automation.support.TestContext;
import com.gym.crm.automation.support.Unique;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import lombok.RequiredArgsConstructor;
import org.awaitility.Awaitility;

import java.time.Duration;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@RequiredArgsConstructor
public class IntegrationSteps {

    private static final String TRAINING_YEAR = "2026";
    private static final String TRAINING_MONTH = "8";
    private static final String INVALID_MESSAGE_DATE = "2026-08-01";

    private final TestContext context;
    private final ApiClient workloadClient = new ApiClient(TestProperties.workloadBaseUrl());
    private final WorkloadQueuePublisher queuePublisher = new WorkloadQueuePublisher();
    private final DeadLetterQueueClient dlqClient = new DeadLetterQueueClient();

    @Then("the trainer's monthly workload eventually reflects the created training duration")
    public void theTrainersMonthlyWorkloadEventuallyReflectsTheCreatedTrainingDuration() {
        int expectedDuration = Integer.parseInt(context.get("trainingDuration"));
        awaitTrainerMonthlyWorkload(expectedDuration);
    }

    @When("the registered trainee deletes their own profile")
    public void theRegisteredTraineeDeletesTheirOwnProfile() {
        ApiClient coreClient = new ApiClient(TestProperties.coreBaseUrl());
        var loginResponse = coreClient.post("/api/v1/auth/login", null,
                Payloads.login(context.get("traineeUsername"), context.get("traineePassword")));
        String traineeToken = loginResponse.jsonPath().getString("token");

        context.setLastResponse(coreClient.delete("/api/v1/trainees/" + context.get("traineeUsername"), traineeToken));
    }

    @Then("the trainer's monthly workload eventually shows {int} minutes")
    public void theTrainersMonthlyWorkloadEventuallyShowsMinutes(int expectedDuration) {
        awaitTrainerMonthlyWorkload(expectedDuration);
    }

    private void awaitTrainerMonthlyWorkload(int expectedDuration) {
        String trainerUsername = context.get("trainerUsername");

        Awaitility.await()
                .atMost(Duration.ofSeconds(15))
                .pollInterval(Duration.ofMillis(500))
                .untilAsserted(() -> {
                    var response = workloadClient.get("/api/v1/trainer-workloads/" + trainerUsername,
                            context.getToken(), Map.of("year", TRAINING_YEAR, "month", TRAINING_MONTH));
                    assertThat(response.statusCode()).isEqualTo(200);
                    assertThat(response.jsonPath().getInt("trainingSummaryDuration")).isEqualTo(expectedDuration);
                });
    }

    @When("an invalid workload message is published to the queue")
    public void anInvalidWorkloadMessageIsPublishedToTheQueue() {
        String username = Unique.username("Invalid", "Trainer");
        context.put("dlqTrainerUsername", username);

        queuePublisher.publish(Payloads.workload(username, "Invalid", "Trainer",
                true, INVALID_MESSAGE_DATE, 0, "ADD"));
    }

    @Then("that message eventually appears on the dead letter queue with reason {string}")
    public void thatMessageEventuallyAppearsOnTheDeadLetterQueueWithReason(String expectedReason) {
        String username = context.get("dlqTrainerUsername");

        Optional<JsonNode> dlqMessage = dlqClient.awaitMessageForTrainer(username, Duration.ofSeconds(10));

        assertThat(dlqMessage).as("dead letter queue message for trainer " + username).isPresent();
        assertThat(dlqMessage.get().path("reason").asText()).isEqualTo(expectedReason);
    }
}
