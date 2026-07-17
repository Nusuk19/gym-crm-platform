package com.gym.crm.automation.steps;

import com.gym.crm.automation.client.ApiClient;
import com.gym.crm.automation.config.TestProperties;
import com.gym.crm.automation.support.DefaultUser;
import com.gym.crm.automation.support.Payloads;
import com.gym.crm.automation.support.TestContext;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.When;
import lombok.RequiredArgsConstructor;

import static org.assertj.core.api.Assertions.assertThat;

@RequiredArgsConstructor
public class AuthSteps {

    private static final String LOGIN_ENDPOINT = "/api/v1/auth/login";
    private static final String WRONG_PASSWORD = "wrong-password";

    private final TestContext context;
    private final ApiClient coreClient = new ApiClient(TestProperties.coreBaseUrl());

    @Given("an authenticated gym user")
    public void anAuthenticatedGymUser() {
        var response = coreClient.post(LOGIN_ENDPOINT, null,
                Payloads.login(DefaultUser.username(), DefaultUser.password()));
        assertThat(response.statusCode()).isEqualTo(200);

        context.setToken(response.jsonPath().getString("token"));
    }

    @When("the default user logs in with valid credentials")
    public void theDefaultUserLogsInWithValidCredentials() {
        context.setLastResponse(coreClient.post(LOGIN_ENDPOINT, null,
                Payloads.login(DefaultUser.username(), DefaultUser.password())));
    }

    @When("a user logs in with an unknown username")
    public void aUserLogsInWithAnUnknownUsername() {
        context.setLastResponse(coreClient.post(LOGIN_ENDPOINT, null,
                Payloads.login("unknown.user", "wrong-password")));
    }

    @When("the default user logs in with a wrong password")
    public void theDefaultUserLogsInWithAWrongPassword() {
        context.setLastResponse(coreClient.post(LOGIN_ENDPOINT, null,
                Payloads.login(DefaultUser.username(), "wrong-password")));
    }
}
