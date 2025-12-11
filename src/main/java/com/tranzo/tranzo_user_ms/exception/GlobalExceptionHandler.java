package com.tranzo.tranzo_user_ms.exception;

import com.tranzo.tranzo_user_ms.dto.ResponseDto;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // 400 - Invalid uuid
    @ExceptionHandler(InvalidUserIdException.class)
    public ResponseEntity<ResponseDto<Void>> handleInvalidUserIdException(InvalidUserIdException ex) {
        ResponseDto<Void> response = ResponseDto.<Void>builder()
                .status("error")
                .statusCode(400)
                .statusMessage(ex.getMessage())
                .data(null )
                .build();
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }
    // 404 - User profile not found
    @ExceptionHandler(UserProfileNotFoundException.class)
    public ResponseEntity<ResponseDto<Void>> handleUserProfileNotFoundException(UserProfileNotFoundException ex) {
        ResponseDto<Void> response = ResponseDto.<Void>builder()
                .status("error")
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
                .status("error")
                .statusCode(400)
                .statusMessage(ex.getMessage())
                .data(null)
                .build();
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(UserAlreadyDeletedExeption.class)
    public ResponseEntity<ResponseDto<Void>> handleUserAlreadyDeletedException(UserAlreadyDeletedExeption ex) {
        ResponseDto<Void> response = ResponseDto.<Void>builder()
                .status("error")
                .statusCode(HttpStatus.CONFLICT.value())
                .statusMessage(ex.getMessage())
                .data(null)
                .build();
        return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
    }
}
