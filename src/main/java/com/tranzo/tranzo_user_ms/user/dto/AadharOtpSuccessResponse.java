package com.tranzo.tranzo_user_ms.user.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@EqualsAndHashCode(callSuper = true)
@Data
public class AadharOtpSuccessResponse extends AadharOtpBaseResponse {
    private DataNode data;

    @Data
    public static class DataNode {
        @JsonProperty("@entity")
        private String entity;

        @JsonProperty("reference_id")
        private Long referenceId;

        private String message;
    }
}
