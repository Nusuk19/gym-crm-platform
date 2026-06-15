package com.gym.crm.service.common;

import com.gym.crm.dto.request.UserCredentials;
import com.gym.crm.exception.AuthenticationFailedException;
import com.gym.crm.model.Trainee;
import com.gym.crm.model.Trainer;
import com.gym.crm.model.TrainingType;
import com.gym.crm.model.User;
import com.gym.crm.repository.TraineeRepository;
import com.gym.crm.repository.TrainerRepository;
import com.gym.crm.repository.UserRepository;
import com.gym.crm.security.BruteForceProtectionService;
import com.gym.crm.security.TokenBlacklistService;
import com.gym.crm.service.profile.PasswordEncoder;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthenticationServiceTest {
    private static final String TOKEN = "jwt-token";
    private static final String AUTHORIZATION_HEADER = "Bearer " + TOKEN;

    @Mock
    private TraineeRepository traineeRepository;

    @Mock
    private TrainerRepository trainerRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private BruteForceProtectionService bruteForceProtectionService;

    @Mock
    private TokenBlacklistService tokenBlacklistService;

    @InjectMocks
    private AuthenticationService service;

    @Test
    void validateTraineeCredentials_validCredentials_doesNotThrow() {
        UserCredentials credentials = buildCredentials("Abdul.Hariton", "password123");

        when(traineeRepository.findByUserUsername("Abdul.Hariton")).thenReturn(Optional.of(buildTrainee()));
        when(passwordEncoder.matches("password123", "encodedPassword")).thenReturn(true);

        assertThatCode(() -> service.validateTraineeCredentials(credentials)).doesNotThrowAnyException();

        verify(traineeRepository).findByUserUsername("Abdul.Hariton");
        verify(bruteForceProtectionService).checkBlocked("Abdul.Hariton");
        verify(passwordEncoder).matches("password123", "encodedPassword");
        verify(bruteForceProtectionService).loginSucceeded("Abdul.Hariton");
        verify(bruteForceProtectionService, never()).loginFailed("Abdul.Hariton");
    }

    @Test
    void validateTraineeCredentials_wrongPassword_throwsAuthenticationExceptionAndRegistersFailure() {
        UserCredentials credentials = buildCredentials("Abdul.Hariton", "wrongPassword");

        when(traineeRepository.findByUserUsername("Abdul.Hariton")).thenReturn(Optional.of(buildTrainee()));
        when(passwordEncoder.matches("wrongPassword", "encodedPassword")).thenReturn(false);

        assertThatThrownBy(() -> service.validateTraineeCredentials(credentials))
                .isInstanceOf(AuthenticationFailedException.class)
                .hasMessageContaining("Invalid credentials");

        verify(bruteForceProtectionService).checkBlocked("Abdul.Hariton");
        verify(passwordEncoder).matches("wrongPassword", "encodedPassword");
        verify(bruteForceProtectionService).loginFailed("Abdul.Hariton");
        verify(bruteForceProtectionService, never()).loginSucceeded("Abdul.Hariton");
    }

    @Test
    void validateTraineeCredentials_blockedUser_throwsAuthenticationExceptionAndDoesNotCheckPassword() {
        UserCredentials credentials = buildCredentials("Abdul.Hariton", "password123");

        when(traineeRepository.findByUserUsername("Abdul.Hariton")).thenReturn(Optional.of(buildTrainee()));
        doThrow(new AuthenticationFailedException("Too many failed login attempts"))
                .when(bruteForceProtectionService).checkBlocked("Abdul.Hariton");

        assertThatThrownBy(() -> service.validateTraineeCredentials(credentials))
                .isInstanceOf(AuthenticationFailedException.class)
                .hasMessageContaining("Too many failed login attempts");

        verify(bruteForceProtectionService).checkBlocked("Abdul.Hariton");
        verify(passwordEncoder, never()).matches("password123", "encodedPassword");
        verify(bruteForceProtectionService, never()).loginFailed("Abdul.Hariton");
        verify(bruteForceProtectionService, never()).loginSucceeded("Abdul.Hariton");
    }

    @Test
    void validateTraineeCredentials_traineeNotFound_throwsAuthenticationException() {
        UserCredentials credentials = buildCredentials("ghost.user", "password123");

        when(traineeRepository.findByUserUsername("ghost.user")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.validateTraineeCredentials(credentials))
                .isInstanceOf(AuthenticationFailedException.class)
                .hasMessageContaining("Invalid credentials");

        verify(bruteForceProtectionService, never()).checkBlocked("ghost.user");
    }

    @Test
    void validateTrainerCredentials_validCredentials_doesNotThrow() {
        UserCredentials credentials = buildCredentials("Mike.Tyson", "password123");

        when(trainerRepository.findByUserUsername("Mike.Tyson")).thenReturn(Optional.of(buildTrainer()));
        when(passwordEncoder.matches("password123", "encodedPassword")).thenReturn(true);

        assertThatCode(() -> service.validateTrainerCredentials(credentials)).doesNotThrowAnyException();

        verify(trainerRepository).findByUserUsername("Mike.Tyson");
        verify(bruteForceProtectionService).checkBlocked("Mike.Tyson");
        verify(passwordEncoder).matches("password123", "encodedPassword");
        verify(bruteForceProtectionService).loginSucceeded("Mike.Tyson");
        verify(bruteForceProtectionService, never()).loginFailed("Mike.Tyson");
    }

    @Test
    void validateTrainerCredentials_wrongPassword_throwsAuthenticationExceptionAndRegistersFailure() {
        UserCredentials credentials = buildCredentials("Mike.Tyson", "wrongPassword");

        when(trainerRepository.findByUserUsername("Mike.Tyson")).thenReturn(Optional.of(buildTrainer()));
        when(passwordEncoder.matches("wrongPassword", "encodedPassword")).thenReturn(false);

        assertThatThrownBy(() -> service.validateTrainerCredentials(credentials))
                .isInstanceOf(AuthenticationFailedException.class)
                .hasMessageContaining("Invalid credentials");

        verify(bruteForceProtectionService).checkBlocked("Mike.Tyson");
        verify(passwordEncoder).matches("wrongPassword", "encodedPassword");
        verify(bruteForceProtectionService).loginFailed("Mike.Tyson");
        verify(bruteForceProtectionService, never()).loginSucceeded("Mike.Tyson");
    }

    @Test
    void validateTrainerCredentials_blockedUser_throwsAuthenticationExceptionAndDoesNotCheckPassword() {
        UserCredentials credentials = buildCredentials("Mike.Tyson", "password123");

        when(trainerRepository.findByUserUsername("Mike.Tyson")).thenReturn(Optional.of(buildTrainer()));
        doThrow(new AuthenticationFailedException("Too many failed login attempts"))
                .when(bruteForceProtectionService).checkBlocked("Mike.Tyson");

        assertThatThrownBy(() -> service.validateTrainerCredentials(credentials))
                .isInstanceOf(AuthenticationFailedException.class)
                .hasMessageContaining("Too many failed login attempts");

        verify(bruteForceProtectionService).checkBlocked("Mike.Tyson");
        verify(passwordEncoder, never()).matches("password123", "encodedPassword");
        verify(bruteForceProtectionService, never()).loginFailed("Mike.Tyson");
        verify(bruteForceProtectionService, never()).loginSucceeded("Mike.Tyson");
    }

    @Test
    void validateTrainerCredentials_trainerNotFound_throwsAuthenticationException() {
        UserCredentials credentials = buildCredentials("ghost.user", "password123");

        when(trainerRepository.findByUserUsername("ghost.user")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.validateTrainerCredentials(credentials))
                .isInstanceOf(AuthenticationFailedException.class)
                .hasMessageContaining("Invalid credentials");

        verify(bruteForceProtectionService, never()).checkBlocked("ghost.user");
    }

    @Test
    void validateCredentials_validCredentials_doesNotThrow() {
        UserCredentials credentials = buildCredentials("Abdul.Hariton", "password123");

        when(userRepository.findByUsername("Abdul.Hariton")).thenReturn(Optional.of(buildUser("Abdul.Hariton")));
        when(passwordEncoder.matches("password123", "encodedPassword")).thenReturn(true);

        assertThatCode(() -> service.validateCredentials(credentials)).doesNotThrowAnyException();

        verify(userRepository).findByUsername("Abdul.Hariton");
        verify(passwordEncoder).matches("password123", "encodedPassword");
    }

    @Test
    void validateCredentials_wrongPassword_throwsAuthenticationException() {
        UserCredentials credentials = buildCredentials("Abdul.Hariton", "wrongPassword");

        when(userRepository.findByUsername("Abdul.Hariton")).thenReturn(Optional.of(buildUser("Abdul.Hariton")));
        when(passwordEncoder.matches("wrongPassword", "encodedPassword")).thenReturn(false);

        assertThatThrownBy(() -> service.validateCredentials(credentials))
                .isInstanceOf(AuthenticationFailedException.class)
                .hasMessageContaining("Invalid credentials");
    }

    @Test
    void validateCredentials_userNotFound_throwsAuthenticationException() {
        UserCredentials credentials = buildCredentials("ghost.user", "password123");

        when(userRepository.findByUsername("ghost.user")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.validateCredentials(credentials))
                .isInstanceOf(AuthenticationFailedException.class)
                .hasMessageContaining("Invalid credentials");
    }

    @Test
    @DisplayName("Should blacklist token when authorization header is valid")
    void logout_validAuthorizationHeader_shouldBlacklistToken() {
        assertDoesNotThrow(() -> service.logout(AUTHORIZATION_HEADER));

        verify(tokenBlacklistService).blacklist(TOKEN);
    }

    @Test
    @DisplayName("Should throw exception when authorization header is null")
    void logout_nullAuthorizationHeader_shouldThrowException() {
        AuthenticationFailedException exception = assertThrows(AuthenticationFailedException.class, () -> service.logout(null));

        assertEquals("Invalid authorization header", exception.getMessage());
        verify(tokenBlacklistService, never()).blacklist(TOKEN);
    }

    @Test
    @DisplayName("Should throw exception when authorization header does not start with Bearer prefix")
    void logout_invalidAuthorizationHeader_shouldThrowException() {
        AuthenticationFailedException exception = assertThrows(AuthenticationFailedException.class, () -> service.logout(TOKEN));

        assertEquals("Invalid authorization header", exception.getMessage());
        verify(tokenBlacklistService, never()).blacklist(TOKEN);
    }

    private UserCredentials buildCredentials(String username, String password) {
        return UserCredentials.builder()
                .username(username)
                .password(password)
                .build();
    }

    private User buildUser(String username) {
        return User.builder()
                .id(1L)
                .username(username)
                .password("encodedPassword")
                .isActive(true)
                .build();
    }

    private Trainee buildTrainee() {
        return Trainee.builder()
                .id(1L)
                .user(User.builder()
                        .id(1L)
                        .username("Abdul.Hariton")
                        .password("encodedPassword")
                        .isActive(true)
                        .build())
                .build();
    }

    private Trainer buildTrainer() {
        return Trainer.builder()
                .id(1L)
                .user(User.builder()
                        .id(2L)
                        .username("Mike.Tyson")
                        .password("encodedPassword")
                        .isActive(true)
                        .build())
                .specialization(TrainingType.builder()
                        .trainingTypeName("BOXING")
                        .build())
                .build();
    }
}