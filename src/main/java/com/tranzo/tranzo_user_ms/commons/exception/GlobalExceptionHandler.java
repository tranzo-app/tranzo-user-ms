package com.tranzo.tranzo_user_ms.commons.exception;

import com.tranzo.tranzo_user_ms.chat.exception.ConversationNotFoundException;
import com.tranzo.tranzo_user_ms.chat.exception.ConversationMutedException;
import com.tranzo.tranzo_user_ms.chat.exception.InvalidMessageException;
import com.tranzo.tranzo_user_ms.chat.exception.UserLeftConversationException;
import com.tranzo.tranzo_user_ms.chat.exception.ChatException;
import com.tranzo.tranzo_user_ms.chat.enums.ChatErrorCode;
import com.tranzo.tranzo_user_ms.commons.dto.ErrorDetailsDto;
import com.tranzo.tranzo_user_ms.commons.utility.ExceptionResponseUtil;
import com.tranzo.tranzo_user_ms.commons.dto.ResponseDto;
import com.tranzo.tranzo_user_ms.trip.enums.TripPublishErrorCode;
import com.tranzo.tranzo_user_ms.trip.exception.TripPublishException;
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
        log.warn("BaseException caught: {}", ex.getMessage());
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
        log.warn("ChatException caught: {}", ex.getErrorCode());

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

        log.warn("Validation errors: {}", errors);

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
        log.warn("AuthException caught: {}", ex.getMessage());

        ResponseDto<Void> response = ResponseDto.<Void>builder()
                .status("ERROR")
                .statusCode(ex.getStatusCode())
                .statusMessage(ex.getMessage())
                .data(null)
                .build();

        return ResponseEntity.status(ex.getStatusCode()).body(response);
    }

    /**
     * Handle ConversationNotFoundException - chat-specific exception
     */
    @ExceptionHandler(ConversationNotFoundException.class)
    public ResponseEntity<ResponseDto<Void>> handleConversationNotFoundException(
            ConversationNotFoundException ex) {
        log.warn("ConversationNotFoundException caught: {}", ex.getMessage());

        return ResponseEntity.status(404).body(
                ResponseDto.error(404, ex.getMessage())
        );
    }


    /**
     * Handle ConversationMutedException - chat-specific exception
     */
    @ExceptionHandler(ConversationMutedException.class)
    public ResponseEntity<ResponseDto<Void>> handleConversationMutedException(
            ConversationMutedException ex) {
        log.warn("ConversationMutedException caught: {}", ex.getMessage());

        return ResponseEntity.status(403).body(
                ResponseDto.error(403, ex.getMessage())
        );
    }

    /**
     * Handle InvalidMessageException - chat-specific exception
     */
    @ExceptionHandler(InvalidMessageException.class)
    public ResponseEntity<ResponseDto<Void>> handleInvalidMessageException(
            InvalidMessageException ex) {
        log.warn("InvalidMessageException caught: {}", ex.getMessage());

        return ResponseEntity.status(400).body(
                ResponseDto.error(400, ex.getMessage())
        );
    }

    /**
     * Handle UserLeftConversationException - chat-specific exception
     */
    @ExceptionHandler(UserLeftConversationException.class)
    public ResponseEntity<ResponseDto<Void>> handleUserLeftConversationException(
            UserLeftConversationException ex) {
        log.warn("UserLeftConversationException caught: {}", ex.getMessage());

        return ResponseEntity.status(410).body(
                ResponseDto.error(410, ex.getMessage())
        );
    }

    /**
     * Resolve user-friendly message for ChatErrorCode
     */
    private String resolveMessage(ChatErrorCode errorCode) {
        return switch (errorCode) {
            case CONVERSATION_NOT_FOUND -> "Conversation not found";
            case RECIPIENT_NOT_FOUND -> "Recipient user not found";
            case TRIP_NOT_FOUND -> "Trip not found";
            case INVALID_MESSAGE -> "Invalid message content";
            case MESSAGE_TOO_LONG -> "Message exceeds maximum length";
            case MESSAGE_EMPTY -> "Message cannot be empty";
            case USER_NOT_IN_CONVERSATION -> "User is not a participant in this conversation";
            case USER_LEFT_CONVERSATION -> "User has left this conversation";
            case USER_BLOCKED -> "User is blocked from this conversation";
            case USER_MUTED -> "User is muted in this conversation";
            case BLOCK_NOT_ALLOWED -> "Blocking is not allowed for this conversation type";
            case UNBLOCK_NOT_ALLOWED -> "Unblocking is not allowed for this conversation type";
            case SELF_CONVERSATION -> "Cannot create conversation with yourself";
            case INVALID_LIMIT -> "Invalid limit specified";
            case INVALID_CONVERSATION_TYPE -> "Invalid conversation type for this operation";
        };
    }

    /**
     * Handle TripPublishException - trip publishing validation errors
     */
    @ExceptionHandler(TripPublishException.class)
    public ResponseEntity<ResponseDto<ErrorDetailsDto>> handleTripPublishException(
            TripPublishException ex) {
        log.warn("TripPublishException caught: {}", ex.getErrorCode());

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

    /**
     * Fallback handler for all unhandled exceptions
     * (VERY IMPORTANT - Catch-all to prevent stack traces leaking to client)
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ResponseDto<Void>> handleUnhandledException(Exception ex) {
        log.error("Unhandled exception occurred", ex);

        return ExceptionResponseUtil.build(
                "Something went wrong. Please try again later.",
                500,
                null
        );
    }

    /**
     * Resolve user-friendly message for TripPublishErrorCode
     */
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
