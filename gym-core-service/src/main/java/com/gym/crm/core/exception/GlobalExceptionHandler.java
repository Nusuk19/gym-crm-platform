package com.gym.crm.core.exception;

import jakarta.persistence.PersistenceException;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.HibernateException;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

import static com.gym.crm.core.exception.ApiErrorCode.AUTHENTICATION_ERROR;
import static com.gym.crm.core.exception.ApiErrorCode.AUTHORIZATION_ERROR;
import static com.gym.crm.core.exception.ApiErrorCode.DATABASE_ERROR;
import static com.gym.crm.core.exception.ApiErrorCode.NOT_FOUND_ERROR;
import static com.gym.crm.core.exception.ApiErrorCode.SERVICE_ERROR;
import static com.gym.crm.core.exception.ApiErrorCode.VALIDATION_ERROR;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleMethodArgumentNotValid(MethodArgumentNotValidException ex) {
        String details = ex.getBindingResult().getFieldErrors().stream()
                .map(fieldError -> fieldError.getField() + " " + fieldError.getDefaultMessage())
                .collect(Collectors.joining(", "));
        log.warn("Request body validation failed: {}", details);

        return buildResponse(VALIDATION_ERROR, details);
    }

    @ExceptionHandler(HandlerMethodValidationException.class)
    public ResponseEntity<Map<String, Object>> handleHandlerMethodValidation(HandlerMethodValidationException ex) {
        String details = ex.getAllValidationResults().stream()
                .flatMap(results -> results.getResolvableErrors().stream())
                .map(error -> error.getDefaultMessage())
                .collect(Collectors.joining(", "));
        log.warn("Path/param validation failed: {}", details);

        return buildResponse(VALIDATION_ERROR, details);
    }

    @ExceptionHandler(EntityValidationException.class)
    public ResponseEntity<Map<String, Object>> handleEntityValidation(EntityValidationException ex) {
        log.warn("Entity validation error: {}", ex.getMessage());

        return buildResponse(VALIDATION_ERROR, ex.getMessage());
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Map<String, Object>> handleNotReadable(HttpMessageNotReadableException ex) {
        log.warn("Unreadable request body: {}", ex.getMessage());

        return buildResponse(VALIDATION_ERROR, "Malformed or missing request body");
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<Map<String, Object>> handleMissingParam(MissingServletRequestParameterException ex) {
        log.warn("Missing request parameter: {}", ex.getParameterName());

        return buildResponse(VALIDATION_ERROR, "Required parameter is missing: " + ex.getParameterName());
    }

    @ExceptionHandler(AuthenticationFailedException.class)
    public ResponseEntity<Map<String, Object>> handleAuthentication(AuthenticationFailedException ex) {
        log.warn("Authentication failed: {}", ex.getMessage());

        return buildResponse(AUTHENTICATION_ERROR);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<Map<String, Object>> handleAccessDenied(AccessDeniedException ex) {
        log.warn("Access denied: {}", ex.getMessage());

        return buildResponse(AUTHORIZATION_ERROR);
    }

    @ExceptionHandler(AuthorizationException.class)
    public ResponseEntity<Map<String, Object>> handleAuthorization(AuthorizationException ex) {
        log.warn("Authorization error: {}", ex.getMessage());

        return buildResponse(AUTHORIZATION_ERROR);
    }

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleNotFound(EntityNotFoundException ex) {
        log.warn("Entity not found: {}", ex.getMessage());

        return buildResponse(NOT_FOUND_ERROR, ex.getMessage());
    }

    @ExceptionHandler(HibernateException.class)
    public ResponseEntity<Map<String, Object>> handleHibernate(HibernateException ex) {
        log.error("Hibernate error", ex);

        return buildResponse(DATABASE_ERROR);
    }

    @ExceptionHandler(PersistenceException.class)
    public ResponseEntity<Map<String, Object>> handlePersistence(PersistenceException ex) {
        log.error("JPA persistence error", ex);

        return buildResponse(DATABASE_ERROR);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGeneric(Exception ex) {
        log.error("Unexpected error", ex);

        return buildResponse(SERVICE_ERROR);
    }

    @ExceptionHandler(UserBlockedException.class)
    public ResponseEntity<Map<String, Object>> handleUserBlockedException(UserBlockedException exception) {
        log.warn("User blocked: {}", exception.getMessage(), exception);

        return buildResponse(AUTHORIZATION_ERROR, "User is temporarily blocked due to multiple failed login attempts. Please try again later.");
    }

    @ExceptionHandler(ServiceTimeoutException.class)
    public ResponseEntity<Map<String, Object>> handleServiceTimeout(ServiceTimeoutException ex) {
        log.warn("Timeout: {}", ex.getMessage());

        return buildResponse(ApiErrorCode.TIMEOUT_ERROR, ex.getMessage());
    }

    @ExceptionHandler(ServiceConnectionException.class)
    public ResponseEntity<Map<String, Object>> handleServiceConnection(ServiceConnectionException ex) {
        log.warn("Connection error: {}", ex.getMessage());

        return buildResponse(ApiErrorCode.CONNECTION_ERROR, ex.getMessage());
    }

    private ResponseEntity<Map<String, Object>> buildResponse(ApiErrorCode errorCode) {
        return buildBody(errorCode, errorCode.getMessage());
    }

    private ResponseEntity<Map<String, Object>> buildResponse(ApiErrorCode errorCode, String details) {
        String message = errorCode.getMessage() + ": " + details;

        return buildBody(errorCode, message);
    }

    private ResponseEntity<Map<String, Object>> buildBody(ApiErrorCode errorCode, String message) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("errorCode", errorCode.getCode());
        body.put("errorMessage", message);

        return ResponseEntity.status(errorCode.getStatus()).body(body);
    }
}