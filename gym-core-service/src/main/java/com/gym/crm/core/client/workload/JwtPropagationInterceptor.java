package com.gym.crm.core.client.workload;

import com.gym.crm.core.logging.TransactionIdFilter;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Slf4j
@Component
public class JwtPropagationInterceptor implements ClientHttpRequestInterceptor {

    private static final String BEARER_PREFIX = "Bearer ";

    @Override
    public ClientHttpResponse intercept(HttpRequest request, byte[] body,
                                        ClientHttpRequestExecution execution) throws IOException {
        String token = extractToken();
        if (token != null) {
            request.getHeaders().set(HttpHeaders.AUTHORIZATION, BEARER_PREFIX + token);
        } else {
            log.warn("No JWT token found in SecurityContext, request to workload-service will be unauthenticated");
        }

        String transactionId = MDC.get(TransactionIdFilter.TRANSACTION_ID_KEY);
        if (transactionId != null) {
            request.getHeaders().set(TransactionIdFilter.TRANSACTION_ID_HEADER, transactionId);
        }

        return execution.execute(request, body);
    }

    private String extractToken() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            return null;
        }

        Object credentials = authentication.getCredentials();

        return credentials instanceof String token ? token : null;
    }
}