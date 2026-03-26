package com.tranzo.tranzo_user_ms;

import com.tranzo.tranzo_user_ms.config.TestConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

/**
 * Base for full integration API tests. Uses H2 + data.sql.
 * Do not add @WithMockUser here; use it on individual test methods for authenticated vs unauthenticated scenarios.
 */
@SpringBootTest(classes = TranzoUserMsApplication.class, properties = "spring.profiles.active=test")
@AutoConfigureMockMvc
@Import(TestConfig.class)
public abstract class ApiTestBase {

    protected static final String USER_UUID_1 = "11111111-1111-4111-8111-111111111111";
    protected static final String USER_UUID_2 = "22222222-2222-4222-8222-222222222222";
    protected static final UUID COMPLETED_TRIP_ID = UUID.fromString("eeeeeeee-eeee-4eee-8eee-eeeeeeeeeeee");
    protected static final String NOTIFICATION_ID_USER1 = "d4000001-0001-4001-8001-000000000001";
    protected static final UUID NON_EXISTENT_UUID = UUID.fromString("00000000-0000-4000-8000-000000000000");

    @Autowired
    protected MockMvc mvc;
}
