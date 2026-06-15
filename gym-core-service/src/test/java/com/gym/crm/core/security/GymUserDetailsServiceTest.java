package com.gym.crm.core.security;

import com.gym.crm.core.model.User;
import com.gym.crm.core.repository.UserRepository;
import com.gym.crm.core.security.GymUserDetailsService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GymUserDetailsServiceTest {

    private static final String USERNAME = "Abdul.Hariton";
    private static final String PASSWORD = "hashedPassword";

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private GymUserDetailsService userDetailsService;

    @Test
    void loadUserByUsername_whenUserExists_returnsUserDetails() {
        User user = buildActiveUser();
        when(userRepository.findByUsername(USERNAME)).thenReturn(Optional.of(user));

        UserDetails actual = userDetailsService.loadUserByUsername(USERNAME);

        assertThat(actual.getUsername()).isEqualTo(USERNAME);
        assertThat(actual.getPassword()).isEqualTo(PASSWORD);
        verify(userRepository).findByUsername(USERNAME);
    }

    @Test
    void loadUserByUsername_whenUserNotFound_throwsUsernameNotFoundException() {
        when(userRepository.findByUsername(USERNAME)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userDetailsService.loadUserByUsername(USERNAME))
                .isInstanceOf(UsernameNotFoundException.class)
                .hasMessageContaining(USERNAME);

        verify(userRepository).findByUsername(USERNAME);
    }

    @Test
    void loadUserByUsername_whenUserIsInactive_returnsDisabledUserDetails() {
        User user = buildInactiveUser();
        when(userRepository.findByUsername(USERNAME)).thenReturn(Optional.of(user));

        UserDetails actual = userDetailsService.loadUserByUsername(USERNAME);

        assertThat(actual.isEnabled()).isFalse();
    }

    private User buildActiveUser() {
        return User.builder()
                .username(USERNAME)
                .password(PASSWORD)
                .isActive(true)
                .build();
    }

    private User buildInactiveUser() {
        return User.builder()
                .username(USERNAME)
                .password(PASSWORD)
                .isActive(false)
                .build();
    }
}