package com.tranzo.tranzo_user_ms.commons.utility;

import com.tranzo.tranzo_user_ms.commons.dto.ResponseDto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("ExceptionResponseUtil Unit Tests")
class ExceptionResponseUtilTest {

    @Test
    @DisplayName("Should build error response with message and status code")
    void build_WithMessageAndStatusCode() {
        ResponseEntity<ResponseDto<Void>> res = ExceptionResponseUtil.build("Not found", 404, null);

        assertEquals(404, res.getStatusCodeValue());
        assertNotNull(res.getBody());
        assertEquals("ERROR", res.getBody().getStatus());
        assertEquals(404, res.getBody().getStatusCode());
        assertEquals("Not found", res.getBody().getStatusMessage());
        assertNull(res.getBody().getData());
    }

    @Test
    @DisplayName("Should build error response with data")
    void build_WithData() {
        Map<String, String> errors = Map.of("field", "invalid");
        ResponseEntity<ResponseDto<Map<String, String>>> res = ExceptionResponseUtil.build("Validation failed", 400, errors);

        assertEquals(400, res.getStatusCodeValue());
        assertNotNull(res.getBody().getData());
        assertEquals("invalid", res.getBody().getData().get("field"));
    }
}
