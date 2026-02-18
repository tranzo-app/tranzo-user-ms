package com.tranzo.tranzo_user_ms.trip;

import org.junit.jupiter.api.DisplayName;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

/**
 * Base class for all integration tests in the trip module.
 * Provides common configuration for Spring Boot integration tests.
 */
@SpringBootTest
@TestPropertySource(locations = "classpath:application-test.properties")
@DisplayName("Trip Module Integration Tests")
public class TripIntegrationTestBase {
    // Base configuration for integration tests
}

