package com.gym.crm.workload.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JwtAuthenticationFilterTest {

    @Mock
    private JwtService jwtService;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private FilterChain filterChain;

    @InjectMocks
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.clearContext();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void doFilterInternal_whenAuthHeaderIsNull_shouldSkipAuthentication() throws ServletException, IOException {
        when(request.getHeader(HttpHeaders.AUTHORIZATION)).thenReturn(null);

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        Authentication actual = SecurityContextHolder.getContext().getAuthentication();

        verify(filterChain).doFilter(request, response);
        verify(jwtService, never()).isTokenValid(any());
        assertNull(actual);
    }

    @Test
    void doFilterInternal_whenAuthHeaderHasNoBearerPrefix_shouldSkipAuthentication() throws ServletException, IOException {
        when(request.getHeader(HttpHeaders.AUTHORIZATION)).thenReturn("Basic sometoken");

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        Authentication actual = SecurityContextHolder.getContext().getAuthentication();

        verify(filterChain).doFilter(request, response);
        verify(jwtService, never()).isTokenValid(any());
        assertNull(actual);
    }

    @Test
    void doFilterInternal_whenTokenIsValid_shouldSetAuthentication() throws ServletException, IOException {
        String token = "valid.jwt.token";
        String username = "service-user";
        when(request.getHeader(HttpHeaders.AUTHORIZATION)).thenReturn("Bearer " + token);
        when(jwtService.isTokenValid(token)).thenReturn(true);
        when(jwtService.extractUsername(token)).thenReturn(username);

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        Authentication actual = SecurityContextHolder.getContext().getAuthentication();

        assertNotNull(actual);
        assertEquals(username, actual.getPrincipal());
        assertEquals(token, actual.getCredentials());
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void doFilterInternal_whenTokenIsInvalid_shouldSkipAuthentication() throws ServletException, IOException {
        String token = "invalid.jwt.token";
        when(request.getHeader(HttpHeaders.AUTHORIZATION)).thenReturn("Bearer " + token);
        when(jwtService.isTokenValid(token)).thenReturn(false);

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        Authentication actual = SecurityContextHolder.getContext().getAuthentication();

        assertNull(actual);
        verify(jwtService, never()).extractUsername(any());
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void doFilterInternal_whenAuthenticationAlreadyExists_shouldNotOverrideAuthentication() throws ServletException, IOException {
        String token = "valid.jwt.token";
        String existingUsername = "already-authenticated-user";
        when(request.getHeader(HttpHeaders.AUTHORIZATION)).thenReturn("Bearer " + token);
        when(jwtService.isTokenValid(token)).thenReturn(true);

        UsernamePasswordAuthenticationToken existingAuth = new UsernamePasswordAuthenticationToken(existingUsername, null, List.of());
        SecurityContextHolder.getContext().setAuthentication(existingAuth);

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        Authentication actual = SecurityContextHolder.getContext().getAuthentication();

        assertEquals(existingUsername, actual.getPrincipal());
        verify(jwtService, never()).extractUsername(any());
        verify(filterChain).doFilter(request, response);
    }
}