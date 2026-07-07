package com.gym.crm.workload;

import com.gym.crm.workload.config.MongoContainerTestConfig;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

@SpringBootTest
@ActiveProfiles("test")
class WorkloadApplicationTest {

    @DynamicPropertySource
    static void setMongoProperties(DynamicPropertyRegistry registry) {
        MongoContainerTestConfig.setMongoContainerProperties(registry);
    }

    @Test
    void contextLoads() {
    }

    @Test
    void mainRuns() {
        assertDoesNotThrow(() -> WorkloadApplication.main(new String[]{}));
    }
}