package com.gym.crm.core.client.workload;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.io.IOException;
import java.util.List;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JwtPropagationInterceptorTest {

    @Mock
    private HttpRequest request;
    @Mock
    private HttpHeaders headers;
    @Mock
    private ClientHttpRequestExecution execution;
    @Mock
    private ClientHttpResponse response;

    private JwtPropagationInterceptor interceptor;

    @BeforeEach
    void setUp() {
        interceptor = new JwtPropagationInterceptor();
        SecurityContextHolder.clearContext();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void intercept_whenTokenPresent_shouldSetAuthorizationHeader() throws IOException {
        String token = "valid.jwt.token";
        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken("user", token, List.of());
        SecurityContextHolder.getContext().setAuthentication(auth);
        when(request.getHeaders()).thenReturn(headers);
        when(execution.execute(request, new byte[0])).thenReturn(response);

        interceptor.intercept(request, new byte[0], execution);

        verify(headers).set(HttpHeaders.AUTHORIZATION, "Bearer " + token);
        verify(execution).execute(request, new byte[0]);
    }

    @Test
    void intercept_whenNoAuthentication_shouldNotSetAuthorizationHeader() throws IOException {
        when(execution.execute(request, new byte[0])).thenReturn(response);

        interceptor.intercept(request, new byte[0], execution);

        verify(request, never()).getHeaders();
        verify(execution).execute(request, new byte[0]);
    }

    @Test
    void intercept_whenCredentialsAreNotString_shouldNotSetAuthorizationHeader() throws IOException {
        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken("user", null, List.of());
        SecurityContextHolder.getContext().setAuthentication(auth);
        when(execution.execute(request, new byte[0])).thenReturn(response);

        interceptor.intercept(request, new byte[0], execution);

        verify(request, never()).getHeaders();
        verify(execution).execute(request, new byte[0]);
    }
}