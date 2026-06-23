package com.gym.crm.core.client.workload;

import com.gym.crm.core.client.workload.model.TrainerWorkloadRequest;
import com.gym.crm.core.exception.ServiceConnectionException;
import com.gym.crm.core.exception.ServiceException;
import com.gym.crm.core.exception.ServiceTimeoutException;
import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.RestClient;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.SocketTimeoutException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(MockitoExtension.class)
class WorkloadServiceClientFallbackTest {

    @Mock
    RestClient restClient;

    @InjectMocks
    WorkloadServiceClient client;

    @Test
    void fallback_shouldThrowServiceConnectionException_onCallNotPermitted() {
        TrainerWorkloadRequest request = new TrainerWorkloadRequest();

        CircuitBreaker cb = CircuitBreaker.ofDefaults("workload-service");
        CallNotPermittedException ex = CallNotPermittedException.createCallNotPermittedException(cb);

        assertThatThrownBy(() -> invokeFallback(request, ex))
                .isInstanceOf(ServiceConnectionException.class)
                .hasMessageContaining("circuit breaker open");
    }

    @Test
    void fallback_shouldThrowServiceTimeoutException_onSocketTimeout() {
        TrainerWorkloadRequest request = new TrainerWorkloadRequest();

        SocketTimeoutException cause = new SocketTimeoutException("timeout");

        assertThatThrownBy(() -> invokeFallback(request, cause)).isInstanceOf(ServiceTimeoutException.class);
    }

    @Test
    void fallback_shouldReturnSameServiceException_whenAlreadyMapped() {
        TrainerWorkloadRequest request = new TrainerWorkloadRequest();
        ServiceException original = new ServiceTimeoutException("timeout");

        RuntimeException thrown = assertThrows(ServiceException.class, () -> invokeFallback(request, original));

        assertThat(thrown).isSameAs(original);
    }

    @Test
    void fallback_shouldThrowGenericException_onUnknownError() {
        TrainerWorkloadRequest request = new TrainerWorkloadRequest();

        RuntimeException cause = new RuntimeException("boom");

        assertThatThrownBy(() -> invokeFallback(request, cause)).hasMessageContaining("Unexpected error");
    }

    private void invokeFallback(TrainerWorkloadRequest request, Exception ex) {
        try {
            Method method = WorkloadServiceClient.class
                    .getDeclaredMethod("fallback", TrainerWorkloadRequest.class, Exception.class);
            method.setAccessible(true);
            method.invoke(client, request, ex);
        } catch (InvocationTargetException e) {
            throw (RuntimeException) e.getCause();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
