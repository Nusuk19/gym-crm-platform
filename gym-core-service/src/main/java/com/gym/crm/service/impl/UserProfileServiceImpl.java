package com.gym.crm.service.impl;

import com.gym.crm.service.UserProfileService;
import com.gym.crm.service.profile.PasswordEncoder;
import com.gym.crm.service.profile.PasswordGenerator;
import com.gym.crm.service.profile.UsernameGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserProfileServiceImpl implements UserProfileService {
    private UsernameGenerator usernameGenerator;
    private PasswordGenerator passwordGenerator;
    private PasswordEncoder passwordEncoder;

    @Autowired
    public void setUsernameGenerator(UsernameGenerator usernameGenerator) {
        this.usernameGenerator = usernameGenerator;
    }

    @Autowired
    public void setPasswordGenerator(PasswordGenerator passwordGenerator) {
        this.passwordGenerator = passwordGenerator;
    }

    @Autowired
    public void setPasswordEncoder(PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public String generateUsername(String firstName, String lastName) {
        return usernameGenerator.generate(firstName, lastName);
    }

    @Override
    public String generatePassword() {
        return passwordGenerator.generate();
    }

    @Override
    public String hashPassword(String rawPassword) {
        return passwordEncoder.encode(rawPassword);
    }
}
