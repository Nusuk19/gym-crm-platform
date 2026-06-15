package com.gym.crm.core.service;

public interface UserProfileService {
    String generateUsername(String firstName, String lastName);

    String generatePassword();

    String hashPassword(String rawPassword);
}
