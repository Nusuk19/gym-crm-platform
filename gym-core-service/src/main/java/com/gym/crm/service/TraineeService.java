package com.gym.crm.service;

import com.gym.crm.dto.request.ActivationRequest;
import com.gym.crm.dto.request.ChangePasswordRequest;
import com.gym.crm.model.Trainee;
import com.gym.crm.model.Trainer;

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