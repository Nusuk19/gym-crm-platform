package com.gym.crm.service.impl;

import com.gym.crm.dto.request.ActivationRequest;
import com.gym.crm.dto.request.ChangePasswordRequest;
import com.gym.crm.exception.EntityNotFoundException;
import com.gym.crm.exception.EntityValidationException;
import com.gym.crm.model.User;
import com.gym.crm.repository.UserRepository;
import com.gym.crm.service.UserService;
import com.gym.crm.service.common.CoreValidator;
import com.gym.crm.service.profile.PasswordEncoder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Slf4j
@RequiredArgsConstructor
@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final CoreValidator coreValidator;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional(readOnly = true)
    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    @Override
    @Transactional(readOnly = true)
    public void changePassword(ChangePasswordRequest request) {
        coreValidator.validate(request);
        log.info("Changing password for user: username={}", request.getUsername());

        User user = requireUserByUsername(request.getUsername());

        if (!passwordEncoder.matches(request.getOldPassword(), user.getPassword())) {
            log.warn("Password change failed - wrong old password: username={}", request.getUsername());
            throw new EntityValidationException("Old password does not match for user: " + request.getUsername());
        }

        userRepository.save(user.toBuilder()
                .password(passwordEncoder.encode(request.getNewPassword()))
                .build());

        log.info("Password changed successfully for user: username={}", request.getUsername());
    }

    @Override
    @Transactional
    public void activate(ActivationRequest request) {
        log.info("Activating user: username={}", request.getUsername());

        User user = requireUserByUsername(request.getUsername());

        if (Boolean.TRUE.equals(user.getIsActive())) {
            throw new EntityValidationException("User is already active: " + request.getUsername());
        }

        userRepository.save(user.toBuilder().isActive(true).build());
        log.info("User activated: username={}", request.getUsername());
    }

    @Override
    @Transactional
    public void deactivate(ActivationRequest request) {
        log.info("Deactivating user: username={}", request.getUsername());

        User user = requireUserByUsername(request.getUsername());

        if (Boolean.FALSE.equals(user.getIsActive())) {
            throw new EntityValidationException("User is already inactive: " + request.getUsername());
        }

        userRepository.save(user.toBuilder().isActive(false).build());
        log.info("User deactivated: username={}", request.getUsername());
    }

    private User requireUserByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new EntityNotFoundException("User not found: " + username));
    }
}