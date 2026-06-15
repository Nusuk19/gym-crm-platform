package com.gym.crm.security;

import com.gym.crm.exception.UserBlockedException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
@RequiredArgsConstructor
public class BruteForceProtectionService {

    private static final int MAX_ATTEMPTS = 3;
    private static final Duration BLOCK_DURATION = Duration.ofMinutes(5);
    private static final Duration ATTEMPTS_TTL = Duration.ofMinutes(5);
    private static final String ATTEMPTS_PREFIX = "login:attempts:";
    private static final String BLOCKED_PREFIX = "login:blocked:";
    private static final String USER_BLOCKED_MESSAGE = "User is temporarily blocked";

    private final StringRedisTemplate redisTemplate;

    public void checkBlocked(String username) {
        Boolean isKeyPresentInRedis = redisTemplate.hasKey(BLOCKED_PREFIX + username);

        if (isKeyPresentInRedis != null && isKeyPresentInRedis) {
            throw new UserBlockedException(USER_BLOCKED_MESSAGE);
        }
    }

    public void loginFailed(String username) {
        String attemptsKey = ATTEMPTS_PREFIX + username;
        Long attempts = redisTemplate.opsForValue().increment(attemptsKey);

        if (attempts != null && attempts == 1) {
            redisTemplate.expire(attemptsKey, ATTEMPTS_TTL);
        }

        if (attempts != null && attempts >= MAX_ATTEMPTS) {
            redisTemplate.opsForValue().set(BLOCKED_PREFIX + username, "true", BLOCK_DURATION);
            redisTemplate.delete(attemptsKey);
        }
    }

    public void loginSucceeded(String username) {
        redisTemplate.delete(ATTEMPTS_PREFIX + username);
        redisTemplate.delete(BLOCKED_PREFIX + username);
    }
}