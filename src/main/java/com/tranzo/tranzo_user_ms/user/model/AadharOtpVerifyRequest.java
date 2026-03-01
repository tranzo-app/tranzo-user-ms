package com.tranzo.tranzo_user_ms.user.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AadharOtpVerifyRequest {
    @JsonProperty("@entity")
    private String entity;

    @JsonProperty("reference_id")
    private String referenceId;

    private String otp;
}
