package com.gym.crm.core.security;

import com.gym.crm.core.security.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class JwtServiceTest {

    private static final String USERNAME = "Abdul.Hariton";
    private static final String SECRET = "404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970";
    private static final long EXPIRATION = 3600000L;

    private JwtService jwtService;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService(SECRET, EXPIRATION);
    }

    @Test
    void generateToken_returnsNonNullToken() {
        String token = jwtService.generateToken(USERNAME);

        assertThat(token).isNotNull().isNotBlank();
    }

    @Test
    void extractUsername_returnsCorrectUsername() {
        String token = jwtService.generateToken(USERNAME);

        String actual = jwtService.extractUsername(token);

        assertThat(actual).isEqualTo(USERNAME);
    }

    @Test
    void extractExpiration_returnsValidExpirationDate() {
        String token = jwtService.generateToken(USERNAME);

        assertThat(jwtService.extractExpiration(token)).isNotNull();
        assertThat(jwtService.extractExpiration(token).getTime()).isGreaterThan(System.currentTimeMillis());
    }

    @Test
    void isTokenValid_validToken_returnsTrue() {
        String token = jwtService.generateToken(USERNAME);

        assertThat(jwtService.isTokenValid(token)).isTrue();
    }

    @Test
    void isTokenValid_invalidToken_returnsFalse() {
        assertThat(jwtService.isTokenValid("invalid.token.value")).isFalse();
    }

    @Test
    void isTokenValid_expiredToken_returnsFalse() {
        JwtService expiredService = new JwtService(SECRET, -1000L);
        String token = expiredService.generateToken(USERNAME);

        assertThat(jwtService.isTokenValid(token)).isFalse();
    }

    @Test
    void generateToken_differentUsers_returnsDifferentTokens() {
        String token1 = jwtService.generateToken("user1.one");
        String token2 = jwtService.generateToken("user2.two");

        assertThat(token1).isNotEqualTo(token2);
    }
}