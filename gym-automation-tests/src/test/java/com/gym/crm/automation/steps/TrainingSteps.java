package com.gym.crm.automation.steps;

import com.gym.crm.automation.client.ApiClient;
import com.gym.crm.automation.config.TestProperties;
import com.gym.crm.automation.support.Payloads;
import com.gym.crm.automation.support.TestContext;
import com.gym.crm.automation.support.Unique;
import io.cucumber.java.en.When;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class TrainingSteps {

    private static final String TRAINING_DATE = "2026-08-01";

    private final TestContext context;
    private final ApiClient coreClient = new ApiClient(TestProperties.coreBaseUrl());

    @When("a training is created for the registered trainee and trainer")
    public void aTrainingIsCreatedForTheRegisteredTraineeAndTrainer() {
        int duration = 45;
        context.put("trainingDuration", String.valueOf(duration));
        context.setLastResponse(coreClient.post("/api/v1/trainings", context.getToken(), Payloads.training(
                Unique.name("Session"), TRAINING_DATE, duration, context.get("traineeUsername"), context.get("trainerUsername"))));
    }

    @When("a training is created for an unknown trainee")
    public void aTrainingIsCreatedForAnUnknownTrainee() {
        context.setLastResponse(coreClient.post("/api/v1/trainings", context.getToken(), Payloads.training(
                Unique.name("Session"), TRAINING_DATE, 45, "no.such.trainee", context.get("trainerUsername"))));
    }

    @When("a training is created with a non-positive duration")
    public void aTrainingIsCreatedWithANonPositiveDuration() {
        context.setLastResponse(coreClient.post("/api/v1/trainings", context.getToken(), Payloads.training(
                Unique.name("Session"), TRAINING_DATE, 0, context.get("traineeUsername"), context.get("trainerUsername"))));
    }
}
