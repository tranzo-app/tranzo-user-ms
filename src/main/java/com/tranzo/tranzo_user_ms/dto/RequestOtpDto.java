package com.tranzo.tranzo_user_ms.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import lombok.NonNull;

@Data
public class RequestOtpDto {
    @NotBlank(message = "Mobile number is required")
    private String mobileNumber;
}
