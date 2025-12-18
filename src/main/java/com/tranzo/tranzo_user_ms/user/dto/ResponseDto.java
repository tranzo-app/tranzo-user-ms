package com.tranzo.tranzo_user_ms.user.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ResponseDto<T> {
    @NotNull(message = "Status code cannot be null")
    private Integer statusCode;

    @NotBlank(message = "Status cannot be blank")
    private String status;

    @NotBlank(message = "Status message cannot be blank")
    private String statusMessage;

    private T data;

    // --------------------Factory Methods--------------------

    public static <T> ResponseDto<T> success(String message, T data) {
        return ResponseDto.<T>builder()
                .statusCode(200)
                .status("SUCCESS")
                .statusMessage(message)
                .data(data)
                .build();
    }

    public static <T> ResponseDto<T> success(int statusCode, String message, T data) {
        return ResponseDto.<T>builder()
                .statusCode(statusCode)
                .status("SUCCESS")
                .statusMessage(message)
                .data(data)
                .build();
    }

    public static <T> ResponseDto<T> failure(int statusCode, String message) {
        return ResponseDto.<T>builder()
                .statusCode(statusCode)
                .status("FAILURE")
                .statusMessage(message)
                .data(null)
                .build();
    }

    public static <T> ResponseDto<T> error(int statusCode, String message) {
        return ResponseDto.<T>builder()
                .statusCode(statusCode)
                .status("ERROR")
                .statusMessage(message)
                .data(null)
                .build();
    }
}
