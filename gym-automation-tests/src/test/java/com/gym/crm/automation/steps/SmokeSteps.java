package com.gym.crm.automation.steps;

import com.gym.crm.automation.client.ApiClient;
import com.gym.crm.automation.config.TestProperties;
import com.gym.crm.automation.support.TestContext;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import lombok.RequiredArgsConstructor;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@RequiredArgsConstructor
public class SmokeSteps {

    private final TestContext context;

    @When("I check the health of {string} service")
    public void iCheckTheHealthOfService(String service) {
        ApiClient client = new ApiClient(baseUrlFor(service));
        context.setLastResponse(client.get("/actuator/health", null, Map.of()));
    }

    @Then("the service reports status {string}")
    public void theServiceReportsStatus(String expectedStatus) {
        assertThat(context.getLastResponse().statusCode()).isEqualTo(200);
        assertThat(context.getLastResponse().jsonPath().getString("status")).isEqualTo(expectedStatus);
    }

    private String baseUrlFor(String service) {
        return switch (service) {
            case "core" -> TestProperties.coreBaseUrl();
            case "workload" -> TestProperties.workloadBaseUrl();
            default -> throw new IllegalArgumentException("Unknown service: " + service);
        };
    }
}
