package com.gym.crm.automation.client;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import lombok.RequiredArgsConstructor;

import java.util.Map;

@RequiredArgsConstructor
public class ApiClient {

    private final String baseUrl;

    public Response get(String path, String token, Map<String, ?> queryParams) {
        return request(token)
                .queryParams(queryParams)
                .get(baseUrl + path);
    }

    public Response post(String path, String token, Object body) {
        return request(token)
                .body(body)
                .post(baseUrl + path);
    }

    public Response put(String path, String token, Object body) {
        return request(token)
                .body(body)
                .put(baseUrl + path);
    }

    public Response delete(String path, String token) {
        return request(token)
                .delete(baseUrl + path);
    }

    private RequestSpecification request(String token) {
        RequestSpecification request = RestAssured.given()
                .relaxedHTTPSValidation()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON);

        if (token != null && !token.isBlank()) {
            request.header("Authorization", "Bearer " + token);
        }

        return request;
    }
}
