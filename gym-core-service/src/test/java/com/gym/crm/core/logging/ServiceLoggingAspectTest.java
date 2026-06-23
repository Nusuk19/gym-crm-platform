package com.gym.crm.core.logging;

import com.gym.crm.core.logging.TransactionIdFilter;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;
import org.springframework.aop.aspectj.annotation.AspectJProxyFactory;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ServiceLoggingAspectTest {

    private ServiceLoggingAspect aspect;
    private SampleService proxy;

    @BeforeEach
    void setUp() {
        aspect = new ServiceLoggingAspect();
        SampleService target = new SampleService();
        AspectJProxyFactory factory = new AspectJProxyFactory(target);
        factory.addAspect(aspect);
        proxy = factory.getProxy();
    }

    @AfterEach
    void tearDown() {
        MDC.clear();
    }

    @Test
    void aspect_shouldReturnResultFromTargetMethod() {
        String result = proxy.greet("world");
        assertThat(result).isEqualTo("hello world");
    }

    @Test
    void aspect_shouldNotSwallowException_whenMethodThrows() {
        assertThatThrownBy(() -> proxy.failing())
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("boom");
    }

    @Test
    void aspect_shouldWorkWithTransactionIdInMdc() {
        MDC.put(TransactionIdFilter.TRANSACTION_ID_KEY, "test-tx-id");

        String result = proxy.greet("mdc");

        assertThat(result).isEqualTo("hello mdc");
    }

    @Test
    void aspect_shouldWorkWithoutTransactionIdInMdc() {
        String result = proxy.greet("no-mdc");
        assertThat(result).isEqualTo("hello no-mdc");
    }

    static class SampleService {
        public String greet(String name) {
            return "hello " + name;
        }

        public void failing() {
            throw new IllegalStateException("boom");
        }
    }
}
