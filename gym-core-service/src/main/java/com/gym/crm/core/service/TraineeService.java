package com.gym.crm.core.service;

import com.gym.crm.core.dto.request.ActivationRequest;
import com.gym.crm.core.dto.request.ChangePasswordRequest;
import com.gym.crm.core.model.Trainee;
import com.gym.crm.core.model.Trainer;

import java.util.List;
import java.util.Optional;

public interface TraineeService {
    Trainee create(Trainee trainee);

    Trainee update(Trainee trainee);

    List<Trainer> updateTrainers(String traineeUsername, List<String> trainerUsernames);

    void deleteByUsername(String username);

    Optional<Trainee> findByUsername(String username);

    List<Trainee> findAll();

    void changePassword(ChangePasswordRequest request);

    void activate(ActivationRequest request);

    void deactivate(ActivationRequest request);
}