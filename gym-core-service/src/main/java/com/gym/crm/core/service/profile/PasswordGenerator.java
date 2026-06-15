package com.gym.crm.core.service.profile;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.SecureRandom;

@Component
public class PasswordGenerator {
    private static final int REQUIRED_PASSWORD_LENGTH = 10;

    private final SecureRandom random = new SecureRandom();

    private String characters;
    private int passwordLength;

    @Autowired
    public void setCharacters(@Value("${password.characters}") String characters) {
        this.characters = characters;
    }

    @Autowired
    public void setPasswordLength(@Value("${password.length}") int passwordLength) {
        this.passwordLength = passwordLength;
    }

    @PostConstruct
    public void validate() {
        if (characters == null || characters.isEmpty()) {
            throw new IllegalStateException("Password characters must not be empty");
        }
        if (passwordLength != REQUIRED_PASSWORD_LENGTH) {
            throw new IllegalStateException(
                    String.format("Default password length value must be exactly %d, but is defined in properties as %d",
                            REQUIRED_PASSWORD_LENGTH, passwordLength));
        }
    }

    public String generate() {
        StringBuilder password = new StringBuilder(passwordLength);
        for (int i = 0; i < passwordLength; i++) {
            password.append(characters.charAt(random.nextInt(characters.length())));
        }

        return password.toString();
    }
}
