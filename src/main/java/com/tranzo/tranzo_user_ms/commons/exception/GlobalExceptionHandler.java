package com.tranzo.tranzo_user_ms.commons.exception;

import com.tranzo.tranzo_user_ms.commons.dto.ErrorDetailsDto;
import com.tranzo.tranzo_user_ms.commons.utility.ExceptionResponseUtil;
import com.tranzo.tranzo_user_ms.commons.dto.ResponseDto;
import com.tranzo.tranzo_user_ms.trip.enums.TripPublishErrorCode;
import com.tranzo.tranzo_user_ms.trip.exception.TripPublishException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BaseException.class)
    public ResponseEntity<ResponseDto<Void>> handleBaseException(BaseException ex) {
        return ExceptionResponseUtil.build(
                ex.getMessage(),
                ex.getStatusCode(),
                null
        );
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ResponseDto<Map<String, String>>> handleValidationErrors(
            MethodArgumentNotValidException ex) {

        Map<String, String> errors = new HashMap<>();

        ex.getBindingResult().getFieldErrors()
                .forEach(error ->
                        errors.put(error.getField(), error.getDefaultMessage()));

        ex.getBindingResult().getGlobalErrors()
                .forEach(error ->
                        errors.put(error.getObjectName(), error.getDefaultMessage()));

        return ExceptionResponseUtil.build(
                "Validation failed",
                400,
                errors
        );
    }

    @ExceptionHandler(AuthException.class)
    public ResponseEntity<ResponseDto<Void>> handleAuthException(AuthException ex) {

        ResponseDto<Void> response = ResponseDto.<Void>builder()
                .status("ERROR")
                .statusCode(ex.getStatusCode())
                .statusMessage(ex.getMessage())
                .data(null)
                .build();

        return ResponseEntity.status(ex.getStatusCode()).body(response);
    }

    @ExceptionHandler(TripPublishException.class)
    public ResponseEntity<ResponseDto<ErrorDetailsDto>> handleTripPublishException(
            TripPublishException ex) {

        ErrorDetailsDto errorDetails = ErrorDetailsDto.builder()
                .errorCode(ex.getErrorCode().name())
                .message(resolveMessage(ex.getErrorCode()))
                .build();

        return ResponseEntity
                .badRequest()
                .body(ResponseDto.failure(
                        400,
                        "Trip publish validation failed",
                        errorDetails
                ));
    }

    // Fallback handler (VERY IMPORTANT)
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ResponseDto<Void>> handleUnhandledException(Exception ex) {
        return ExceptionResponseUtil.build(
                "Something went wrong",
                500,
                null
        );
    }

    private String resolveMessage(TripPublishErrorCode errorCode) {
        return switch (errorCode) {
            case TITLE_MISSING -> "Trip title is required for publishing";
            case DESCRIPTION_MISSING -> "Trip description is required for publishing";
            case DESTINATION_MISSING -> "Trip destination is required for publishing";
            case START_DATE_MISSING -> "Trip start date is required";
            case END_DATE_MISSING -> "Trip end date is required";
            case INVALID_DATE_RANGE -> "Trip end date cannot be before start date";
            case ESTIMATED_BUDGET_MISSING -> "Estimated budget is required";
            case INVALID_ESTIMATED_BUDGET -> "Estimated budget must be positive";
            case MAX_PARTICIPANTS_MISSING -> "Max participants is required";
            case INVALID_MAX_PARTICIPANTS -> "Max participants must be greater than zero";
            case JOIN_POLICY_MISSING -> "Join policy is required";
            case VISIBILITY_STATUS_MISSING -> "Visibility status is required";
            case TRIP_POLICY_MISSING -> "Trip policy is required";
            case ITINERARY_MISSING -> "At least one itinerary is required";
            case TRIP_ALREADY_PUBLISHED -> "Trip is already published";
            case TRIP_NOT_FOUND -> "Trip not found";
            case TRIP_NOT_PUBLISHED -> "Trip not published";
        };
    }

}