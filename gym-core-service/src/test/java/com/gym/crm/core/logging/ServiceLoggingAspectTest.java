package com.gym.crm.core.logging;

import com.gym.crm.common.logging.AbstractServiceLoggingAspect;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ServiceLoggingAspectTest {

    private final AbstractServiceLoggingAspect aspect = new ServiceLoggingAspect();

    @AfterEach
    void tearDown() {
        MDC.clear();
    }

    @Test
    void logServiceOperation_shouldReturnResult() throws Throwable {
        ProceedingJoinPoint joinPoint = mockJoinPoint("greet", "hello world");

        Object result = aspect.logServiceOperation(joinPoint);

        assertThat(result).isEqualTo("hello world");
    }

    @Test
    void logServiceOperation_shouldRethrowException() throws Throwable {
        ProceedingJoinPoint joinPoint = mock(ProceedingJoinPoint.class);
        MethodSignature signature = mock(MethodSignature.class);
        when(joinPoint.getSignature()).thenReturn(signature);
        when(signature.getDeclaringType()).thenReturn(Object.class);
        when(signature.getName()).thenReturn("failing");
        when(joinPoint.getArgs()).thenReturn(new Object[]{});
        when(joinPoint.proceed()).thenThrow(new IllegalStateException("boom"));

        assertThatThrownBy(() -> aspect.logServiceOperation(joinPoint))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("boom");
    }

    @Test
    void logServiceOperation_shouldWorkWithTransactionIdInMdc() throws Throwable {
        MDC.put(TransactionIdFilter.TRANSACTION_ID_KEY, "test-tx-id");
        ProceedingJoinPoint joinPoint = mockJoinPoint("greet", "hello mdc");

        Object result = aspect.logServiceOperation(joinPoint);

        assertThat(result).isEqualTo("hello mdc");
    }

    @Test
    void logServiceOperation_shouldWorkWithoutTransactionIdInMdc() throws Throwable {
        ProceedingJoinPoint joinPoint = mockJoinPoint("greet", "hello no-mdc");

        Object result = aspect.logServiceOperation(joinPoint);

        assertThat(result).isEqualTo("hello no-mdc");
    }

    private ProceedingJoinPoint mockJoinPoint(String methodName, Object returnValue) throws Throwable {
        ProceedingJoinPoint joinPoint = mock(ProceedingJoinPoint.class);
        MethodSignature signature = mock(MethodSignature.class);
        when(joinPoint.getSignature()).thenReturn(signature);
        when(signature.getDeclaringType()).thenReturn(Object.class);
        when(signature.getName()).thenReturn(methodName);
        when(joinPoint.getArgs()).thenReturn(new Object[]{});
        when(joinPoint.proceed()).thenReturn(returnValue);

        return joinPoint;
    }
}