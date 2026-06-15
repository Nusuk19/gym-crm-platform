package com.gym.crm.exception;

import jakarta.persistence.PersistenceException;
import org.hibernate.HibernateException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.context.MessageSourceResolvable;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.validation.method.ParameterValidationResult;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.method.annotation.HandlerMethodValidationException;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    @DisplayName("MethodArgumentNotValidException → 400, code 2760, field details included")
    void handleMethodArgumentNotValid_returns400WithDetails() {
        MethodArgumentNotValidException ex = mock(MethodArgumentNotValidException.class);
        BindingResult bindingResult = mock(BindingResult.class);

        when(ex.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.getFieldErrors()).thenReturn(List.of(
                new FieldError("obj", "firstName", "must not be blank"),
                new FieldError("obj", "lastName", "must not be blank")));

        ResponseEntity<Map<String, Object>> response = handler.handleMethodArgumentNotValid(ex);

        assertThat(response.getStatusCode().value()).isEqualTo(400);
        assertThat(response.getBody()).containsEntry("errorCode", 2760);
        assertThat(response.getBody().get("errorMessage").toString()).contains("firstName").contains("lastName");
    }

    @Test
    @DisplayName("EntityValidationException → 400, code 2760, message included")
    void handleEntityValidation_returns400WithMessage() {
        EntityValidationException ex = new EntityValidationException("trainingDuration must be positive");

        ResponseEntity<Map<String, Object>> response = handler.handleEntityValidation(ex);

        assertThat(response.getStatusCode().value()).isEqualTo(400);
        assertThat(response.getBody()).containsEntry("errorCode", 2760);
        assertThat(response.getBody().get("errorMessage").toString()).contains("trainingDuration must be positive");
    }

    @Test
    @DisplayName("AccessDeniedException → 403, code 2806, no internal details leaked")
    void handleAccessDenied_returns403WithoutDetails() {
        AccessDeniedException ex = new AccessDeniedException("Access denied");

        ResponseEntity<Map<String, Object>> response = handler.handleAccessDenied(ex);

        assertThat(response.getStatusCode().value()).isEqualTo(403);
        assertThat(response.getBody()).containsEntry("errorCode", 2806);
        assertThat(response.getBody()).containsEntry("errorMessage", "User is not authorized for request operation");
    }

    @Test
    @DisplayName("AuthenticationFailedException → 401, code 2805, no internal details leaked")
    void handleAuthentication_returns401WithoutDetails() {
        AuthenticationFailedException ex = new AuthenticationFailedException("Invalid credentials");

        ResponseEntity<Map<String, Object>> response = handler.handleAuthentication(ex);

        assertThat(response.getStatusCode().value()).isEqualTo(401);
        assertThat(response.getBody()).containsEntry("errorCode", 2805);
        assertThat(response.getBody()).containsEntry("errorMessage", "Authentication fails");
    }

    @Test
    @DisplayName("AuthorizationException → 403, code 2806, no internal details leaked")
    void handleAuthorization_returns403WithoutDetails() {
        AuthorizationException ex = new AuthorizationException("User is not authorized");

        ResponseEntity<Map<String, Object>> response = handler.handleAuthorization(ex);

        assertThat(response.getStatusCode().value()).isEqualTo(403);
        assertThat(response.getBody()).containsEntry("errorCode", 2806);
        assertThat(response.getBody()).containsEntry("errorMessage", "User is not authorized for request operation");
    }

    @Test
    @DisplayName("EntityNotFoundException → 404, code 2835, entity details included")
    void handleNotFound_returns404WithDetails() {
        EntityNotFoundException ex = new EntityNotFoundException("Trainee not found: john.doe");

        ResponseEntity<Map<String, Object>> response = handler.handleNotFound(ex);

        assertThat(response.getStatusCode().value()).isEqualTo(404);
        assertThat(response.getBody()).containsEntry("errorCode", 2835);
        assertThat(response.getBody().get("errorMessage").toString()).contains("john.doe");
    }

    @Test
    @DisplayName("HibernateException → 500, code 3358, no internal details leaked")
    void handleHibernate_returns500WithoutDetails() {
        HibernateException ex = new HibernateException("connection refused to 192.168.1.1");

        ResponseEntity<Map<String, Object>> response = handler.handleHibernate(ex);

        assertThat(response.getStatusCode().value()).isEqualTo(500);
        assertThat(response.getBody()).containsEntry("errorCode", 3358);
        assertThat(response.getBody()).containsEntry("errorMessage", "Unexpected database access failure");
    }

    @Test
    @DisplayName("Generic Exception → 500, code 3200, no internal details leaked")
    void handleGeneric_returns500WithoutDetails() {
        RuntimeException ex = new RuntimeException("NullPointerException at line 42");

        ResponseEntity<Map<String, Object>> response = handler.handleGeneric(ex);

        assertThat(response.getStatusCode().value()).isEqualTo(500);
        assertThat(response.getBody()).containsEntry("errorCode", 3200);
        assertThat(response.getBody()).containsEntry("errorMessage", "Internal processing error");
    }

    @Test
    @DisplayName("HandlerMethodValidationException → 400, code 2760, details included")
    void handleHandlerMethodValidation_returns400WithDetails() {
        HandlerMethodValidationException ex = mock(HandlerMethodValidationException.class);
        ParameterValidationResult result = mock(ParameterValidationResult.class);
        MessageSourceResolvable error = mock(MessageSourceResolvable.class);

        when(ex.getAllValidationResults()).thenReturn(List.of(result));
        when(result.getResolvableErrors()).thenReturn(List.of(error));
        when(error.getDefaultMessage()).thenReturn("must not be blank");

        ResponseEntity<Map<String, Object>> response = handler.handleHandlerMethodValidation(ex);

        assertThat(response.getStatusCode().value()).isEqualTo(400);
        assertThat(response.getBody()).containsEntry("errorCode", 2760);
        assertThat(response.getBody().get("errorMessage").toString()).contains("must not be blank");
    }

    @Test
    @DisplayName("HttpMessageNotReadableException → 400, code 2760, generic message returned")
    void handleNotReadable_returns400WithGenericMessage() {
        HttpMessageNotReadableException ex = mock(HttpMessageNotReadableException.class);
        when(ex.getMessage()).thenReturn("JSON parse error");

        ResponseEntity<Map<String, Object>> response = handler.handleNotReadable(ex);

        assertThat(response.getStatusCode().value()).isEqualTo(400);
        assertThat(response.getBody()).containsEntry("errorCode", 2760);
        assertThat(response.getBody().get("errorMessage").toString()).contains("Malformed or missing request body");
    }

    @Test
    @DisplayName("MissingServletRequestParameterException → 400, code 2760, parameter name included")
    void handleMissingParam_returns400WithParamName() {
        MissingServletRequestParameterException ex = mock(MissingServletRequestParameterException.class);
        when(ex.getParameterName()).thenReturn("fromDate");

        ResponseEntity<Map<String, Object>> response = handler.handleMissingParam(ex);

        assertThat(response.getStatusCode().value()).isEqualTo(400);
        assertThat(response.getBody()).containsEntry("errorCode", 2760);
        assertThat(response.getBody().get("errorMessage").toString()).contains("fromDate");
    }

    @Test
    @DisplayName("PersistenceException → 500, code 3358, no internal details leaked")
    void handlePersistence_returns500WithoutDetails() {
        PersistenceException ex = new PersistenceException("constraint violation");

        ResponseEntity<Map<String, Object>> response = handler.handlePersistence(ex);

        assertThat(response.getStatusCode().value()).isEqualTo(500);
        assertThat(response.getBody()).containsEntry("errorCode", 3358);
        assertThat(response.getBody()).containsEntry("errorMessage", "Unexpected database access failure");
    }

    @Test
    @DisplayName("UserBlockedException → 403, code 2806, block details included")
    void handleUserBlockedException_returns403WithBlockMessage() {
        UserBlockedException ex = new UserBlockedException("User is blocked");

        ResponseEntity<Map<String, Object>> response = handler.handleUserBlockedException(ex);

        assertThat(response.getStatusCode().value()).isEqualTo(403);
        assertThat(response.getBody()).containsEntry("errorCode", 2806);
        assertThat(response.getBody().get("errorMessage").toString())
                .contains("User is not authorized for request operation")
                .contains("User is temporarily blocked due to multiple failed login attempts. Please try again later.");
    }
}