package com.tranzo.tranzo_user_ms.trip.utility;

import com.tranzo.tranzo_user_ms.commons.exception.ForbiddenException;
import com.tranzo.tranzo_user_ms.trip.enums.TripMemberRole;
import com.tranzo.tranzo_user_ms.trip.repository.TripMemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class UserUtil {
    TripMemberRepository tripMemberRepository;

    public void validateUserIsHost(UUID tripId, UUID userId)
    {
        tripMemberRepository.findByTrip_TripIdAndUserIdAndRole(tripId, userId, TripMemberRole.HOST)
                .orElseThrow(() -> new ForbiddenException("User is forbidden to perform trip update as user is not HOST"));
    }
}
