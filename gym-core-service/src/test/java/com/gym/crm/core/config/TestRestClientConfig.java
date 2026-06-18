package com.gym.crm.core.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestClient;

@TestConfiguration
public class TestRestClientConfig {

    @Bean
    public RestClient workloadRestClient(RestClient.Builder builder) {
        return builder
                .baseUrl("http://localhost:8082/workload-service/api/v1")
                .build();
    }
}