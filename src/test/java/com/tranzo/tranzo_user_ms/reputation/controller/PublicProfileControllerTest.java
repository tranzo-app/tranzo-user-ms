package com.tranzo.tranzo_user_ms.reputation.controller;

import com.tranzo.tranzo_user_ms.user.controller.PublicProfileController;
import com.tranzo.tranzo_user_ms.user.dto.PublicProfileResponseDto;
import com.tranzo.tranzo_user_ms.user.service.PublicProfileService;
import com.tranzo.tranzo_user_ms.user.controller.AadharController;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("PublicProfileController Unit Tests")
class PublicProfileControllerTest {

    @Mock
    private PublicProfileService publicProfileService;

    @InjectMocks
    private PublicProfileController controller;

    private UUID userId;
    private PublicProfileResponseDto profileDto;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        profileDto = PublicProfileResponseDto.builder()
                .firstName("Jane")
                .lastName("Doe")
                .trustScore(new BigDecimal("4.50"))
                .reviews(List.of())
                .totalReviewCount(0)
                .build();
    }

    @Test
    @DisplayName("Should return public profile with default pagination")
    void getPublicProfile_Success() {
        when(publicProfileService.getPublicProfile(userId, 0, 20)).thenReturn(profileDto);

        ResponseEntity<?> res = controller.getPublicProfile(userId, 0, 20);

        assertEquals(HttpStatus.OK, res.getStatusCode());
        assertNotNull(res.getBody());
        verify(publicProfileService).getPublicProfile(userId, 0, 20);
    }

    @Test
    @DisplayName("Should use custom page and size")
    void getPublicProfile_CustomPagination() {
        when(publicProfileService.getPublicProfile(userId, 2, 10)).thenReturn(profileDto);

        ResponseEntity<?> res = controller.getPublicProfile(userId, 2, 10);

        assertEquals(HttpStatus.OK, res.getStatusCode());
        verify(publicProfileService).getPublicProfile(userId, 2, 10);
    }
}
