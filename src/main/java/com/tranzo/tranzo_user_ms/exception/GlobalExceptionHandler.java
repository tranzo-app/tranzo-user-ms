//package com.tranzo.tranzo_user_ms.exception;
//
//import com.tranzo.tranzo_user_ms.dto.ResponseDto;
//import org.springframework.http.HttpStatus;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.ExceptionHandler;
//import org.springframework.web.bind.annotation.RestControllerAdvice;
//
//@RestControllerAdvice
//public class GlobalExceptionHandler {
//
//    @ExceptionHandler(BadRequestsException.class)
//    public ResponseEntity<ResponseDto> handleBadRequest(BadRequestsException ex) {
//        return ResponseEntity
//                .status(HttpStatus.BAD_REQUEST)
//                .body(new ResponseDto(HttpStatus.BAD_REQUEST.value(), ex.getMessage()));
//    }
//
//    @ExceptionHandler(TooManyRequestsException.class)
//    public ResponseEntity<ResponseDto> handleTooManyRequests(TooManyRequestsException ex) {
//        return ResponseEntity
//                .status(HttpStatus.TOO_MANY_REQUESTS)
//                .body(new ResponseDto(HttpStatus.TOO_MANY_REQUESTS.value(), ex.getMessage()));
//    }
//
//    @ExceptionHandler(Exception.class)
//    public ResponseEntity<ResponseDto> handleGeneric(Exception ex) {
//        return ResponseEntity
//                .status(HttpStatus.INTERNAL_SERVER_ERROR)
//                .body(new ResponseDto(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Something went wrong"));
//    }
//}