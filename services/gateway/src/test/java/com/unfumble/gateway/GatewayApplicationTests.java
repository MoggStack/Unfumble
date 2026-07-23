package com.unfumble.gateway;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

/**
 * Smoke test — verifies that the Spring application context loads without errors.
 *
 * Run with:  ./mvnw test
 *
 * This test uses the 'test' profile so it can override datasource settings
 * (e.g., point to an in-memory or test-container database in the future).
 */
@SpringBootTest
@ActiveProfiles("test")
class GatewayApplicationTests {

    @Test
    void contextLoads() {
        // If the context starts up, the test passes.
        // No assertions needed — a startup failure will cause this test to fail.
    }
}
