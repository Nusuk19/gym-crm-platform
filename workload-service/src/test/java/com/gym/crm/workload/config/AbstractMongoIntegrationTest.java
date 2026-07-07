package com.gym.crm.workload.config;

import com.gym.crm.workload.repository.TrainerWorkloadRepository;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

@ActiveProfiles("test")
public abstract class AbstractMongoIntegrationTest {

    @Autowired
    protected TrainerWorkloadRepository repository;

    @DynamicPropertySource
    static void setMongoProperties(DynamicPropertyRegistry registry) {
        MongoContainerTestConfig.setMongoProperties(registry);
    }

    @BeforeEach
    void cleanRepository() {
        repository.deleteAll();
    }
}