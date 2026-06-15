package com.gym.crm.core.service.profile;

import com.gym.crm.core.service.profile.PasswordEncoder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PasswordEncoderTest {

    private static final String RAW_PASSWORD = "rawPassword";

    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void setUp() {
        passwordEncoder = new PasswordEncoder(new BCryptPasswordEncoder());
    }

    @Test
    void encode_returnsNonNullHash() {
        String actual = passwordEncoder.encode(RAW_PASSWORD);

        assertNotNull(actual);
    }

    @Test
    void encode_returnsDifferentValueThanRawPassword() {
        String actual = passwordEncoder.encode(RAW_PASSWORD);

        assertNotEquals(RAW_PASSWORD, actual);
    }

    @Test
    void encode_returnsBCryptFormattedHash() {
        String actual = passwordEncoder.encode(RAW_PASSWORD);

        assertTrue(actual.startsWith("$2a$"), "Expected BCrypt hash starting with $2a$, but got: " + actual);
    }

    @Test
    void encode_samePasswordProducesDifferentHashes() {
        String firstHash = passwordEncoder.encode(RAW_PASSWORD);
        String secondHash = passwordEncoder.encode(RAW_PASSWORD);

        assertNotEquals(firstHash, secondHash);
    }

    @Test
    void matches_correctPassword_returnsTrue() {
        String encoded = passwordEncoder.encode(RAW_PASSWORD);

        assertTrue(passwordEncoder.matches(RAW_PASSWORD, encoded));
    }

    @Test
    void matches_wrongPassword_returnsFalse() {
        String encoded = passwordEncoder.encode(RAW_PASSWORD);

        assertFalse(passwordEncoder.matches("wrongPassword", encoded));
    }

    @Test
    void matches_emptyPassword_returnsFalse() {
        String encoded = passwordEncoder.encode(RAW_PASSWORD);

        assertFalse(passwordEncoder.matches("", encoded));
    }

    @Test
    void matches_differentHashesOfSamePassword_bothReturnTrue() {
        String firstHash = passwordEncoder.encode(RAW_PASSWORD);
        String secondHash = passwordEncoder.encode(RAW_PASSWORD);

        assertTrue(passwordEncoder.matches(RAW_PASSWORD, firstHash));
        assertTrue(passwordEncoder.matches(RAW_PASSWORD, secondHash));
    }
}