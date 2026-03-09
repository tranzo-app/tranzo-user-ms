package com.tranzo.tranzo_user_ms.user.controller;

import com.tranzo.tranzo_user_ms.commons.dto.ResponseDto;
import com.tranzo.tranzo_user_ms.user.dto.SessionRequestDto;
import com.tranzo.tranzo_user_ms.user.dto.SessionResponseDto;
import com.tranzo.tranzo_user_ms.user.service.SessionService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("SessionController Unit Tests")
class SessionControllerTest {

    @Mock
    private SessionService sessionService;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @InjectMocks
    private SessionController controller;

    @Test
    @DisplayName("Should create session on login")
    void createSession_Success() {
        SessionRequestDto dto = SessionRequestDto.builder().emailId("u@test.com").build();
        SessionResponseDto sessionResponse = new SessionResponseDto();
        when(sessionService.createSession(eq(dto), any(HttpServletResponse.class))).thenReturn(sessionResponse);

        ResponseEntity<ResponseDto<SessionResponseDto>> res = controller.createSession(dto, response);

        assertEquals(HttpStatus.OK, res.getStatusCode());
        assertNotNull(res.getBody());
        assertEquals(200, res.getBody().getStatusCode());
        assertEquals("Session created successfully", res.getBody().getStatusMessage());
    }

    @Test
    @DisplayName("Should refresh session")
    void refreshSession_Success() throws Exception {
        SessionResponseDto sessionResponse = new SessionResponseDto();
        when(sessionService.refreshSession(any(HttpServletRequest.class), any(HttpServletResponse.class)))
                .thenReturn(sessionResponse);

        ResponseEntity<ResponseDto<SessionResponseDto>> res = controller.refreshSession(request, response);

        assertEquals(HttpStatus.OK, res.getStatusCode());
        assertEquals("Session refreshed successfully", res.getBody().getStatusMessage());
    }

    @Test
    @DisplayName("Should logout successfully")
    void logout_Success() {
        ResponseEntity<ResponseDto<SessionResponseDto>> res = controller.logout(request, response);

        assertEquals(HttpStatus.OK, res.getStatusCode());
        assertEquals("Session logged out successfully", res.getBody().getStatusMessage());
        verify(sessionService).logout(eq(request), eq(response));
    }
}
