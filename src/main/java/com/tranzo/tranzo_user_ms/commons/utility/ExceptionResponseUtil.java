package com.tranzo.tranzo_user_ms.commons.utility;

import com.tranzo.tranzo_user_ms.commons.dto.ResponseDto;
import org.springframework.http.ResponseEntity;

public class ExceptionResponseUtil {
    public static <T> ResponseEntity<ResponseDto<T>> build (
            String message,
            int statusCode,
            T data
    ) {
        return ResponseEntity.status(statusCode)
                .body(ResponseDto.<T>builder()
                        .status("ERROR")
                        .statusCode(statusCode)
                        .statusMessage(message)
                        .data(data)
                        .build());
    }
}
