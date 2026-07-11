package com.gym.crm.workload.exception;

import com.gym.crm.workload.openapi.ErrorResponse;
import com.mongodb.DuplicateKeyException;
import com.mongodb.MongoTimeoutException;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.context.request.WebRequest;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GlobalExceptionHandlerTest {

    @InjectMocks
    private GlobalExceptionHandler handler;

    @Test
    void handleNotFoundException_shouldReturn404() {
        NoSuchElementException ex = new NoSuchElementException("Trainer not found");

        ResponseEntity<ErrorResponse> response = handler.handleNotFoundException(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getErrorCode()).isEqualTo(404);
        assertThat(response.getBody().getErrorMessage()).isEqualTo("Trainer not found");
    }

    @Test
    void handleUnexpectedException_shouldReturn500() {
        Exception ex = new RuntimeException("Something went wrong");

        ResponseEntity<ErrorResponse> response = handler.handleUnexpectedException(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getErrorCode()).isEqualTo(500);
        assertThat(response.getBody().getErrorMessage()).isEqualTo("Internal server error");
    }

    @Test
    void handleMethodArgumentNotValid_shouldReturn400() {
        MethodArgumentNotValidException ex = mock(MethodArgumentNotValidException.class);
        BindingResult bindingResult = mock(BindingResult.class);
        FieldError fieldError = new FieldError("request", "trainerUsername", "must not be blank");

        when(ex.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.getFieldErrors()).thenReturn(List.of(fieldError));

        ResponseEntity<Object> response = handler.handleMethodArgumentNotValid(ex, new HttpHeaders(), HttpStatus.BAD_REQUEST, mock(WebRequest.class));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        ErrorResponse body = (ErrorResponse) response.getBody();
        assertThat(body).isNotNull();
        assertThat(body.getErrorCode()).isEqualTo(400);
        assertThat(body.getErrorMessage()).isEqualTo("Validation error: trainerUsername: must not be blank");
    }

    @Test
    void handleConstraintViolation_shouldReturn400() {
        ConstraintViolation<?> violation = mock(ConstraintViolation.class);
        when(violation.getPropertyPath()).thenReturn(mock(jakarta.validation.Path.class));
        when(violation.getMessage()).thenReturn("must not be blank");
        ConstraintViolationException ex = new ConstraintViolationException(Set.of(violation));

        ResponseEntity<ErrorResponse> response = handler.handleConstraintViolation(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getErrorCode()).isEqualTo(400);
        assertThat(response.getBody().getErrorMessage()).isNotBlank();
    }

    @Test
    void handleDuplicateKey_shouldReturn409() {
        DuplicateKeyException ex = mock(DuplicateKeyException.class);

        ResponseEntity<ErrorResponse> response = handler.handleDuplicateKey(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getErrorCode()).isEqualTo(409);
        assertThat(response.getBody().getErrorMessage()).isEqualTo("Resource already exists");
    }

    @Test
    void handleMongoTimeout_shouldReturn503() {
        MongoTimeoutException ex = new MongoTimeoutException("Timeout");

        ResponseEntity<ErrorResponse> response = handler.handleMongoTimeout(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.SERVICE_UNAVAILABLE);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getErrorCode()).isEqualTo(503);
        assertThat(response.getBody().getErrorMessage()).isEqualTo("Database operation timed out, please retry");
    }

    @Test
    void handleOptimisticLocking_shouldReturn409() {
        OptimisticLockingFailureException ex = new OptimisticLockingFailureException("Concurrent modification");

        ResponseEntity<ErrorResponse> response = handler.handleOptimisticLocking(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getErrorCode()).isEqualTo(409);
        assertThat(response.getBody().getErrorMessage()).isEqualTo("Resource was modified concurrently, please retry");
    }

    @Test
    void handleDataAccess_shouldReturn503() {
        DataAccessException ex = mock(DataAccessException.class);

        ResponseEntity<ErrorResponse> response = handler.handleDataAccess(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.SERVICE_UNAVAILABLE);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getErrorCode()).isEqualTo(503);
        assertThat(response.getBody().getErrorMessage()).isEqualTo("Database operation failed");
    }
}