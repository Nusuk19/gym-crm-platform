package com.gym.crm.automation.steps;

import com.gym.crm.automation.client.ApiClient;
import com.gym.crm.automation.client.WorkloadQueuePublisher;
import com.gym.crm.automation.config.TestProperties;
import com.gym.crm.automation.support.Payloads;
import com.gym.crm.automation.support.TestContext;
import com.gym.crm.automation.support.Unique;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import lombok.RequiredArgsConstructor;
import org.awaitility.Awaitility;

import java.time.Duration;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@RequiredArgsConstructor
public class WorkloadSteps {

    private static final String TRAINING_DATE = "2026-06-10";
    private static final String OTHER_MONTH_QUERY = "1";
    private static final String RECORDED_MONTH_QUERY = "6";
    private static final String YEAR_QUERY = "2026";
    private static final String WORKLOAD_ENDPOINT = "/api/v1/trainer-workloads";
    private static final String WORKLOAD_PROFILE_ENDPOINT = "/api/v1/trainer-workloads/%s";

    private final TestContext context;
    private final ApiClient workloadClient = new ApiClient(TestProperties.workloadBaseUrl());
    private final WorkloadQueuePublisher queuePublisher = new WorkloadQueuePublisher();

    @Given("a workload entry is added for a trainer with {int} minutes")
    public void aWorkloadEntryIsAddedForATrainerWithMinutes(int duration) {
        String username = Unique.username("Flow", "Trainer");

        context.put("workloadTrainerUsername", username);
        context.setLastResponse(workloadClient.put(WORKLOAD_ENDPOINT, context.getToken(),
                Payloads.workload(username, "Flow", "Trainer", true, TRAINING_DATE, duration, "ADD")));
    }

    @When("{int} minutes are removed from that trainer's workload")
    public void minutesAreRemovedFromThatTrainersWorkload(int duration) {
        context.setLastResponse(workloadClient.put(WORKLOAD_ENDPOINT, context.getToken(),
                Payloads.workload(context.get("workloadTrainerUsername"), "Flow", "Trainer",
                        true, TRAINING_DATE, duration, "DELETE")));
    }

    @When("workload is updated without a trainer username")
    public void workloadIsUpdatedWithoutATrainerUsername() {
        context.setLastResponse(workloadClient.put(WORKLOAD_ENDPOINT, context.getToken(),
                Payloads.workload(null, "Flow", "Trainer", true, TRAINING_DATE, 30, "ADD")));
    }

    @When("workload is updated without a token")
    public void workloadIsUpdatedWithoutAToken() {
        context.setLastResponse(workloadClient.put(WORKLOAD_ENDPOINT, null,
                Payloads.workload(Unique.username("Anon", "Trainer"), "Anon", "Trainer",
                        true, TRAINING_DATE, 30, "ADD")));
    }

    @When("that trainer's monthly workload is requested")
    public void thatTrainersMonthlyWorkloadIsRequested() {
        context.setLastResponse(workloadClient.get(String.format(WORKLOAD_PROFILE_ENDPOINT, context.get("workloadTrainerUsername")),
                context.getToken(), Map.of("year", YEAR_QUERY, "month", RECORDED_MONTH_QUERY)));
    }

    @When("that trainer's workload for a different month is requested")
    public void thatTrainersWorkloadForADifferentMonthIsRequested() {
        context.setLastResponse(workloadClient.get(String.format(WORKLOAD_PROFILE_ENDPOINT, context.get("workloadTrainerUsername")),
                context.getToken(), Map.of("year", YEAR_QUERY, "month", OTHER_MONTH_QUERY)));
    }

    @When("an unknown trainer's monthly workload is requested")
    public void anUnknownTrainersMonthlyWorkloadIsRequested() {
        String username = Unique.username("Unknown", "Trainer");
        context.setLastResponse(workloadClient.get(String.format(WORKLOAD_PROFILE_ENDPOINT, username),
                context.getToken(), Map.of("year", YEAR_QUERY, "month", RECORDED_MONTH_QUERY)));
    }

    @When("that trainer's monthly workload is requested without a token")
    public void thatTrainersMonthlyWorkloadIsRequestedWithoutAToken() {
        context.setLastResponse(workloadClient.get(String.format(WORKLOAD_PROFILE_ENDPOINT, context.get("workloadTrainerUsername")),
                null, Map.of("year", YEAR_QUERY, "month", RECORDED_MONTH_QUERY)));
    }

    @When("a raw workload message is published to the queue with {int} minutes")
    public void aRawWorkloadMessageIsPublishedToTheQueueWithMinutes(int duration) {
        String username = Unique.username("Queue", "Trainer");

        context.put("queueTrainerUsername", username);
        queuePublisher.publish(Payloads.workload(username, "Queue", "Trainer", true, TRAINING_DATE, duration, "ADD"));
    }

    @Then("that trainer's monthly workload eventually shows {int} minutes")
    public void thatTrainersMonthlyWorkloadEventuallyShowsMinutes(int expectedDuration) {
        String username = context.get("queueTrainerUsername");

        Awaitility.await()
                .atMost(Duration.ofSeconds(10))
                .pollInterval(Duration.ofMillis(500))
                .untilAsserted(() -> {
                    var response = workloadClient.get(
                            String.format(WORKLOAD_PROFILE_ENDPOINT, username),
                            context.getToken(), Map.of("year", YEAR_QUERY, "month", RECORDED_MONTH_QUERY));
                    assertThat(response.statusCode()).isEqualTo(200);
                    assertThat(response.jsonPath().getInt("trainingSummaryDuration")).isEqualTo(expectedDuration);
                });
    }
}