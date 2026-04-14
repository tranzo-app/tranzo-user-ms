package com.tranzo.tranzo_user_ms.commons.exception;

import com.tranzo.tranzo_user_ms.chat.exception.ChatException;
import com.tranzo.tranzo_user_ms.chat.enums.ChatErrorCode;
import com.tranzo.tranzo_user_ms.commons.dto.ErrorDetailsDto;
import com.tranzo.tranzo_user_ms.commons.utility.ExceptionResponseUtil;
import com.tranzo.tranzo_user_ms.commons.dto.ResponseDto;
import com.tranzo.tranzo_user_ms.notification.enums.NotificationErrorCode;
import com.tranzo.tranzo_user_ms.notification.exception.NotificationException;
import com.tranzo.tranzo_user_ms.splitwise.enums.SplitwiseErrorCode;
import com.tranzo.tranzo_user_ms.splitwise.exception.SplitwiseException;
import com.tranzo.tranzo_user_ms.trip.enums.TripErrorCode;
import com.tranzo.tranzo_user_ms.trip.exception.TripException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

/**
 * Global Exception Handler for the application
 * Centralizes exception handling across all controllers
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    /**
     * Handle BaseException - custom business exceptions
     */
    @ExceptionHandler(BaseException.class)
    public ResponseEntity<ResponseDto<Void>> handleBaseException(BaseException ex) {
        log.warn("Business exception | type={} | message={}", ex.getClass().getSimpleName(), ex.getMessage());
        return ExceptionResponseUtil.build(
                ex.getMessage(),
                ex.getStatusCode(),
                null
        );
    }

    /**
     * Handle ChatException - chat-specific exceptions with error codes
     */
    @ExceptionHandler(ChatException.class)
    public ResponseEntity<ResponseDto<ErrorDetailsDto>> handleChatException(
            ChatException ex) {
        log.warn("Chat exception | errorCode={} | message={}", ex.getErrorCode(), ex.getMessage());

        ErrorDetailsDto errorDetails = ErrorDetailsDto.builder()
                .errorCode(ex.getErrorCode().getCode())
                .message(resolveMessage(ex.getErrorCode()))
                .build();

        return ResponseEntity
                .status(ex.getStatusCode())
                .body(ResponseDto.failure(
                        ex.getStatusCode(),
                        "Chat operation failed",
                        errorDetails
                ));
    }

    /**
     * Handle SplitwiseException - splitwise-specific exceptions with error codes
     */
    @ExceptionHandler(SplitwiseException.class)
    public ResponseEntity<ResponseDto<ErrorDetailsDto>> handleSplitwiseException(
            SplitwiseException ex) {
        log.warn("Splitwise exception | errorCode={} | message={}", ex.getErrorCode(), ex.getMessage());

        ErrorDetailsDto errorDetails = ErrorDetailsDto.builder()
                .errorCode(ex.getErrorCode().getCode())
                .message(resolveMessage(ex.getErrorCode()))
                .build();

        return ResponseEntity
                .status(ex.getStatusCode())
                .body(ResponseDto.failure(
                        ex.getStatusCode(),
                        "Splitwise operation failed",
                        errorDetails
                ));
    }

    /**
     * Handle NotificationException - notification-specific exceptions with error codes
     */
    @ExceptionHandler(NotificationException.class)
    public ResponseEntity<ResponseDto<ErrorDetailsDto>> handleNotificationException(
            NotificationException ex) {
        log.warn("Notification exception | errorCode={} | message={}", ex.getErrorCode(), ex.getMessage());

        ErrorDetailsDto errorDetails = ErrorDetailsDto.builder()
                .errorCode(ex.getErrorCode().getCode())
                .message(resolveMessage(ex.getErrorCode()))
                .build();

        return ResponseEntity
                .status(ex.getStatusCode())
                .body(ResponseDto.failure(
                        ex.getStatusCode(),
                        "Notification operation failed",
                        errorDetails
                ));
    }

    /**
     * Handle TripException - trip-specific exceptions with error codes
     */
    @ExceptionHandler(TripException.class)
    public ResponseEntity<ResponseDto<ErrorDetailsDto>> handleTripException(
            TripException ex) {
        log.warn("Trip exception | errorCode={} | message={}", ex.getErrorCode(), ex.getMessage());

        ErrorDetailsDto errorDetails = ErrorDetailsDto.builder()
                .errorCode(ex.getErrorCode().getCode())
                .message(resolveMessage(ex.getErrorCode()))
                .build();

        return ResponseEntity
                .status(ex.getStatusCode())
                .body(ResponseDto.failure(
                        ex.getStatusCode(),
                        "Trip operation failed",
                        errorDetails
                ));
    }

    /**
     * Handle MethodArgumentNotValidException - validation errors
     */
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

        log.warn("Validation failed | errors={}", errors);

        return ExceptionResponseUtil.build(
                "Validation failed",
                400,
                errors
        );
    }

    /**
     * Handle AuthException - authentication and authorization errors
     */
    @ExceptionHandler(AuthException.class)
    public ResponseEntity<ResponseDto<Void>> handleAuthException(AuthException ex) {
        log.warn("Authentication exception | message={}", ex.getMessage());

        ResponseDto<Void> response = ResponseDto.<Void>builder()
                .status("ERROR")
                .statusCode(ex.getStatusCode())
                .statusMessage(ex.getMessage())
                .data(null)
                .build();

        return ResponseEntity.status(ex.getStatusCode()).body(response);
    }

    /**
     * Handle BadRequestException - bad request errors
     */
    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<ResponseDto<Void>> handleBadRequestException(BadRequestException ex) {
        log.warn("Bad request exception | message={}", ex.getMessage());
        return ExceptionResponseUtil.build(
                ex.getMessage(),
                400,
                null
        );
    }

    /**
     * Handle ConflictException - conflict errors
     */
    @ExceptionHandler(ConflictException.class)
    public ResponseEntity<ResponseDto<Void>> handleConflictException(ConflictException ex) {
        log.warn("Conflict exception | message={}", ex.getMessage());
        return ExceptionResponseUtil.build(
                ex.getMessage(),
                409,
                null
        );
    }

    /**
     * Handle ForbiddenException - forbidden access errors
     */
    @ExceptionHandler(ForbiddenException.class)
    public ResponseEntity<ResponseDto<Void>> handleForbiddenException(ForbiddenException ex) {
        log.warn("Forbidden exception | message={}", ex.getMessage());
        return ExceptionResponseUtil.build(
                ex.getMessage(),
                403,
                null
        );
    }

    /**
     * Handle EntityNotFoundException - entity not found errors
     */
    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<ResponseDto<Void>> handleEntityNotFoundException(EntityNotFoundException ex) {
        log.warn("Entity not found exception | message={}", ex.getMessage());
        return ExceptionResponseUtil.build(
                ex.getMessage(),
                404,
                null
        );
    }

    /**
     * Fallback handler for all unhandled exceptions
     * (VERY IMPORTANT - Catch-all to prevent stack traces leaking to client)
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ResponseDto<Void>> handleUnhandledException(Exception ex) {
        log.error("Unhandled exception | type={} | message={}", ex.getClass().getSimpleName(), ex.getMessage(), ex);

        return ExceptionResponseUtil.build(
                "Something went wrong. Please try again later.",
                500,
                null
        );
    }

    /**
     * Resolve user-friendly message for ChatErrorCode
     */
    private String resolveMessage(ChatErrorCode errorCode) {
        return switch (errorCode) {
            case CONVERSATION_NOT_FOUND -> "Conversation not found";
            case RECIPIENT_NOT_FOUND -> "Recipient not found";
            case TRIP_NOT_FOUND -> "Trip not found";
            case INVALID_MESSAGE -> "Invalid message format";
            case MESSAGE_TOO_LONG -> "Message exceeds maximum length";
            case MESSAGE_EMPTY -> "Message cannot be empty";
            case USER_NOT_IN_CONVERSATION -> "User is not a participant in this conversation";
            case USER_LEFT_CONVERSATION -> "User has left this conversation";
            case USER_BLOCKED -> "User is blocked from this conversation";
            case USER_MUTED -> "User is muted in this conversation";
            case BLOCK_NOT_ALLOWED -> "Blocking operation not allowed";
            case UNBLOCK_NOT_ALLOWED -> "Unblocking operation not allowed";
            case SELF_CONVERSATION -> "Cannot create conversation with yourself";
            case INVALID_LIMIT -> "Invalid limit parameter";
            case INVALID_CONVERSATION_TYPE -> "Invalid conversation type";
        };
    }

    /**
     * Resolve user-friendly message for SplitwiseErrorCode
     */
    private String resolveMessage(SplitwiseErrorCode errorCode) {
        return switch (errorCode) {
            case GROUP_NOT_FOUND -> "Group not found";
            case EXPENSE_NOT_FOUND -> "Expense not found";
            case EXPENSE_SPLIT_INVALID -> "Expense split configuration is invalid";
            case SETTLEMENT_NOT_FOUND -> "Settlement not found";
            case USER_NOT_MEMBER -> "User is not a member of this group";
            case INSUFFICIENT_BALANCE -> "Insufficient balance";
        };
    }

    /**
     * Resolve user-friendly message for NotificationErrorCode
     */
    private String resolveMessage(NotificationErrorCode errorCode) {
        return switch (errorCode) {
            case NOTIFICATION_NOT_FOUND -> "Notification not found";
            case NOTIFICATION_ACCESS_DENIED -> "Access denied to this notification";
        };
    }

    /**
     * Resolve user-friendly message for TripErrorCode
     */
    private String resolveMessage(TripErrorCode errorCode) {
        return switch (errorCode) {
            // Trip existence and access errors
            case TRIP_NOT_FOUND -> "Trip not found";
            case TRIP_ACCESS_DENIED -> "Access denied to this trip";

            // Trip status errors
            case INVALID_TRIP_STATUS_TRANSITION -> "Invalid trip status transition";
            case TRIP_ALREADY_CANCELLED -> "Trip is already cancelled";
            case TRIP_NOT_PUBLISHED -> "Trip is not published";
            case TRIP_ALREADY_PUBLISHED -> "Trip is already published";

            // Trip validation errors
            case TITLE_MISSING -> "Trip title is required";
            case DESCRIPTION_MISSING -> "Trip description is required";
            case DESTINATION_MISSING -> "Trip destination is required";
            case START_DATE_MISSING -> "Trip start date is required";
            case END_DATE_MISSING -> "Trip end date is required";
            case INVALID_DATE_RANGE -> "Trip end date cannot be before start date";
            case ESTIMATED_BUDGET_MISSING -> "Estimated budget is required";
            case INVALID_ESTIMATED_BUDGET -> "Estimated budget must be positive";
            case MAX_PARTICIPANTS_MISSING -> "Max participants is required";
            case INVALID_MAX_PARTICIPANTS -> "Max participants must be greater than zero";
            case JOIN_POLICY_MISSING -> "Join policy is required";
            case ITINERARY_MISSING -> "At least one itinerary is required";

            // Trip capacity and membership errors
            case TRIP_FULL -> "Trip is already full";
            case TRIP_ALREADY_MARKED_FULL -> "Trip is already marked as full";
            case USER_NOT_TRIP_MEMBER -> "User is not a member of this trip";
            case USER_ALREADY_TRIP_MEMBER -> "User is already a member of this trip";
            case HOST_CANNOT_LEAVE_TRIP -> "Host cannot leave their own trip";
            case PARTICIPANT_NOT_FOUND -> "Participant not found";

            // Trip member role errors
            case INVALID_ROLE_CHANGE -> "Invalid role change operation";
            case USER_ALREADY_CO_HOST -> "User is already a co-host";
            case ONLY_MEMBER_CAN_BE_PROMOTED -> "Only members can be promoted to co-host";
            case HOST_CANNOT_BE_CO_HOST -> "Host cannot be made co-host";

            // Trip join request errors
            case JOIN_REQUEST_NOT_FOUND -> "Join request not found";
            case JOIN_REQUEST_NOT_PENDING -> "Join request is not pending";
            case JOIN_REQUEST_ALREADY_EXISTS -> "Join request already exists";
            case HOST_CANNOT_CREATE_JOIN_REQUEST -> "Trip host cannot create join request";
            case TRIP_NOT_JOINABLE -> "Trip is not joinable";
            case INVALID_JOIN_REQUEST_CANCEL -> "Cannot cancel this join request";

            // Trip Q&A errors
            case QUESTION_EMPTY -> "Question cannot be empty";
            case QNA_ALREADY_ANSWERED -> "Q&A has already been answered";
            case QNA_NOT_FOUND -> "Q&A not found";
            case INVALID_QNA_STATUS -> "Invalid Q&A status for this operation";

            // Trip reporting errors
            case TRIP_ALREADY_REPORTED -> "Trip has already been reported";
            case REPORT_REASON_EMPTY -> "Report reason cannot be empty";
            case REPORT_NOT_FOUND -> "Report not found";

            // Trip broadcast errors
            case TRIP_NOT_BROADCASTABLE -> "Trip cannot be broadcasted";
        };
    }

   

}
