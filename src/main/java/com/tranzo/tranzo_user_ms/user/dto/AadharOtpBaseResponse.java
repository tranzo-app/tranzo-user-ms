package com.tranzo.tranzo_user_ms.user.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@Data
public class AadharOtpBaseResponse {
    private int code;

    private long timestamp;

    @JsonProperty("transaction_id")
    private String transactionId;

    private String message;
}
