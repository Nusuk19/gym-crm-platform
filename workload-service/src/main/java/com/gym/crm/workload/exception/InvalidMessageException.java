package com.gym.crm.workload.exception;

public class InvalidMessageException extends RuntimeException {

    public InvalidMessageException(String reason) {
        super(reason);
    }
}
