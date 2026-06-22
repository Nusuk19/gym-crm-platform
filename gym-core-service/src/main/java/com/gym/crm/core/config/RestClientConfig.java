package com.gym.crm.core.config;

import com.gym.crm.core.client.workload.JwtPropagationInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
@RequiredArgsConstructor
public class RestClientConfig {

    private final JwtPropagationInterceptor jwtPropagationInterceptor;

    @Value("${workload.service.base-url}")
    private String baseUrl;

    @Bean
    @LoadBalanced
    public RestClient.Builder loadBalancedRestClientBuilder() {
        return RestClient.builder();
    }

    @Bean
    public RestClient workloadRestClient(RestClient.Builder loadBalancedRestClientBuilder) {
        return loadBalancedRestClientBuilder
                .baseUrl(baseUrl)
                .requestInterceptor(jwtPropagationInterceptor)
                .build();
    }
}