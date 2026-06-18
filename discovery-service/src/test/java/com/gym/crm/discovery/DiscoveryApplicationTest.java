package com.gym.crm.discovery;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

@SpringBootTest
class DiscoveryApplicationTest {

    @Test
    void contextLoads() {
    }

    @Test
    void main_shouldStartApplication() {
        assertDoesNotThrow(() -> DiscoveryApplication.main(new String[]{}));
    }
}