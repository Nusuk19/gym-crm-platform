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
public class TraineeSteps {

    private static final String REGISTER_ENDPOINT = "/api/v1/trainees/register";
    private static final String PROFILE_ENDPOINT = "/api/v1/trainees/%s";

    private final TestContext context;
    private final ApiClient coreClient = new ApiClient(TestProperties.coreBaseUrl());

    @Given("a registered trainee")
    public void aRegisteredTrainee() {
        String name = Unique.name("Trainee");
        Response response = coreClient.post(REGISTER_ENDPOINT, null, Payloads.trainee(name, name));

        context.put("traineeUsername", response.jsonPath().getString("username"));
    }

    @When("a new trainee registers")
    public void aNewTraineeRegisters() {
        String name = Unique.name("Trainee");

        context.setLastResponse(coreClient.post(REGISTER_ENDPOINT, null, Payloads.trainee(name, name)));
    }

    @When("a trainee registers with the following details:")
    public void aTraineeRegistersWithTheFollowingDetails(Map<String, String> details) {
        context.setLastResponse(coreClient.post(REGISTER_ENDPOINT, null, new LinkedHashMap<>(details)));
    }

    @When("a trainee registers without a last name")
    public void aTraineeRegistersWithoutALastName() {
        Map<String, Object> body = Payloads.trainee(Unique.name("Trainee"), null);

        context.setLastResponse(coreClient.post(REGISTER_ENDPOINT, null, body));
    }

    @When("a trainee registers, then registers again with the same name")
    public void aTraineeRegistersThenRegistersAgainWithTheSameName() {
        String name = Unique.name("Trainee");
        Response first = coreClient.post(REGISTER_ENDPOINT, null, Payloads.trainee(name, name));

        context.put("firstUsername", first.jsonPath().getString("username"));
        context.setLastResponse(coreClient.post(REGISTER_ENDPOINT, null, Payloads.trainee(name, name)));
    }

    @Given("another trainee is registered")
    public void anotherTraineeIsRegistered() {
        Response response = coreClient.post(REGISTER_ENDPOINT, null,
                Payloads.trainee("Other", "Trainee" + Unique.digits()));

        context.put("otherUsername", response.jsonPath().getString("username"));
    }

    @When("that other trainee's profile is requested with the current token")
    public void thatOtherTraineesProfileIsRequestedWithTheCurrentToken() {
        context.setLastResponse(coreClient.get(String.format(PROFILE_ENDPOINT, context.get("otherUsername")),
                context.getToken(), Map.of()));
    }

    @When("that other trainee's profile is requested without a token")
    public void thatOtherTraineesProfileIsRequestedWithoutAToken() {
        context.setLastResponse(coreClient.get(String.format(PROFILE_ENDPOINT, context.get("otherUsername")), null, Map.of()));
    }
}