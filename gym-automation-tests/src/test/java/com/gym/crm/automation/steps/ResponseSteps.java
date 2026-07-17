package com.gym.crm.automation.steps;

import com.gym.crm.automation.support.TestContext;
import io.cucumber.java.en.Then;
import lombok.RequiredArgsConstructor;

import static org.assertj.core.api.Assertions.assertThat;

@RequiredArgsConstructor
public class ResponseSteps {

    private final TestContext context;

    @Then("the response status is {int}")
    public void theResponseStatusIs(int expectedStatus) {
        assertThat(context.getLastResponse().statusCode()).isEqualTo(expectedStatus);
    }

    @Then("the response error code is {int}")
    public void theResponseErrorCodeIs(int expectedErrorCode) {
        assertThat(context.getLastResponse().jsonPath().getInt("errorCode")).isEqualTo(expectedErrorCode);
    }

    @Then("the response contains a {string} field")
    public void theResponseContainsAField(String field) {
        assertThat((Object) context.getLastResponse().jsonPath().get(field)).isNotNull();
    }

    @Then("the {string} field differs from {string}")
    public void theFieldDiffersFromContextValue(String field, String contextKey) {
        String actual = context.getLastResponse().jsonPath().getString(field);
        assertThat(actual).isNotEqualTo(context.get(contextKey));
    }
}
