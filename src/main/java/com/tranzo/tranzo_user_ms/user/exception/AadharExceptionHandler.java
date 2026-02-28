package com.tranzo.tranzo_user_ms.user.exception;

import com.tranzo.tranzo_user_ms.commons.dto.ResponseDto;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class AadharExceptionHandler {

    @ExceptionHandler(AadharValidationException.class)
    public ResponseEntity<ResponseDto<Void>> handleValidation(AadharValidationException ex) {
        ResponseDto<Void> response = ResponseDto.<Void>builder()
                .status("ERROR")
                .statusCode(422)
                .statusMessage(ex.getMessage())
                .data(null)
                .build();
        return ResponseEntity.unprocessableEntity().body(response);
    }

    @ExceptionHandler(AadharServiceUnavailableException.class)
    public ResponseEntity<ResponseDto<Void>> handleServiceDown(AadharServiceUnavailableException ex) {
        ResponseDto<Void> response = ResponseDto.<Void>builder()
                .status("ERROR")
                .statusCode(503)
                .statusMessage(ex.getMessage())
                .data(null)
                .build();
        return ResponseEntity.status(503).body(response);
    }
}
