package com.tranzo.tranzo_user_ms.commons.exception;

import com.tranzo.tranzo_user_ms.commons.utility.ExceptionResponseUtil;
import com.tranzo.tranzo_user_ms.user.dto.ResponseDto;
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

    // Fallback handler (VERY IMPORTANT)
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ResponseDto<Void>> handleUnhandledException(Exception ex) {
        return ExceptionResponseUtil.build(
                "Something went wrong",
                500,
                null
        );
    }
}