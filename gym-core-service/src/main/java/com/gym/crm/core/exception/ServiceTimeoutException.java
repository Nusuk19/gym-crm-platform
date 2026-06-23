package com.gym.crm.core.exception;

public class ServiceTimeoutException extends ServiceException {
    public ServiceTimeoutException(String message) {
        super(message);
    }
}