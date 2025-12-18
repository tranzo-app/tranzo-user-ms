package com.tranzo.tranzo_user_ms.commons.exception;

import com.tranzo.tranzo_user_ms.user.dto.ResponseDto;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // 400 - Invalid uuid
    @ExceptionHandler(InvalidUserIdException.class)
    public ResponseEntity<ResponseDto<Void>> handleInvalidUserIdException(InvalidUserIdException ex) {
        ResponseDto<Void> response = ResponseDto.<Void>builder()
                .status("ERROR")
                .statusCode(400)
                .statusMessage(ex.getMessage())
                .data(null)
                .build();
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }
    // 404 - User profile not found
    @ExceptionHandler(UserProfileNotFoundException.class)
    public ResponseEntity<ResponseDto<Void>> handleUserProfileNotFoundException(UserProfileNotFoundException ex) {
        ResponseDto<Void> response = ResponseDto.<Void>builder()
                .status("ERROR")
                .statusCode(404)
                .statusMessage(ex.getMessage())
                .data(null)
                .build();
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }
    // 400 - Invalid patch request
    @ExceptionHandler(InvalidPatchRequestException.class)
    public ResponseEntity<ResponseDto<Void>> handleInvalidPatchRequestException(InvalidPatchRequestException ex) {
        ResponseDto<Void> response = ResponseDto.<Void>builder()
                .status("ERROR")
                .statusCode(400)
                .statusMessage(ex.getMessage())
                .data(null)
                .build();
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(UserAlreadyDeletedExeption.class)
    public ResponseEntity<ResponseDto<Void>> handleUserAlreadyDeletedException(UserAlreadyDeletedExeption ex) {
        ResponseDto<Void> response = ResponseDto.<Void>builder()
                .status("ERROR")
                .statusCode(400)
                .statusMessage(ex.getMessage())
                .data(null)
                .build();
        return ResponseEntity.status(400).body(response);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ResponseDto<Map<String, String>>> handleValidationErrors(
            MethodArgumentNotValidException ex) {

        Map<String, String> errors = new HashMap<>();

        // Field level errors
        ex.getBindingResult().getFieldErrors().forEach(error ->
                errors.put(error.getField(), error.getDefaultMessage())
        );

        // Class level errors
        ex.getBindingResult().getGlobalErrors().forEach(error ->
                errors.put(error.getObjectName(), error.getDefaultMessage())
        );

        ResponseDto<Map<String, String>> body = ResponseDto.<Map<String, String>>builder()
                .status("ERROR")
                .statusCode(400)
                .statusMessage("Validation failed")
                .data(errors)
                .build();

        return ResponseEntity.badRequest().body(body);
    }

    @ExceptionHandler(OtpException.class)
    public ResponseEntity<ResponseDto<Void>> handleOtpException(OtpException ex)
    {
        return ResponseEntity.badRequest()
                .body(ResponseDto.<Void>builder()
                .status("ERROR")
                .statusCode(400)
                .statusMessage(ex.getMessage())
                .data(null)
                .build());
    }

    @ExceptionHandler(InvalidReportRequestException.class)
    public ResponseEntity<ResponseDto<Void>> handleInvalidReportRequestException(InvalidReportRequestException ex) {
        ResponseDto<Void> response = ResponseDto.<Void>builder()
                .status("error")
                .statusCode(400)
                .statusMessage(ex.getMessage())
                .data(null)
                .build();
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(DuplicateReportException.class)
    public ResponseEntity<ResponseDto<Void>> handleUserReportAlreadyExistsException(DuplicateReportException ex) {
        ResponseDto<Void> response = ResponseDto.<Void>builder()
                .status("error")
                .statusCode(400)
                .statusMessage(ex.getMessage())
                .data(null)
                .build();
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

}
