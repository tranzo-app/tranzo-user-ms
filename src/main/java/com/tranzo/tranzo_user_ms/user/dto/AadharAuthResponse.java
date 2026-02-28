package com.tranzo.tranzo_user_ms.user.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class AadharAuthResponse {
    private int code;

    private long timestamp;

    @JsonProperty("transaction_id")
    private String transactionId;

    private TokenData data;

    @Data
    public static class TokenData {
        @JsonProperty("access_token")
        private String accessToken;
    }
}
