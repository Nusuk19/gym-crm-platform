package com.gym.crm.automation.steps;

import com.gym.crm.automation.client.ApiClient;
import com.gym.crm.automation.config.TestProperties;
import com.gym.crm.automation.support.Payloads;
import com.gym.crm.automation.support.TestContext;
import com.gym.crm.automation.support.Unique;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.When;
import io.restassured.response.Response;
import lombok.RequiredArgsConstructor;

import java.util.LinkedHashMap;
import java.util.Map;

@RequiredArgsConstructor
public class TrainerSteps {

    private final TestContext context;
    private final ApiClient coreClient = new ApiClient(TestProperties.coreBaseUrl());

    @Given("a registered trainer")
    public void aRegisteredTrainer() {
        String name = Unique.name("Trainer");
        Response response = coreClient.post("/api/v1/trainers/register", null,
                Payloads.trainer(name, name, "Cardio"));

        context.put("trainerUsername", response.jsonPath().getString("username"));
    }

    @When("a new trainer registers with specialization {string}")
    public void aNewTrainerRegistersWithSpecialization(String specialization) {
        String name = Unique.name("Trainer");

        context.setLastResponse(coreClient.post("/api/v1/trainers/register", null,
                Payloads.trainer(name, name, specialization)));
    }

    @When("a trainer registers with the following details:")
    public void aTrainerRegistersWithTheFollowingDetails(Map<String, String> details) {
        context.setLastResponse(coreClient.post("/api/v1/trainers/register", null, new LinkedHashMap<>(details)));
    }

    @When("a trainer registers without a first name")
    public void aTrainerRegistersWithoutAFirstName() {
        context.setLastResponse(coreClient.post("/api/v1/trainers/register", null,
                Payloads.trainer(null, Unique.name("Trainer"), "Cardio")));
    }
}
