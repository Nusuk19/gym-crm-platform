package com.gym.crm.exception;

public class EntityValidationException extends RuntimeException {
    public EntityValidationException(String message) {
        super(message);
    }
}