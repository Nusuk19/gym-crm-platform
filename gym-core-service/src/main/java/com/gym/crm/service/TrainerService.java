package com.gym.crm.service;

import com.gym.crm.dto.request.ActivationRequest;
import com.gym.crm.dto.request.ChangePasswordRequest;
import com.gym.crm.model.Trainer;

import java.util.List;
import java.util.Optional;

public interface TrainerService {
    Trainer create(Trainer trainer, String specializationName);

    Trainer update(Trainer trainer);

    Optional<Trainer> findByUsername(String username);

    List<Trainer> findAll();

    List<Trainer> findAllNotAssignedToTrainee(String traineeUsername);

    void changePassword(ChangePasswordRequest request);

    void activate(ActivationRequest request);

    void deactivate(ActivationRequest request);
}