package com.tranzo.tranzo_user_ms.dto;

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
}
