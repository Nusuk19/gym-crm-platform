package com.gym.crm.automation.hooks;

import com.gym.crm.automation.client.ApiClient;
import com.gym.crm.automation.config.TestProperties;
import com.gym.crm.automation.support.DefaultUser;
import com.gym.crm.automation.support.Payloads;
import com.gym.crm.automation.support.Unique;
import io.cucumber.java.BeforeAll;
import io.restassured.response.Response;

public class Hooks {

    @BeforeAll
    public static void registerDefaultUser() {
        ApiClient coreClient = new ApiClient(TestProperties.coreBaseUrl());
        String name = Unique.name("DefaultUser");

        Response response = coreClient.post("/api/v1/trainees/register", null, Payloads.trainee(name, name));
        if (response.statusCode() != 200) {
            throw new IllegalStateException(String.format("Failed to bootstrap the default user: %s %s",
                    response.statusCode(), response.getBody().asString()));
        }

        DefaultUser.set(response.jsonPath().getString("username"), response.jsonPath().getString("password"));
    }
}
