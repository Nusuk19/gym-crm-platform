package com.gym.crm.core.security;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class JwtTokenExtractorTest {

    private JwtTokenExtractor jwtTokenExtractor;

    @BeforeEach
    void setUp() {
        jwtTokenExtractor = new JwtTokenExtractor();
        SecurityContextHolder.clearContext();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void extract_whenTokenPresent_shouldReturnToken() {
        String expected = "valid.jwt.token";
        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken("user", expected, List.of());
        SecurityContextHolder.getContext().setAuthentication(auth);

        String actual = jwtTokenExtractor.extract();

        assertEquals(expected, actual);
    }

    @Test
    void extract_whenNoAuthentication_shouldReturnNull() {
        String actual = jwtTokenExtractor.extract();

        assertNull(actual);
    }

    @Test
    void extract_whenCredentialsAreNotString_shouldReturnNull() {
        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken("user", null, List.of());
        SecurityContextHolder.getContext().setAuthentication(auth);

        String actual = jwtTokenExtractor.extract();

        assertNull(actual);
    }
}