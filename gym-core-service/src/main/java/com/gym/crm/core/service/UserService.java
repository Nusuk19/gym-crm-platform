package com.gym.crm.core.service;

import com.gym.crm.core.dto.request.ActivationRequest;
import com.gym.crm.core.dto.request.ChangePasswordRequest;
import com.gym.crm.core.model.User;

import java.util.Optional;

public interface UserService {

    Optional<User> findByUsername(String username);

    void changePassword(ChangePasswordRequest request);

    void activate(ActivationRequest request);

    void deactivate(ActivationRequest request);
}
