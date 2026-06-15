package com.gym.crm.core.service.profile;

import com.gym.crm.core.repository.TraineeRepository;
import com.gym.crm.core.repository.TrainerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class UsernameGenerator {

    private final TraineeRepository traineeRepository;
    private final TrainerRepository trainerRepository;

    public String generate(String firstName, String lastName) {
        String baseUsername = firstName + "." + lastName;

        if (!usernameExists(baseUsername)) {
            return baseUsername;
        }

        int serialNumber = 1;
        while (usernameExists(baseUsername + serialNumber)) {
            serialNumber++;
        }

        return baseUsername + serialNumber;
    }

    private boolean usernameExists(String username) {
        return traineeRepository.existsByUserUsername(username)
                || trainerRepository.existsByUserUsername(username);
    }
}
