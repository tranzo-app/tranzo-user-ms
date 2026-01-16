package com.tranzo.tranzo_user_ms.trip.utility;


import com.tranzo.tranzo_user_ms.trip.dto.CreateTripRequestDto;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class TripDateRangeValidator
        implements ConstraintValidator<TripDateRangeValid, CreateTripRequestDto> {

    @Override
    public boolean isValid(CreateTripRequestDto dto, ConstraintValidatorContext context) {
        if (dto.getTripStartDate() == null || dto.getTripEndDate() == null) {
            return true;
        }
        return dto.getTripEndDate().isAfter(dto.getTripStartDate());
    }
}

