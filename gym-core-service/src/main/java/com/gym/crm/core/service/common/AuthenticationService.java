package com.gym.crm.core.service.common;

import com.gym.crm.core.dto.request.UserCredentials;
import com.gym.crm.core.exception.AuthenticationFailedException;
import com.gym.crm.core.model.Trainee;
import com.gym.crm.core.model.Trainer;
import com.gym.crm.core.model.User;
import com.gym.crm.core.repository.TraineeRepository;
import com.gym.crm.core.repository.TrainerRepository;
import com.gym.crm.core.repository.UserRepository;
import com.gym.crm.core.security.BruteForceProtectionService;
import com.gym.crm.core.security.TokenBlacklistService;
import com.gym.crm.core.service.profile.PasswordEncoder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@RequiredArgsConstructor
@Component
public class AuthenticationService {

    private static final String INVALID_CREDENTIALS = "Invalid credentials";
    private static final String BEARER_PREFIX = "Bearer ";
    private static final String INVALID_AUTHORIZATION_HEADER_MESSAGE = "Invalid authorization header";

    private final TraineeRepository traineeRepository;
    private final TrainerRepository trainerRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;
    private final BruteForceProtectionService bruteForceProtectionService;
    private final TokenBlacklistService tokenBlacklistService;

    public void validateTraineeCredentials(UserCredentials credentials) {
        log.info("Validating trainee credentials: username={}", credentials.getUsername());

        Trainee trainee = traineeRepository.findByUserUsername(credentials.getUsername())
                .orElseThrow(() -> new AuthenticationFailedException(INVALID_CREDENTIALS));
        String username = credentials.getUsername();

        bruteForceProtectionService.checkBlocked(username);
        if (!passwordEncoder.matches(credentials.getPassword(), trainee.getUser().getPassword())) {
            log.warn("Authentication failed for trainee: username={}", credentials.getUsername());
            bruteForceProtectionService.loginFailed(username);

            throw new AuthenticationFailedException(INVALID_CREDENTIALS);
        }

        bruteForceProtectionService.loginSucceeded(username);
        log.info("Trainee credentials validated successfully: username={}", credentials.getUsername());
    }

    public void validateTrainerCredentials(UserCredentials credentials) {
        log.info("Validating trainer credentials: username={}", credentials.getUsername());

        Trainer trainer = trainerRepository.findByUserUsername(credentials.getUsername())
                .orElseThrow(() -> new AuthenticationFailedException(INVALID_CREDENTIALS));

        String username = credentials.getUsername();
        bruteForceProtectionService.checkBlocked(username);
        if (!passwordEncoder.matches(credentials.getPassword(), trainer.getUser().getPassword())) {
            log.warn("Authentication failed for trainer: username={}", credentials.getUsername());
            bruteForceProtectionService.loginFailed(username);

            throw new AuthenticationFailedException(INVALID_CREDENTIALS);
        }

        bruteForceProtectionService.loginSucceeded(username);
        log.info("Trainer credentials validated successfully: username={}", credentials.getUsername());
    }

    public void validateCredentials(UserCredentials credentials) {
        log.info("Validating credentials: username={}", credentials.getUsername());

        User user = userRepository.findByUsername(credentials.getUsername())
                .orElseThrow(() -> new AuthenticationFailedException(INVALID_CREDENTIALS));


        if (!passwordEncoder.matches(credentials.getPassword(), user.getPassword())) {
            log.warn("Authentication failed: username={}", credentials.getUsername());

            throw new AuthenticationFailedException(INVALID_CREDENTIALS);
        }

        log.info("Credentials validated successfully: username={}", credentials.getUsername());
    }

    public void logout(String authorizationHeader) {
        String token = extractToken(authorizationHeader);

        tokenBlacklistService.blacklist(token);
    }

    private String extractToken(String authorizationHeader) {
        if (authorizationHeader == null || !authorizationHeader.startsWith(BEARER_PREFIX)) {
            throw new AuthenticationFailedException(INVALID_AUTHORIZATION_HEADER_MESSAGE);
        }

        return authorizationHeader.substring(BEARER_PREFIX.length());
    }
}