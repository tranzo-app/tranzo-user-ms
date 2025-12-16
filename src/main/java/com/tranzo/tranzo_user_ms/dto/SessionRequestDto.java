package com.tranzo.tranzo_user_ms.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SessionRequestDto {

    @Pattern(regexp = "[0-9]{7,15}")
    private String mobileNumber;

    @Email
    private String emailId;
}

