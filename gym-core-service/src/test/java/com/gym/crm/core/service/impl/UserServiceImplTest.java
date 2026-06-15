package com.gym.crm.core.service.impl;

import com.gym.crm.core.dto.request.ActivationRequest;
import com.gym.crm.core.dto.request.ChangePasswordRequest;
import com.gym.crm.core.exception.EntityNotFoundException;
import com.gym.crm.core.exception.EntityValidationException;
import com.gym.crm.core.model.User;
import com.gym.crm.core.repository.UserRepository;
import com.gym.crm.core.service.common.CoreValidator;
import com.gym.crm.core.service.impl.UserServiceImpl;
import com.gym.crm.core.service.profile.PasswordEncoder;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private CoreValidator coreValidator;
    @InjectMocks
    private UserServiceImpl service;

    @Test
    void findByUsername_existingUser_returnsUser() {
        when(userRepository.findByUsername("Abdul.Hariton")).thenReturn(Optional.of(buildActiveUser()));

        Optional<User> result = service.findByUsername("Abdul.Hariton");

        assertThat(result).isPresent();
        assertThat(result.get().getUsername()).isEqualTo("Abdul.Hariton");
        assertThat(result.get().getFirstName()).isEqualTo("Abdul");
        assertThat(result.get().getLastName()).isEqualTo("Hariton");
        assertThat(result.get().getIsActive()).isTrue();
        verify(userRepository).findByUsername("Abdul.Hariton");
    }

    @Test
    void findByUsername_nonExistingUser_returnsEmpty() {
        when(userRepository.findByUsername("ghost.user")).thenReturn(Optional.empty());

        Optional<User> result = service.findByUsername("ghost.user");

        assertThat(result).isEmpty();
        verify(userRepository).findByUsername("ghost.user");
    }

    @Test
    void changePassword_validRequest_updatesPassword() {
        ChangePasswordRequest request = buildChangePasswordRequest("Abdul.Hariton", "rawOldPassword", "newPassword123");

        when(userRepository.findByUsername("Abdul.Hariton")).thenReturn(Optional.of(buildActiveUser()));
        when(passwordEncoder.matches("rawOldPassword", "encodedPassword")).thenReturn(true);
        when(passwordEncoder.encode("newPassword123")).thenReturn("newEncodedPassword");

        service.changePassword(request);

        verify(userRepository).findByUsername("Abdul.Hariton");
        verify(passwordEncoder).matches("rawOldPassword", "encodedPassword");
        verify(passwordEncoder).encode("newPassword123");
        verify(userRepository).save(any(User.class));
    }

    @Test
    void changePassword_wrongOldPassword_throwsEntityValidationException() {
        ChangePasswordRequest request = buildChangePasswordRequest("Abdul.Hariton", "wrongOldPassword", "newPassword123");

        when(userRepository.findByUsername("Abdul.Hariton")).thenReturn(Optional.of(buildActiveUser()));
        when(passwordEncoder.matches("wrongOldPassword", "encodedPassword")).thenReturn(false);

        assertThatThrownBy(() -> service.changePassword(request))
                .isInstanceOf(EntityValidationException.class)
                .hasMessageContaining("Old password does not match")
                .hasMessageContaining("Abdul.Hariton");

        verify(passwordEncoder, never()).encode(anyString());
        verify(userRepository, never()).save(any());
    }

    @Test
    void changePassword_userNotFound_throwsEntityNotFoundException() {
        ChangePasswordRequest request = buildChangePasswordRequest("ghost.user", "oldPassword", "newPassword123");

        when(userRepository.findByUsername("ghost.user")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.changePassword(request))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("User not found")
                .hasMessageContaining("ghost.user");

        verify(passwordEncoder, never()).matches(anyString(), anyString());
        verify(userRepository, never()).save(any());
    }

    @Test
    void activate_inactiveUser_activatesSuccessfully() {
        ActivationRequest request = buildActivationRequest("Mike.Tyson");

        when(userRepository.findByUsername("Mike.Tyson")).thenReturn(Optional.of(buildInactiveUser()));

        service.activate(request);

        verify(userRepository).findByUsername("Mike.Tyson");
        verify(userRepository).save(any(User.class));
    }

    @Test
    void activate_alreadyActiveUser_throwsEntityValidationException() {
        ActivationRequest request = buildActivationRequest("Abdul.Hariton");

        when(userRepository.findByUsername("Abdul.Hariton")).thenReturn(Optional.of(buildActiveUser()));

        assertThatThrownBy(() -> service.activate(request))
                .isInstanceOf(EntityValidationException.class)
                .hasMessageContaining("User is already active")
                .hasMessageContaining("Abdul.Hariton");

        verify(userRepository, never()).save(any());
    }

    @Test
    void activate_userNotFound_throwsEntityNotFoundException() {
        ActivationRequest request = buildActivationRequest("ghost.user");

        when(userRepository.findByUsername("ghost.user")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.activate(request))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("User not found")
                .hasMessageContaining("ghost.user");

        verify(userRepository, never()).save(any());
    }

    @Test
    void deactivate_activeUser_deactivatesSuccessfully() {
        ActivationRequest request = buildActivationRequest("Abdul.Hariton");

        when(userRepository.findByUsername("Abdul.Hariton")).thenReturn(Optional.of(buildActiveUser()));

        service.deactivate(request);

        verify(userRepository).findByUsername("Abdul.Hariton");
        verify(userRepository).save(any(User.class));
    }

    @Test
    void deactivate_alreadyInactiveUser_throwsEntityValidationException() {
        ActivationRequest request = buildActivationRequest("Mike.Tyson");

        when(userRepository.findByUsername("Mike.Tyson")).thenReturn(Optional.of(buildInactiveUser()));

        assertThatThrownBy(() -> service.deactivate(request))
                .isInstanceOf(EntityValidationException.class)
                .hasMessageContaining("User is already inactive")
                .hasMessageContaining("Mike.Tyson");

        verify(userRepository, never()).save(any());
    }

    @Test
    void deactivate_userNotFound_throwsEntityNotFoundException() {
        ActivationRequest request = buildActivationRequest("ghost.user");

        when(userRepository.findByUsername("ghost.user")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.deactivate(request))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("User not found")
                .hasMessageContaining("ghost.user");

        verify(userRepository, never()).save(any());
    }

    private User buildActiveUser() {
        return User.builder()
                .id(1L)
                .firstName("Abdul")
                .lastName("Hariton")
                .username("Abdul.Hariton")
                .password("encodedPassword")
                .isActive(true)
                .build();
    }

    private User buildInactiveUser() {
        return User.builder()
                .id(2L)
                .firstName("Mike")
                .lastName("Tyson")
                .username("Mike.Tyson")
                .password("encodedPassword2")
                .isActive(false)
                .build();
    }

    private ChangePasswordRequest buildChangePasswordRequest(String username, String oldPassword, String newPassword) {
        return ChangePasswordRequest.builder()
                .username(username)
                .oldPassword(oldPassword)
                .newPassword(newPassword)
                .build();
    }

    private ActivationRequest buildActivationRequest(String username) {
        return ActivationRequest.builder()
                .username(username)
                .build();
    }
}