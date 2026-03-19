package com.tranzo.tranzo_user_ms.commons.exception;

import com.tranzo.tranzo_user_ms.commons.dto.ResponseDto;
import com.tranzo.tranzo_user_ms.trip.enums.TripErrorCode;
import com.tranzo.tranzo_user_ms.trip.exception.TripException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("GlobalExceptionHandler Unit Tests")
class GlobalExceptionHandlerTest {

    @InjectMocks
    private GlobalExceptionHandler handler;

    @Test
    @DisplayName("Should handle BaseException and return correct status")
    void handleBaseException() {
        BaseException ex = new UserProfileNotFoundException("User not found");
        ResponseEntity<ResponseDto<Void>> res = handler.handleBaseException(ex);

        assertEquals(404, res.getStatusCodeValue());
        assertNotNull(res.getBody());
        assertEquals("ERROR", res.getBody().getStatus());
        assertEquals("User not found", res.getBody().getStatusMessage());
    }

    @Test
    @DisplayName("Should handle AuthException and return correct status")
    void handleAuthException() {
        AuthException ex = new UnauthorizedException("Unauthenticated");
        ResponseEntity<ResponseDto<Void>> res = handler.handleAuthException(ex);

        assertEquals(401, res.getStatusCodeValue());
        assertNotNull(res.getBody());
        assertEquals(401, res.getBody().getStatusCode());
    }

    @Test
    @DisplayName("Should handle TripException and return error details")
    void handleTripException() {
        TripException ex = new TripException(TripErrorCode.TITLE_MISSING, 400);
        ResponseEntity<ResponseDto<com.tranzo.tranzo_user_ms.commons.dto.ErrorDetailsDto>> res =
                handler.handleTripException(ex);

        assertEquals(HttpStatus.BAD_REQUEST, res.getStatusCode());
        assertNotNull(res.getBody());
        assertNotNull(res.getBody().getData());
        assertEquals("TITLE_MISSING", res.getBody().getData().getErrorCode());
    }

    @Test
    @DisplayName("Should handle generic Exception and return 500")
    void handleUnhandledException() {
        Exception ex = new RuntimeException("Unexpected");
        ResponseEntity<ResponseDto<Void>> res = handler.handleUnhandledException(ex);

        assertEquals(500, res.getStatusCodeValue());
        assertNotNull(res.getBody());
        assertEquals("Something went wrong", res.getBody().getStatusMessage());
    }
}
