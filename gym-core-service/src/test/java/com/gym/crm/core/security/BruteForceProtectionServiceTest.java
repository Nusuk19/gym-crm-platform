package com.gym.crm.core.security;

import com.gym.crm.core.exception.UserBlockedException;
import com.gym.crm.core.security.BruteForceProtectionService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LoginAttemptServiceTest {

    private static final String USERNAME = "test.user";
    private static final String ATTEMPTS_KEY = "login:attempts:" + USERNAME;
    private static final String BLOCKED_KEY = "login:blocked:" + USERNAME;
    private static final Duration ATTEMPTS_TTL = Duration.ofMinutes(5);
    private static final Duration BLOCK_DURATION = Duration.ofMinutes(5);

    @Mock
    private StringRedisTemplate redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    @InjectMocks
    private BruteForceProtectionService service;

    @Test
    @DisplayName("Should not throw when user is not blocked")
    void checkBlocked_userIsNotBlocked_shouldNotThrow() {
        when(redisTemplate.hasKey(BLOCKED_KEY)).thenReturn(false);

        assertDoesNotThrow(() -> service.checkBlocked(USERNAME));

        verify(redisTemplate).hasKey(BLOCKED_KEY);
    }

    @Test
    @DisplayName("Should throw exception when user is blocked")
    void checkBlocked_userIsBlocked_shouldThrowException() {
        when(redisTemplate.hasKey(BLOCKED_KEY)).thenReturn(true);

        assertThatThrownBy(() -> service.checkBlocked(USERNAME))
                .isInstanceOf(UserBlockedException.class)
                .hasMessage("User is temporarily blocked");
        verify(redisTemplate).hasKey(BLOCKED_KEY);
    }

    @Test
    @DisplayName("Should increment attempts and set TTL on first failed login")
    void loginFailed_firstAttempt_shouldIncrementAttemptsAndSetTtl() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.increment(ATTEMPTS_KEY)).thenReturn(1L);

        service.loginFailed(USERNAME);

        verify(valueOperations).increment(ATTEMPTS_KEY);
        verify(redisTemplate).expire(ATTEMPTS_KEY, ATTEMPTS_TTL);
        verify(valueOperations, never()).set(BLOCKED_KEY, "true", BLOCK_DURATION);
        verify(redisTemplate, never()).delete(ATTEMPTS_KEY);
    }

    @Test
    @DisplayName("Should increment attempts without setting TTL on second failed login")
    void loginFailed_secondAttempt_shouldIncrementAttemptsWithoutSettingTtl() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.increment(ATTEMPTS_KEY)).thenReturn(2L);

        service.loginFailed(USERNAME);

        verify(valueOperations).increment(ATTEMPTS_KEY);
        verify(redisTemplate, never()).expire(ATTEMPTS_KEY, ATTEMPTS_TTL);
        verify(valueOperations, never()).set(BLOCKED_KEY, "true", BLOCK_DURATION);
        verify(redisTemplate, never()).delete(ATTEMPTS_KEY);
    }

    @Test
    @DisplayName("Should block user and delete attempts after third failed login")
    void loginFailed_thirdAttempt_shouldBlockUserAndDeleteAttempts() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.increment(ATTEMPTS_KEY)).thenReturn(3L);

        service.loginFailed(USERNAME);

        verify(valueOperations).increment(ATTEMPTS_KEY);
        verify(valueOperations).set(BLOCKED_KEY, "true", BLOCK_DURATION);
        verify(redisTemplate).delete(ATTEMPTS_KEY);
    }

    @Test
    @DisplayName("Should clear attempts and blocked keys after successful login")
    void loginSucceeded_shouldClearAttemptsAndBlockedKeys() {
        service.loginSucceeded(USERNAME);

        verify(redisTemplate).delete(ATTEMPTS_KEY);
        verify(redisTemplate).delete(BLOCKED_KEY);
    }
}