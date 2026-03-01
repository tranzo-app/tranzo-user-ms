package com.tranzo.tranzo_user_ms.user.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AadharOtpVerifySuccessResponse
        extends AadharOtpVerifyBaseResponse {
    private AadharVerifyData data;
}
