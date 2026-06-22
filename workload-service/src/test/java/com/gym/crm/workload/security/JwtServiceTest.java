package com.gym.crm.workload.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.crypto.SecretKey;
import java.util.Base64;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JwtServiceTest {

    private static final String SECRET = Base64.getEncoder().encodeToString(
            "my-super-secret-key-for-testing-purposes-only-256bit".getBytes());

    private JwtService jwtService;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService(SECRET);
    }

    @Test
    void extractUsername_whenValidToken_shouldReturnUsername() {
        String expected = "trainer-service";
        String token = buildToken(expected, new Date(System.currentTimeMillis() + 60_000));

        String actual = jwtService.extractUsername(token);

        assertEquals(expected, actual);
    }

    @Test
    void isTokenValid_whenValidToken_shouldReturnTrue() {
        String token = buildToken("trainer-service", new Date(System.currentTimeMillis() + 60_000));

        boolean actual = jwtService.isTokenValid(token);

        assertTrue(actual);
    }

    @Test
    void isTokenValid_whenExpiredToken_shouldReturnFalse() {
        String token = buildToken("trainer-service", new Date(System.currentTimeMillis() - 1_000));

        boolean actual = jwtService.isTokenValid(token);

        assertFalse(actual);
    }

    @Test
    void isTokenValid_whenTokenSignedWithWrongKey_shouldReturnFalse() {

        String wrongSecret = Base64.getEncoder().encodeToString(
                "wrong-secret-key-for-testing-purposes-only-256bit!".getBytes());
        SecretKey wrongKey = Keys.hmacShaKeyFor(Decoders.BASE64.decode(wrongSecret));
        String token = Jwts.builder()
                .subject("trainer-service")
                .expiration(new Date(System.currentTimeMillis() + 60_000))
                .signWith(wrongKey)
                .compact();

        boolean actual = jwtService.isTokenValid(token);

        assertFalse(actual);
    }

    @Test
    void isTokenValid_whenMalformedToken_shouldReturnFalse() {
        String token = "this.is.not.a.valid.jwt";

        boolean actual = jwtService.isTokenValid(token);

        assertFalse(actual);
    }

    @Test
    void isTokenValid_whenEmptyToken_shouldReturnFalse() {
        String token = "";

        boolean actual = jwtService.isTokenValid(token);

        assertFalse(actual);
    }

    private String buildToken(String username, Date expiration) {
        SecretKey key = Keys.hmacShaKeyFor(Decoders.BASE64.decode(SECRET));

        return Jwts.builder()
                .subject(username)
                .expiration(expiration)
                .signWith(key)
                .compact();
    }

}