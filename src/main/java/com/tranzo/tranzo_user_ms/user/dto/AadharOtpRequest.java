package com.tranzo.tranzo_user_ms.user.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AadharOtpRequest {
    @JsonProperty("@entity")
    private String entity;

    @JsonProperty("aadhaar_number")
    private String aadhaarNumber;

    private String consent;

    private String reason;
}
