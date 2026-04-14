package com.tranzo.tranzo_user_ms.user.service;

import com.tranzo.tranzo_user_ms.trip.client.TripStatisticsClient;
import com.tranzo.tranzo_user_ms.user.client.UserProfileClient;
import com.tranzo.tranzo_user_ms.user.dto.SuggestedTravelPalDto;
import com.tranzo.tranzo_user_ms.user.dto.UserNameDto;
import com.tranzo.tranzo_user_ms.user.enums.TravelPalStatus;
import com.tranzo.tranzo_user_ms.user.model.TravelPalEntity;
import com.tranzo.tranzo_user_ms.user.repository.TravelPalRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("TravelPalService Unit Tests")
class TravelPalServiceTest {

    @Mock
    private TravelPalRepository repository;

    @Mock
    private UserProfileClient userProfileClient;

    @Mock
    private TripStatisticsClient tripStatisticsClient;

    @Mock
    private RatingService ratingService;

    @InjectMocks
    private TravelPalService service;

    private UUID userA;
    private UUID userB;
    private TravelPalEntity pendingEntity;

    @BeforeEach
    void setUp() {
        userA = UUID.randomUUID();
        userB = UUID.randomUUID();
        pendingEntity = new TravelPalEntity();
        pendingEntity.setUserLowId(userA.compareTo(userB) < 0 ? userA : userB);
        pendingEntity.setUserHighId(userA.compareTo(userB) < 0 ? userB : userA);
        pendingEntity.setRequestedBy(userA);
        pendingEntity.setStatus(TravelPalStatus.PENDING);
    }

    @Test
    @DisplayName("Should send request successfully when no existing connection")
    void sendRequest_Success() {
        when(repository.findByUserLowIdAndUserHighId(any(UUID.class), any(UUID.class))).thenReturn(Optional.empty());
        when(repository.save(any(TravelPalEntity.class))).thenReturn(pendingEntity);

        assertDoesNotThrow(() -> service.sendRequest(userA, userB));
        verify(repository).save(any(TravelPalEntity.class));
    }

    @Test
    @DisplayName("Should throw when sending request to self")
    void sendRequest_SelfRequest() {
        assertThrows(IllegalArgumentException.class, () -> service.sendRequest(userA, userA));
        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("Should accept request successfully")
    void acceptRequest_Success() {
        when(repository.findByUserLowIdAndUserHighId(any(UUID.class), any(UUID.class)))
                .thenReturn(Optional.of(pendingEntity));

        service.acceptRequest(userB, userA);

        assertEquals(TravelPalStatus.ACCEPTED, pendingEntity.getStatus());
    }

    @Test
    @DisplayName("Should reject request successfully")
    void rejectRequest_Success() {
        when(repository.findByUserLowIdAndUserHighId(any(UUID.class), any(UUID.class)))
                .thenReturn(Optional.of(pendingEntity));

        service.rejectRequest(userB, userA);

        assertEquals(TravelPalStatus.REJECTED, pendingEntity.getStatus());
    }

    @Test
    @DisplayName("Should get my travel pals")
    void getMyTravelPals_Success() {
        when(repository.findAcceptedByUser(userA)).thenReturn(List.of(pendingEntity));
        pendingEntity.setStatus(TravelPalStatus.ACCEPTED);

        List<UUID> pals = service.getMyTravelPals(userA);

        assertNotNull(pals);
        assertEquals(1, pals.size());
        assertEquals(userB, pals.get(0));
    }

    @Test
    @DisplayName("Should get incoming pending requests")
    void getIncomingPendingRequests_Success() {
        when(repository.findIncomingPending(userB)).thenReturn(List.of(pendingEntity));
        when(repository.findAcceptedByUser(userA)).thenReturn(List.of());
        when(tripStatisticsClient.getCompletedTripsCount(userA)).thenReturn(0);
        when(ratingService.getUserAverageRating(userA)).thenReturn(null);

        UserNameDto userNameDto = UserNameDto.builder()
                .userId(userA)
                .firstName("Test")
                .lastName("User")
                .build();
        Map<UUID, UserNameDto> userDetails = new HashMap<>();
        userDetails.put(userA, userNameDto);
        when(userProfileClient.getNamesByUserIds(List.of(userA))).thenReturn(userDetails);

        List<SuggestedTravelPalDto> pending = service.getIncomingPendingRequestsWithDetails(userB);

        assertNotNull(pending);
        assertEquals(1, pending.size());
    }
}