package com.gym.crm.core.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ApiErrorCode {

    VALIDATION_ERROR(2760, HttpStatus.BAD_REQUEST, "Validation error"),
    AUTHENTICATION_ERROR(2805, HttpStatus.UNAUTHORIZED, "Authentication fails"),
    AUTHORIZATION_ERROR(2806, HttpStatus.FORBIDDEN, "User is not authorized for request operation"),
    NOT_FOUND_ERROR(2835, HttpStatus.NOT_FOUND, "Requested data was not found"),
    SERVICE_ERROR(3200, HttpStatus.INTERNAL_SERVER_ERROR, "Internal processing error"),
    DATABASE_ERROR(3358, HttpStatus.INTERNAL_SERVER_ERROR, "Unexpected database access failure");

    private final int code;
    private final HttpStatus status;
    private final String message;
}