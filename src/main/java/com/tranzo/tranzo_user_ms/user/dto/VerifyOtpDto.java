package com.tranzo.tranzo_user_ms.user.dto;

import com.tranzo.tranzo_user_ms.user.utility.VerifyEitherEmailOrMobile;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@VerifyEitherEmailOrMobile
public class VerifyOtpDto implements IdentifierAware{
//    Is country code needed here?
    @Pattern(regexp = "\\+[0-9]{1,4}", message = "Country code must start with + and contain 1–4 digits")
    private String countryCode;

    @Pattern(regexp = "[0-9]{7,15}", message = "Mobile number must be 7–15 digits")
    private String mobileNumber;

    @Email(message = "Invalid email format")
    private String emailId;

    @NotBlank(message = "OTP is required")
    @Size(min = 6, max = 6, message = "OTP must be 6 digits")
    @Pattern(regexp = "^\\d{6}$", message = "OTP must numeric")
    private String otp;
}