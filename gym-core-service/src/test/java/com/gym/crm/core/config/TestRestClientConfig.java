package com.gym.crm.core.config;

import com.gym.crm.core.client.workload.JwtPropagationInterceptor;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestClient;

@TestConfiguration
public class TestRestClientConfig {

    @Bean
    public JwtPropagationInterceptor jwtPropagationInterceptor() {
        return new JwtPropagationInterceptor();
    }

    @Bean
    public CircuitBreakerRegistry circuitBreakerRegistry() {
        return CircuitBreakerRegistry.ofDefaults();
    }

    @Bean
    public RestClient workloadRestClient(RestClient.Builder builder, JwtPropagationInterceptor interceptor) {
        return builder
                .baseUrl("http://localhost:8082/workload-service/api/v1")
                .requestInterceptor(interceptor)
                .build();
    }
}
