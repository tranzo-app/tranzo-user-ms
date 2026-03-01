package com.tranzo.tranzo_user_ms.user.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AadharOtpVerifyBaseResponse {
    private int code;

    private Long timestamp;

    @JsonProperty("transaction_id")
    private String transactionId;

    private String message;
}
