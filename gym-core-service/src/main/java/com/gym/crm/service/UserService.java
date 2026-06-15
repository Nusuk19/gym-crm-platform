package com.gym.crm.service;

import com.gym.crm.dto.request.ActivationRequest;
import com.gym.crm.dto.request.ChangePasswordRequest;
import com.gym.crm.model.User;

import java.util.Optional;

public interface UserService {

    Optional<User> findByUsername(String username);

    void changePassword(ChangePasswordRequest request);

    void activate(ActivationRequest request);

    void deactivate(ActivationRequest request);
}
