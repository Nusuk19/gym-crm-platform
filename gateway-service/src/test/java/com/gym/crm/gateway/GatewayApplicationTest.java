package com.gym.crm.gateway;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

@SpringBootTest
class GatewayApplicationTest {

    @Test
    void contextLoads() {
    }

    @Test
    void main_shouldStartApplication() {
        assertDoesNotThrow(() -> GatewayApplication.main(new String[]{}));
    }
}