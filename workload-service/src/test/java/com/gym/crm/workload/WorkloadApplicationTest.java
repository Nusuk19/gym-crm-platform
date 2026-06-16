package com.gym.crm.workload;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

@SpringBootTest
class WorkloadApplicationTest {

    @Test
    void contextLoads() {
    }

    @Test
    void mainRuns() {
        assertDoesNotThrow(() -> WorkloadApplication.main(new String[]{}));
    }
}